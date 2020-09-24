package team.info.ncmfm.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.common.ModContainer;
import team.info.ncmfm.model.PlayListContainer;
import team.info.ncmfm.model.TrackContainer;
import team.info.ncmfm.ui.MusicPannel;

import java.util.ArrayList;

public class GuiSlotTracks extends GuiScrollingList {

    private MusicPannel parent;
    private ArrayList<TrackContainer> collections;
    private int slotHeight;

    public GuiSlotTracks(MusicPannel parent, ArrayList<TrackContainer> trackList, int listWidth, int slotHeight)
    {
        super(parent.getMinecraftInstance(), parent.width/2-30, parent.height, 32, parent.height - 88 + 4, parent.width/2+20, slotHeight, parent.width, parent.height);
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
        TrackContainer tc     = this.collections.get(slotIdx);
        String       name     = StringUtils.stripControlCodes(tc.getName());
        FontRenderer font     = this.parent.getFontRenderer();

        font.drawString(font.trimStringToWidth(name,listWidth - 10), this.left + 3 , slotTop +  2, 0xFFFFFF);
    }
}
