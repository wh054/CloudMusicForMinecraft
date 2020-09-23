package team.info.ncmfm;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;
import team.info.ncmfm.proxy.CommonProxy;


@net.minecraftforge.fml.common.Mod(modid = NcmMod.MODID, name = NcmMod.NAME, version = NcmMod.VERSION)
public class NcmMod
{
    public static final String MODID = "ncmfm";
    public static final String NAME = "NeteaseCloudMusicMod";
    public static final String VERSION = "1.0";

    @Mod.Instance(MODID)
    public static NcmMod INSTANCE;

    @SidedProxy(serverSide = "team.info.ncmfm.proxy.CommonProxy",
            clientSide = "team.info.ncmfm.proxy.ClientProxy")
    public static CommonProxy proxy;

    private static Logger logger;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
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
