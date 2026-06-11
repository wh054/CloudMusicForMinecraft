package team.info.ncmfm.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.QrStatus;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

/**
 * 二维码扫码登录界面：展示二维码，后台线程轮询扫码状态，扫码成功后自动进入音乐面板。
 */
@SideOnly(Side.CLIENT)
public class QrLoginScreen extends GuiScreen {
    private static final Logger logger = LogManager.getLogger(QrLoginScreen.class);
    private static final int QR_SIZE = 130;
    private static final long POLL_INTERVAL_MS = 1500L;

    private static final int BUTTON_REFRESH = 0;
    private static final int BUTTON_BACK = 1;

    private final int width;
    private final int height;
    private final IMusicManager musicManager;
    private final BlockPos blockPos;

    // 后台线程写、主线程读
    private volatile QrStatus status = QrStatus.WAITING;
    private volatile String pendingQrBase64;
    private volatile boolean running = true;
    private Thread worker;

    private ResourceLocation qrTextureLocation;
    private boolean switched = false;

    public QrLoginScreen(Minecraft mc, IMusicManager musicManager, BlockPos blockPos) {
        ScaledResolution scaled = new ScaledResolution(mc);
        this.width = scaled.getScaledWidth();
        this.height = scaled.getScaledHeight();
        this.musicManager = musicManager;
        this.blockPos = blockPos;
    }

    @Override
    public void initGui() {
        int cx = width / 2;
        int btnY = height / 2 + QR_SIZE / 2 + 12;
        this.buttonList.clear();
        this.buttonList.add(new GuiButton(BUTTON_REFRESH, cx - 105, btnY, 100, 20, "刷新二维码"));
        this.buttonList.add(new GuiButton(BUTTON_BACK, cx + 5, btnY, 100, 20, "返回"));
        startWorker();
    }

    private void startWorker() {
        stopWorker();
        running = true;
        status = QrStatus.WAITING;
        pendingQrBase64 = null;
        worker = new Thread(new Runnable() {
            @Override
            public void run() {
                String img = musicManager.createQrCode();
                if (img == null) {
                    status = QrStatus.ERROR;
                    return;
                }
                pendingQrBase64 = img;
                status = QrStatus.WAITING;

                while (running) {
                    try {
                        Thread.sleep(POLL_INTERVAL_MS);
                    } catch (InterruptedException e) {
                        return;
                    }
                    if (!running) {
                        return;
                    }
                    QrStatus s = musicManager.checkQrStatus();
                    if (s == QrStatus.EXPIRED) {
                        String refreshed = musicManager.createQrCode();
                        if (refreshed != null) {
                            pendingQrBase64 = refreshed;
                            status = QrStatus.WAITING;
                            continue;
                        }
                        status = QrStatus.EXPIRED;
                        continue;
                    }
                    status = s;
                    if (s == QrStatus.CONFIRMED) {
                        running = false;
                        return;
                    }
                }
            }
        }, "ncmfm-qr-poll");
        worker.setDaemon(true);
        worker.start();
    }

    private void stopWorker() {
        running = false;
        if (worker != null) {
            worker.interrupt();
            worker = null;
        }
    }

    @Override
    public void updateScreen() {
        // 纹理上传必须在主线程（持有 GL 上下文）
        String base64 = pendingQrBase64;
        if (base64 != null) {
            pendingQrBase64 = null;
            uploadQrTexture(base64);
        }

        if (status == QrStatus.CONFIRMED && !switched) {
            switched = true;
            running = false;
            if (blockPos != null) {
                mc.displayGuiScreen(new MusicPannel(mc, musicManager, blockPos));
            } else {
                mc.displayGuiScreen(new MusicPannel(mc, musicManager));
            }
        }
    }

    private void uploadQrTexture(String dataUrl) {
        try {
            int comma = dataUrl.indexOf(',');
            String base64 = comma >= 0 ? dataUrl.substring(comma + 1) : dataUrl;
            byte[] bytes = Base64.getDecoder().decode(base64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                return;
            }
            if (qrTextureLocation != null) {
                mc.getTextureManager().deleteTexture(qrTextureLocation);
            }
            DynamicTexture texture = new DynamicTexture(image);
            qrTextureLocation = mc.getTextureManager().getDynamicTextureLocation("ncmfm_qr", texture);
        } catch (Exception e) {
            logger.error("Failed to decode QR image: " + e.getMessage());
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, "扫码登录网易云音乐", width / 2, height / 2 - QR_SIZE / 2 - 28, 0xFFFFFF);

        int qrX = (width - QR_SIZE) / 2;
        int qrY = height / 2 - QR_SIZE / 2;
        if (qrTextureLocation != null) {
            // 白色底，避免二维码透明边被深色背景吞掉
            drawRect(qrX - 4, qrY - 4, qrX + QR_SIZE + 4, qrY + QR_SIZE + 4, 0xFFFFFFFF);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(qrTextureLocation);
            Gui.drawModalRectWithCustomSizedTexture(qrX, qrY, 0.0F, 0.0F, QR_SIZE, QR_SIZE, QR_SIZE, QR_SIZE);
        } else {
            drawCenteredString(fontRenderer, "二维码加载中...", width / 2, qrY + QR_SIZE / 2, 0xAAAAAA);
        }

        drawCenteredString(fontRenderer, statusText(), width / 2, qrY + QR_SIZE + 6, statusColor());
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private String statusText() {
        switch (status) {
            case WAITING:
                return "请使用网易云音乐 App 扫描上方二维码";
            case SCANNED:
                return "已扫描，请在手机上点击确认登录";
            case CONFIRMED:
                return "登录成功，正在进入...";
            case EXPIRED:
                return "二维码已过期，请点击刷新";
            default:
                return "获取二维码失败，请检查 API 服务后刷新";
        }
    }

    private int statusColor() {
        switch (status) {
            case CONFIRMED:
                return 0x55FF55;
            case SCANNED:
                return 0xFFFF55;
            case EXPIRED:
            case ERROR:
                return 0xFF5555;
            default:
                return 0xFFFFFF;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case BUTTON_REFRESH:
                startWorker();
                break;
            case BUTTON_BACK:
                mc.displayGuiScreen(null);
                break;
            default:
                break;
        }
    }

    @Override
    public void onGuiClosed() {
        stopWorker();
        if (qrTextureLocation != null) {
            mc.getTextureManager().deleteTexture(qrTextureLocation);
            qrTextureLocation = null;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
