package team.info.ncmfm.proxy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import team.info.ncmfm.audio.CodecMonoMp3;
import team.info.ncmfm.audio.CodecStereoMp3;
import team.info.ncmfm.eventHandler.*;
import team.info.ncmfm.interfaces.IProxy;
import java.lang.reflect.Field;

public class ClientProxy implements IProxy {
    private static final Logger logger = LogManager.getLogger(ClientProxy.class);
    public static SoundSystem soundSystem;

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(new BlockRegistryHandler());
        MinecraftForge.EVENT_BUS.register(new ItemRegistryHandler());
        MinecraftForge.EVENT_BUS.register(new RenderGuiHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerActionHandler());
        MinecraftForge.EVENT_BUS.register(new GameSoundHandler());

        try{
            SoundSystemConfig.setCodec("MonoMp3", CodecMonoMp3.class);
            SoundSystemConfig.setCodec("StereoMp3", CodecStereoMp3.class);
        }catch (SoundSystemException ex){
            logger.error(ex.getMessage());
        }
    }
    @Override
    public void init(FMLInitializationEvent event)
    {

    }
    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        getSoundSystem();
    }

    private void getSoundSystem(){
        try{
            SoundHandler soundHandler= Minecraft.getMinecraft().getSoundHandler();
            Field field = ObfuscationReflectionHelper.findField(SoundHandler.class,"field_147694_f");
            field.setAccessible(true);

            Field field2=ObfuscationReflectionHelper.findField(SoundManager.class,"field_148620_e");
            field2.setAccessible(true);

            SoundManager soundManager= (SoundManager)field.get(soundHandler);
            soundSystem =(SoundSystem)field2.get(soundManager);
        }catch (IllegalAccessException ex){
            logger.error(ex.getMessage());
        }
    }
}
