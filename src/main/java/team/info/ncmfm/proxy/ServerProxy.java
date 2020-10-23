package team.info.ncmfm.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import team.info.ncmfm.eventHandler.BlockRegistryHandler;
import team.info.ncmfm.eventHandler.ItemRegistryHandler;
import team.info.ncmfm.interfaces.IProxy;
import team.info.ncmfm.net.MusicMessage;
import team.info.ncmfm.net.MusicMessageHandler;
import team.info.ncmfm.net.MusicPacketHandler;

public class ServerProxy implements IProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ItemRegistryHandler());
        MinecraftForge.EVENT_BUS.register(new BlockRegistryHandler());
    }

    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }
}
