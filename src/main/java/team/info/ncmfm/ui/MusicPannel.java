package team.info.ncmfm.ui;

import com.google.gson.Gson;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import team.info.ncmfm.NcmMod;
import team.info.ncmfm.component.GuiSlotPlayList;
import team.info.ncmfm.component.GuiSlotTracks;
import team.info.ncmfm.interfaces.IMusicManager;
import team.info.ncmfm.model.MusicInfoWrapper;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.TrackContainer;
import team.info.ncmfm.net.EnumMusicCommand;
import team.info.ncmfm.net.MusicMessage;
import team.info.ncmfm.net.MusicPacketHandler;

import java.io.IOException;
import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class MusicPannel extends GuiScreen {
    private ResourceLocation texture = new ResourceLocation(NcmMod.MODID, "textures/gui/screen2.jpg");
    public final int width;
    public final int height;

    private int playListWidth;
    private int tracksWidth=200;

    private static final int BUTTON_STOP_MUSIC = 0;
    private static final int BUTTON_REFLASH_STATE =1;

    private ArrayList<PlayListContainer> playList;
    private ArrayList<TrackContainer> trackList;

    private GuiSlotPlayList slotPlayList;
    private GuiSlotTracks slotTracks;

    private int playList_selected_index = -1;
    private int track_selected_index=-1;
    private PlayListContainer selectedPlayList;
    private TrackContainer selectedTrack;

    private IMusicManager musicManager;
    private BlockPos blockPos;

    public MusicPannel(Minecraft mc,IMusicManager musicManager){
        playList=new ArrayList<>();
        trackList=new ArrayList<>();
        ScaledResolution scaled = new ScaledResolution(mc);
        width = scaled.getScaledWidth();
        height = scaled.getScaledHeight();
        this.musicManager=musicManager;
    }

    public MusicPannel(Minecraft mc, IMusicManager musicManager, BlockPos pos){
        playList=new ArrayList<>();
        trackList=new ArrayList<>();
        ScaledResolution scaled = new ScaledResolution(mc);
        width = scaled.getScaledWidth();
        height = scaled.getScaledHeight();
        this.musicManager=musicManager;
        this.blockPos=pos;
    }

    @Override
    public void initGui() {
        int slotHeight = 15;
        musicManager.login();
        playList.addAll(musicManager.LoadPlayList());
        for (PlayListContainer plc : playList)
        {
            playListWidth = Math.max(playListWidth,getFontRenderer().getStringWidth(plc.getName()) + 10);
        }

        for (TrackContainer tc : trackList)
        {
            tracksWidth = Math.max(tracksWidth,getFontRenderer().getStringWidth(tc.getName()) + 10);
        }

        this.buttonList.add(new GuiButton(BUTTON_STOP_MUSIC,(int)(width*0.75), (int)(height*0.85), 80, 20,"停止播放"));
        this.buttonList.add(new GuiButton(BUTTON_REFLASH_STATE,(int)(width*0.25), (int)(height*0.85), 80, 20,"更新"));
        this.slotPlayList = new GuiSlotPlayList(this, playList, playListWidth, slotHeight);
        this.slotTracks=new GuiSlotTracks(this,trackList,tracksWidth,slotHeight);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        mc.renderEngine.bindTexture(texture);
        drawScaledCustomSizeModalRect(0, 0, 0, 0, 1828, (int)(1828*(height/(float)width)), width, height, 1828, 1292);
        if(this.slotPlayList!=null){
            this.slotPlayList.drawScreen(mouseX, mouseY, partialTicks);
        }
        if(this.slotTracks!=null){
            this.slotTracks.drawScreen(mouseX, mouseY, partialTicks);
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void updateScreen() {

    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch(button.id){
            case BUTTON_REFLASH_STATE:
                musicManager.updateLoginState();
                break;
            case BUTTON_STOP_MUSIC:
                StopMusic();
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
        MusicInfoWrapper packet=new MusicInfoWrapper();
        packet.setCommand(EnumMusicCommand.PLAY);
        String musicUrl=musicManager.GetMusicById(this.selectedTrack.getId());
        packet.setSource(musicUrl);
        if(blockPos!=null){
            packet.setPos(this.blockPos);
        }else {
            String msg="点播歌曲==>"+this.selectedTrack.getName();
            super.sendChatMessage(msg,true);
        }
        MusicPacketHandler.INSTANCE.sendToServer(new MusicMessage(new Gson().toJson(packet)));
    }

    public void StopMusic(){
        MusicInfoWrapper packet=new MusicInfoWrapper();
        packet.setCommand(EnumMusicCommand.STOP);
        if(this.blockPos!=null){
            packet.setPos(this.blockPos);
        }
        MusicPacketHandler.INSTANCE.sendToServer(new MusicMessage(new Gson().toJson(packet)));
    }
}
