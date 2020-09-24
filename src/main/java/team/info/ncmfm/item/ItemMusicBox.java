package team.info.ncmfm.item;

import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import team.info.ncmfm.manager.NeteaseCloudMusicManager;
import team.info.ncmfm.ui.MusicPannel;

public class ItemMusicBox extends Item {
    public ItemMusicBox(){
        this.setRegistryName("music_box");
        this.setUnlocalizedName("musicBox");
        this.setCreativeTab(CreativeTabs.TOOLS);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (worldIn.isRemote) {
            Minecraft mc=Minecraft.getMinecraft();
            mc.displayGuiScreen(new MusicPannel(mc,new NeteaseCloudMusicManager()));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }
}
