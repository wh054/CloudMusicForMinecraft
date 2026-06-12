package team.info.ncmfm.eventHandler;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
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
}
