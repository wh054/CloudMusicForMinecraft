package team.info.ncmfm;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import team.info.ncmfm.item.ItemMusicBox;

@Mod.EventBusSubscriber
public class ItemRegistryHandler {
    public static final ItemMusicBox music_box=new ItemMusicBox();

    @SubscribeEvent
    public static void onRegistry(RegistryEvent.Register<Item> event){
        IForgeRegistry<Item> registry = event.getRegistry();
        registry.register(music_box);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onModelRegistry(ModelRegistryEvent event){
        ModelLoader.setCustomModelResourceLocation(music_box, 0,
                new ModelResourceLocation(music_box.getRegistryName(), "inventory"));
    }
}
