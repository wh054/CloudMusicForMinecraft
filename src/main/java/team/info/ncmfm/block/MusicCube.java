package team.info.ncmfm.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class MusicCube extends Block {
    public MusicCube(){
        super(Material.WOOD);
        this.setRegistryName("music_cube");
        this.setUnlocalizedName("MusicCube");
        this.setCreativeTab(CreativeTabs.DECORATIONS);
        this.setHardness(5.0F);
        this.setLightLevel(0.3f);
    }
}
