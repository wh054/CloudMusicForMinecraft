package team.info.ncmfm.interfaces;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import team.info.ncmfm.eventHandler.RenderGuiHandler;

public interface IProxy {
     void preInit(FMLPreInitializationEvent event);

     void init(FMLInitializationEvent event);

     void postInit(FMLPostInitializationEvent event);
}
