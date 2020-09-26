package team.info.ncmfm.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import team.info.ncmfm.NcmMod;
import team.info.ncmfm.eventHandler.ItemRegistryHandler;
import team.info.ncmfm.interfaces.IProxy;

public class ServerProxy implements IProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ItemRegistryHandler());
        NcmMod.AudioBufferChannel=NetworkRegistry.INSTANCE.newEventDrivenChannel("AudioBuffer");
    }

    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }
}
