package team.info.ncmfm.eventHandler;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import team.info.ncmfm.item.ItemMusicBox;

public class ItemRegistryHandler {
    public static final ItemMusicBox music_box=new ItemMusicBox();

    @SubscribeEvent
    public void onRegistry(RegistryEvent.Register<Item> event){
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(music_box);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onModelRegistry(ModelRegistryEvent event){
        ResourceLocation resourceLocation=music_box.getRegistryName();
        ModelResourceLocation modelResourceLocation=new ModelResourceLocation(resourceLocation, "inventory");
        ModelLoader.setCustomModelResourceLocation(music_box, 0,modelResourceLocation);
    }
}
