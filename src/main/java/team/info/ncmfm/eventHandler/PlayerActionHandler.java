package team.info.ncmfm.eventHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.info.ncmfm.manager.NeteaseCloudMusicManager;
import team.info.ncmfm.ui.MusicPannel;

public class PlayerActionHandler {

    @SubscribeEvent
    public void onRightClickMusicPannel(PlayerInteractEvent.RightClickItem event) {
        String itemName= event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem().getRegistryName().toString();
        if(itemName.equals(ItemRegistryHandler.music_box.getRegistryName().toString())){
            Minecraft mc=Minecraft.getMinecraft();
            mc.displayGuiScreen(new MusicPannel(mc,new NeteaseCloudMusicManager()));
        }
    }
}
