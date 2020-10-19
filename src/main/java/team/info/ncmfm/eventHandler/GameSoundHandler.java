package team.info.ncmfm.eventHandler;

import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.info.ncmfm.NcmMod;

public class GameSoundHandler {
    @SubscribeEvent
    public void onGameSoundPlay(PlaySoundEvent event){
        if(event.getName().startsWith("music")){
            if(NcmMod.soundSystem!=null){
                if(NcmMod.soundSystem.playing("custom.mp3")){
                    event.setResultSound(null);
                    System.out.println("Stop Game Music "+event.getName());
                }
            }
        }
    }
}
