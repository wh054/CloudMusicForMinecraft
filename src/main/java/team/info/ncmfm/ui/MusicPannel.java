package team.info.ncmfm.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.info.ncmfm.NcmMod;

import java.io.IOException;

@SideOnly(Side.CLIENT)
public class MusicPannel extends GuiScreen {
    private ResourceLocation texture = new ResourceLocation(NcmMod.MODID, "textures/gui/screen2.jpg");
    private final int width;
    private final int height;

    private static final int BUTTON_GET_MUSICLIST = 0;

    public MusicPannel(Minecraft mc){
        ScaledResolution scaled = new ScaledResolution(mc);
        width = scaled.getScaledWidth();
        height = scaled.getScaledHeight();
    }

    @Override
    public void initGui() {
        this.buttonList.add(new GuiButton(BUTTON_GET_MUSICLIST,(int)(width*0.75), (int)(height*0.85), 80, 20,"¸üÐÂ¸èµ¥"));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.renderEngine.bindTexture(texture);
        drawScaledCustomSizeModalRect(0, 0, 0, 0, 1828, (int)(1828*(height/(float)width)), width, height, 1828, 1292);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);
    }
}
