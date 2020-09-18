package team.info.ncmfm;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;

@net.minecraftforge.fml.common.Mod(modid = NcmMod.MODID, name = NcmMod.NAME, version = NcmMod.VERSION)
public class NcmMod
{
    public static final String MODID = "ncmfm";
    public static final String NAME = "NeteaseCloudMusicMod";
    public static final String VERSION = "1.0";

    private static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        logger.info("system start");
    }

    @SubscribeEvent
    public void onPlayer(PlayerInteractEvent.RightClickItem event){
       EntityPlayer player= event.getEntityPlayer();

    }
}
