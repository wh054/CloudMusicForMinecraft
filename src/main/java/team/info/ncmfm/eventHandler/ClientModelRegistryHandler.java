package team.info.ncmfm.eventHandler;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * 客户端专用：注册物品和方块的模型。
 * 此类只在 ClientProxy 中注册到事件总线，服务端不会加载。
 */
@SideOnly(Side.CLIENT)
public class ClientModelRegistryHandler {

    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent event) {
        ModelResourceLocation musicBox_modelResourceLocation =
                new ModelResourceLocation(ItemRegistryHandler.music_box.getRegistryName(), "inventory");
        ModelResourceLocation musicCube_modelResourceLocation =
                new ModelResourceLocation(ItemRegistryHandler.music_cube.getRegistryName(), "inventory");

        ModelLoader.setCustomModelResourceLocation(ItemRegistryHandler.music_box, 0, musicBox_modelResourceLocation);
        ModelLoader.setCustomModelResourceLocation(ItemRegistryHandler.music_cube, 0, musicCube_modelResourceLocation);
    }
}
