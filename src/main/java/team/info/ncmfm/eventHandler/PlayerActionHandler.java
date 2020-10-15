package team.info.ncmfm.eventHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import team.info.ncmfm.NcmMod;
import team.info.ncmfm.manager.NeteaseCloudMusicManager;
import team.info.ncmfm.ui.MusicPannel;

public class PlayerActionHandler {

    @SubscribeEvent
    public void onRightClickMusicPannel(PlayerInteractEvent.RightClickItem event) {
        if(event.getSide()== Side.CLIENT){
            String itemName= event.getEntityPlayer().getHeldItem(EnumHand.MAIN_HAND).getItem().getRegistryName().toString();
            if(itemName.equals(ItemRegistryHandler.music_box.getRegistryName().toString())){
                Minecraft mc=Minecraft.getMinecraft();
                mc.displayGuiScreen(new MusicPannel(mc,new NeteaseCloudMusicManager()));
            }
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event){
        if(NcmMod.mp3Player!=null){
            NcmMod.mp3Player.close();
        }
    }
}
