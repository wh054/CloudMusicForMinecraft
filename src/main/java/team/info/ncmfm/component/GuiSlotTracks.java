package team.info.ncmfm.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.client.GuiScrollingList;
import net.minecraftforge.fml.common.ModContainer;
import team.info.ncmfm.model.TrackContainer;
import team.info.ncmfm.ui.MusicPannel;

import java.util.ArrayList;

public class GuiSlotTracks extends GuiScrollingList {

    private MusicPannel parent;

    public GuiSlotTracks(MusicPannel parent, ArrayList<TrackContainer> trackList, int listWidth, int slotHeight)
    {
        super(parent.getMinecraftInstance(), listWidth, parent.height, 32, parent.height - 88 + 4, 10, slotHeight, parent.width, parent.height);
        this.parent = parent;
    }

    @Override
    protected int getSize() {
        return 0;
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {

    }

    @Override
    protected boolean isSelected(int index) {
        return false;
    }

    @Override
    protected void drawBackground() {
        this.parent.drawDefaultBackground();
    }

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        TrackContainer tc     = null;
        String       name     = StringUtils.stripControlCodes(tc.getName());
        FontRenderer font     = this.parent.getFontRenderer();

        font.drawString(font.trimStringToWidth(name,    listWidth - 10), this.left + 3 , top +  2, 0xFFFFFF);
    }
}
