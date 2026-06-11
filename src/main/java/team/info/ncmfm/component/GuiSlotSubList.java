package team.info.ncmfm.component;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.client.GuiScrollingList;
import team.info.ncmfm.model.SubListContainer;
import team.info.ncmfm.ui.MusicPannel;

import java.util.ArrayList;

public class GuiSlotSubList extends GuiScrollingList {
    private MusicPannel parent;
    private ArrayList<SubListContainer> collections;
    private int slotHeight;

    public GuiSlotSubList(MusicPannel parent, ArrayList<SubListContainer> collections, int left, int top, int width, int height, int slotHeight){
        super(parent.getMinecraftInstance(), width, parent.height, top, top + height, left, slotHeight, parent.width, parent.height);
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
            this.parent.openAlbum(slc);
        }
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

        font.drawString(font.trimStringToWidth(name, listWidth - 10), this.left + 4 , slotTop +  4, 0xFFFFFF);
    }
}
