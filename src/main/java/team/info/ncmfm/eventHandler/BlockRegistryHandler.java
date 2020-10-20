package team.info.ncmfm.eventHandler;

import net.minecraft.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import team.info.ncmfm.block.MusicCube;

public class BlockRegistryHandler {
    public static final MusicCube music_cube=new MusicCube();

    @SubscribeEvent
    public void onBlockRegister(RegistryEvent.Register<Block> event){
        IForgeRegistry<Block> registry =  event.getRegistry();
        registry.register(music_cube);
    }
}
