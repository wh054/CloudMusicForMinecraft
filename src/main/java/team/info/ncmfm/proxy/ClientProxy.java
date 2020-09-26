package team.info.ncmfm.proxy;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import team.info.ncmfm.eventHandler.ItemRegistryHandler;
import team.info.ncmfm.eventHandler.PlayerActionHandler;
import team.info.ncmfm.eventHandler.RenderGuiHandler;
import team.info.ncmfm.interfaces.IProxy;

public class ClientProxy implements IProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new ItemRegistryHandler());
        MinecraftForge.EVENT_BUS.register(new RenderGuiHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerActionHandler());
    }
    @Override
    public void init(FMLInitializationEvent event)
    {

    }
    @Override
    public void postInit(FMLPostInitializationEvent event)
    {

    }
}
