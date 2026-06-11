package team.info.ncmfm.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.GuiScrollingList;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.ui.MusicPannel;

import java.util.ArrayList;


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
            this.parent.LoadTrackList(this.parent.getPlayListTracks(lc.getId()));
        }
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
