package team.info.ncmfm.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.GuiScrollingList;
import team.info.ncmfm.entity.PlayList;
import team.info.ncmfm.entity.TrackCollection;
import team.info.ncmfm.manager.NeteaseCloudMusicManager;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.SubListContainer;
import team.info.ncmfm.model.TrackContainer;
import team.info.ncmfm.ui.MusicPannel;

import java.util.ArrayList;
import java.util.List;

public class GuiSlotPlayList extends GuiScrollingList {

    private MusicPannel parent;
    private ArrayList<PlayListContainer> collections;
    private int slotHeight;

    public GuiSlotPlayList(MusicPannel parent, ArrayList<PlayListContainer> collections, int slotHeight)
    {
        super(parent.getMinecraftInstance(), parent.width/3-10, parent.height, 8, parent.height/2-4, 10, slotHeight, parent.width, parent.height);
        this.parent=parent;
        this.collections=collections;
        this.slotHeight=slotHeight;
    }

    @Override
    protected int getSize() {
        return this.collections.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        this.parent.selectPlayListIndex(index);
        if(doubleClick){
            PlayListContainer lc= collections.get(index);
            List<TrackContainer> trackList= getTrackList(lc.getId());
            this.parent.LoadTrackList(trackList);
        }
    }

    private ArrayList<TrackContainer> getTrackList(long id) {
        ArrayList<TrackContainer> as=new ArrayList<>();
        TrackCollection trackCollection=null;

        try{
            String collectionId=Long.toString(id);
            if(NeteaseCloudMusicManager.cache.containsKey(collectionId)){
                trackCollection=(TrackCollection)NeteaseCloudMusicManager.cache.get(collectionId);
            }else {
                trackCollection=NeteaseCloudMusicManager.GetTracksById(id);
                if(trackCollection.getPlaylist()!=null){
                    NeteaseCloudMusicManager.cache.put(collectionId,trackCollection);
                }
            }
            if(trackCollection.getPlaylist() !=null){
                for(PlayList.Tracks temp: trackCollection.getPlaylist().getTracks()){
                    as.add(new TrackContainer(
                            temp.getId(),
                            temp.getName(),
                            temp.getAr().get(0).getName(),
                            temp.getAl().getName()
                    ));
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        return as;
    }

    @Override
    protected boolean isSelected(int index) {
        return this.parent.playListIndexSelected(index);
    }

    @Override
    protected void drawBackground() {
        //this.parent.drawDefaultBackground();
    }

    @Override
    protected int getContentHeight() {
        return (this.getSize()) * slotHeight + 1;
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        PlayListContainer plc       = this.collections.get(slotIdx);
        String       name     = StringUtils.stripControlCodes(plc.getName());
        FontRenderer font     = this.parent.getFontRenderer();

        font.drawString(font.trimStringToWidth(name,listWidth - 10), this.left + 3 , slotTop +  2, 0xFFFFFF);
    }
}
