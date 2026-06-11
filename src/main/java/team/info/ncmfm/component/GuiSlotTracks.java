package team.info.ncmfm.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.GuiScrollingList;
import team.info.ncmfm.model.TrackContainer;
import team.info.ncmfm.ui.MusicPannel;

import java.util.ArrayList;

public class GuiSlotTracks extends GuiScrollingList {

    private MusicPannel parent;
    private ArrayList<TrackContainer> collections;
    private int slotHeight;

    public GuiSlotTracks(MusicPannel parent, ArrayList<TrackContainer> trackList, int left, int top, int width, int height, int slotHeight)
    {
        super(parent.getMinecraftInstance(), width, parent.height, top, top + height, left, slotHeight, parent.width, parent.height);
        this.parent = parent;
        this.collections=trackList;
        this.slotHeight=slotHeight;
    }

    @Override
    protected int getSize() {
        return this.collections.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
            this.parent.selectTrackIndex(index);
            if(doubleClick){
                //play music
                this.parent.PlayMusic();
            }
    }

    @Override
    protected boolean isSelected(int index) {
        return this.parent.trackIndexSelected(index);
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
        TrackContainer tc = this.collections.get(slotIdx);
        String title = StringUtils.stripControlCodes(tc.getName() == null ? "" : tc.getName());
        String artist = StringUtils.stripControlCodes(tc.getAuthor() == null ? "" : tc.getAuthor());
        String album = StringUtils.stripControlCodes(tc.getAlbum() == null ? "" : tc.getAlbum());
        String meta = artist.length() == 0 ? album : (album.length() == 0 ? artist : artist + " - " + album);
        FontRenderer font = this.parent.getFontRenderer();

        font.drawString(font.trimStringToWidth(title, listWidth - 12), this.left + 4 , slotTop + 2, 0xFFFFFF);
        if (meta.length() > 0 && slotHeight >= 22) {
            font.drawString(font.trimStringToWidth(meta, listWidth - 12), this.left + 4 , slotTop + 12, 0xAAAAAA);
        }
    }
}
