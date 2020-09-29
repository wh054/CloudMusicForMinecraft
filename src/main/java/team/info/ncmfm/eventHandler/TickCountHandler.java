package team.info.ncmfm.eventHandler;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber
public class  TickCountHandler {

    private static long tickCount=0;
    private static boolean state=false;

    @SubscribeEvent
    public static void countTick(TickEvent event){
        if(TickCountHandler.state){
            TickCountHandler.tickCount++;
        }
    }

    public static void reset(){
        TickCountHandler.state=false;
        TickCountHandler.tickCount=0;
    }
    public static boolean getState(){
        return TickCountHandler.state;
    }
    public static void startCount(){
        TickCountHandler.state=true;
    }

    public static long getTickCount(){
        return TickCountHandler.tickCount;
    }

    public static void stop(){
        TickCountHandler.state=false;
    }
}
