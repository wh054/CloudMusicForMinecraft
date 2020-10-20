package team.info.ncmfm.eventHandler;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import team.info.ncmfm.block.MusicCube;
import team.info.ncmfm.item.ItemMusicBox;

public class ItemRegistryHandler {
    public static final ItemMusicBox music_box=new ItemMusicBox();
    public static final ItemBlock music_cube=new ItemBlock(BlockRegistryHandler.music_cube);

    @SubscribeEvent
    public void onRegistry(RegistryEvent.Register<Item> event){
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(music_box);

        music_cube.setRegistryName(music_cube.getBlock().getRegistryName());
        registry.register(music_cube);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelRegistry(ModelRegistryEvent event){
        ModelResourceLocation musicBox_modelResourceLocation=new ModelResourceLocation(music_box.getRegistryName(), "inventory");
        ModelResourceLocation musicCube_modelResourceLocation=new ModelResourceLocation(music_cube.getRegistryName(), "inventory");

        ModelLoader.setCustomModelResourceLocation(music_box, 0,musicBox_modelResourceLocation);
        ModelLoader.setCustomModelResourceLocation(music_cube, 0 ,musicCube_modelResourceLocation);
    }
}
