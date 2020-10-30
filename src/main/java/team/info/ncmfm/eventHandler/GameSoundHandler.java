package team.info.ncmfm.eventHandler;

import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.info.ncmfm.proxy.ClientProxy;

public class GameSoundHandler {
    @SubscribeEvent
    public void onGameSoundPlay(PlaySoundEvent event){
        if(event.getName().startsWith("music")){
            if(ClientProxy.soundSystem!=null){
                if(ClientProxy.soundSystem.playing("background.StereoMp3")){
                    event.setResultSound(null);
                    System.out.println("Stop Game Music "+event.getName());
                }
            }
        }
    }
}
