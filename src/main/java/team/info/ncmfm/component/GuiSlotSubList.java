package team.info.ncmfm.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.GuiScrollingList;
import team.info.ncmfm.entity.AlbumTracks;
import team.info.ncmfm.manager.NeteaseCloudMusicManager;
import team.info.ncmfm.model.SubListContainer;
import team.info.ncmfm.model.TrackContainer;
import team.info.ncmfm.ui.MusicPannel;

import java.util.ArrayList;
import java.util.List;

public class GuiSlotSubList extends GuiScrollingList {
    private MusicPannel parent;
    private ArrayList<SubListContainer> collections;
    private int slotHeight;

    public GuiSlotSubList(MusicPannel parent, ArrayList<SubListContainer> collections, int slotHeight){
        super(parent.getMinecraftInstance(), parent.width/3-10, parent.height, parent.height/2+4, parent.height-50, 10, slotHeight, parent.width, parent.height);
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
        this.parent.selectSubListIndex(index);
        if(doubleClick){
            SubListContainer slc= collections.get(index);
            List<TrackContainer> trackList= getTrackList(slc.getId());
            this.parent.LoadTrackList(trackList);
        }
    }

    private ArrayList<TrackContainer> getTrackList(long id) {
        ArrayList<TrackContainer> as=new ArrayList<>();
        AlbumTracks albumTracks=null;

        String collectionId=Long.toString(id);
        if(NeteaseCloudMusicManager.cache.containsKey(collectionId)){
            albumTracks=(AlbumTracks)NeteaseCloudMusicManager.cache.get(collectionId);
        }else {
            albumTracks=NeteaseCloudMusicManager.GetTracksBySubId(id);
            NeteaseCloudMusicManager.cache.put(collectionId,albumTracks);
        }
        if(albumTracks!=null){
            for(AlbumTracks.SongsBean temp: albumTracks.getSongs()){
                as.add(new TrackContainer(
                        temp.getId(),
                        temp.getName(),
                        temp.getAr().get(0).getName(),
                        temp.getAl().getName()
                ));
            }
        }
        return as;
    }

    @Override
    protected boolean isSelected(int index) {
        return this.parent.subListIndexSelected(index);
    }

    @Override
    protected void drawBackground() {

    }

    @Override
    protected int getContentHeight() {
        return (this.getSize()) * slotHeight + 1;
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        SubListContainer slc       = this.collections.get(slotIdx);
        String       name     = StringUtils.stripControlCodes(slc.getName());
        FontRenderer font     = this.parent.getFontRenderer();

        font.drawString(font.trimStringToWidth(name,listWidth - 10), this.left + 3 , slotTop +  2, 0xFFFFFF);
    }
}
