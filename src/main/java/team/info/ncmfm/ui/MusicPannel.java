package team.info.ncmfm.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.info.ncmfm.NcmMod;
import team.info.ncmfm.component.GuiSlotPlayList;
import team.info.ncmfm.component.GuiSlotTracks;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.PlayListContainer;

import java.io.IOException;
import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class MusicPannel extends GuiScreen {
    private ResourceLocation texture = new ResourceLocation(NcmMod.MODID, "textures/gui/screen.png");
    public final int width;
    public final int height;

    private int playListWidth;
    private int tracksWidth;

    private static final int BUTTON_GET_MUSICLIST = 0;

    private ArrayList<PlayListContainer> playList;

    private GuiSlotPlayList slotPlayList;
    private GuiSlotTracks slotTracks;

    private IMusicManager musicManager;

    public MusicPannel(Minecraft mc,IMusicManager musicManager){
        playList=new ArrayList<>();
        ScaledResolution scaled = new ScaledResolution(mc);
        width = scaled.getScaledWidth();
        height = scaled.getScaledHeight();
        this.musicManager=musicManager;
    }

    @Override
    public void initGui() {
        int slotHeight = 35;
        playList.addAll(musicManager.LoadPlayList());
        for (PlayListContainer plc : playList)
        {
            playListWidth = Math.max(playListWidth,getFontRenderer().getStringWidth(plc.getName()) + 10);
        }

        this.buttonList.add(new GuiButton(BUTTON_GET_MUSICLIST,(int)(width*0.75), (int)(height*0.85), 80, 20,"¸üÐÂ¸èµ¥"));
        this.slotPlayList = new GuiSlotPlayList(this, playList, playListWidth, slotHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.renderEngine.bindTexture(texture);
        drawScaledCustomSizeModalRect(0, 0, 0, 0, 1828, (int)(1828*(height/(float)width)), width, height, 1828, 1292);
        this.slotPlayList.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch(button.id){
            case BUTTON_GET_MUSICLIST:
                playList.clear();
                playList.addAll(musicManager.LoadPlayList());
                break;
            default:
                super.actionPerformed(button);
        }
    }

    public Minecraft getMinecraftInstance()
    {
        return mc;
    }

    public FontRenderer getFontRenderer()
    {
        return fontRenderer;
    }
}
