package team.info.ncmfm.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class ItemMusicBox extends Item {
    public ItemMusicBox(){
        this.setRegistryName("music_box");
        this.setUnlocalizedName("musicBox");
        this.setCreativeTab(CreativeTabs.TOOLS);
    }
}
