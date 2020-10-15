package team.info.ncmfm;

import javazoom.jl.player.Player;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.Logger;
import team.info.ncmfm.audio.Mp3Player;
import team.info.ncmfm.interfaces.IProxy;
import team.info.ncmfm.net.MusicMessage;
import team.info.ncmfm.net.MusicMessageClientHandler;
import team.info.ncmfm.net.MusicMessageHandler;
import team.info.ncmfm.net.MusicPacketHandler;


@net.minecraftforge.fml.common.Mod(modid = NcmMod.MODID, name = NcmMod.NAME, version = NcmMod.VERSION)
public class NcmMod
{
    public static final String MODID = "ncmfm";
    public static final String NAME = "NeteaseCloudMusicMod";
    public static final String VERSION = "1.0";

    @Mod.Instance(MODID)
    public static NcmMod INSTANCE;

    @SidedProxy(serverSide = "team.info.ncmfm.proxy.ServerProxy",
            clientSide = "team.info.ncmfm.proxy.ClientProxy")
    public static IProxy proxy;

    private static Logger logger;

    public static Player mp3Player;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        MusicPacketHandler.INSTANCE.registerMessage(MusicMessageHandler.class, MusicMessage.class, 223, Side.SERVER);
        MusicPacketHandler.INSTANCE.registerMessage(MusicMessageClientHandler.class, MusicMessage.class, 224, Side.CLIENT);
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postinit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
