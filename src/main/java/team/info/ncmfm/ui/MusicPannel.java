package team.info.ncmfm.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.info.ncmfm.NcmMod;
import team.info.ncmfm.component.GuiSlotPlayList;
import team.info.ncmfm.component.GuiSlotTracks;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.TrackContainer;

import java.io.IOException;
import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class MusicPannel extends GuiScreen {
    private ResourceLocation texture = new ResourceLocation(NcmMod.MODID, "textures/gui/screen2.jpg");
    public final int width;
    public final int height;

    private int playListWidth;
    private int tracksWidth=200;

    private static final int BUTTON_GET_MUSICLIST = 0;

    private ArrayList<PlayListContainer> playList;
    private ArrayList<TrackContainer> trackList;

    private GuiSlotPlayList slotPlayList;
    private GuiSlotTracks slotTracks;

    private int playList_selected_index = -1;
    private int track_selected_index=-1;
    private PlayListContainer selectedPlayList;
    private TrackContainer selectedTrack;

    private IMusicManager musicManager;

    public MusicPannel(Minecraft mc,IMusicManager musicManager){
        playList=new ArrayList<>();
        trackList=new ArrayList<>();
        ScaledResolution scaled = new ScaledResolution(mc);
        width = scaled.getScaledWidth();
        height = scaled.getScaledHeight();
        this.musicManager=musicManager;
    }

    @Override
    public void initGui() {
        int slotHeight = 15;
        playList.addAll(musicManager.LoadPlayList());
        for (PlayListContainer plc : playList)
        {
            playListWidth = Math.max(playListWidth,getFontRenderer().getStringWidth(plc.getName()) + 10);
        }

        for (TrackContainer tc : trackList)
        {
            tracksWidth = Math.max(tracksWidth,getFontRenderer().getStringWidth(tc.getName()) + 10);
        }

        this.buttonList.add(new GuiButton(BUTTON_GET_MUSICLIST,(int)(width*0.75), (int)(height*0.85), 80, 20,"更新歌单"));
        this.slotPlayList = new GuiSlotPlayList(this, playList, playListWidth, slotHeight);
        this.slotTracks=new GuiSlotTracks(this,trackList,tracksWidth,slotHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.renderEngine.bindTexture(texture);
        drawScaledCustomSizeModalRect(0, 0, 0, 0, 1828, (int)(1828*(height/(float)width)), width, height, 1828, 1292);
        this.slotPlayList.drawScreen(mouseX, mouseY, partialTicks);
        this.slotTracks.drawScreen(mouseX, mouseY, partialTicks);
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

    public void selectPlayListIndex(int index)
    {
        if (index == this.playList_selected_index)
            return;
        this.playList_selected_index = index;
        this.selectedPlayList = (index >= 0 && index <= playList.size()) ? playList.get(playList_selected_index) : null;
    }

    public void selectTrackIndex(int index){
        if (index == this.track_selected_index){
            return;
        }
        this.track_selected_index = index;
        this.selectedTrack = (index >= 0 && index <= trackList.size()) ? trackList.get(track_selected_index) : null;
    }


    public boolean playListIndexSelected(int index)
    {
        return index == playList_selected_index;
    }

    public boolean trackIndexSelected(int index){
        return index==track_selected_index;
    }

    public void LoadTrackList(){
        trackList.clear();
        PlayListContainer plc= playList.get(this.playList_selected_index);
        if(plc!=null){
            trackList.addAll(musicManager.LoadTrackList(plc.getId()));
        }
    }

    public void PlayMusic(){
        String msg="点播歌曲==>"+this.selectedTrack.getName();
        super.sendChatMessage(msg,true);
    }
}
