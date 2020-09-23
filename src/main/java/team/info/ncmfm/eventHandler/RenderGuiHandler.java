package team.info.ncmfm.eventHandler;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.info.ncmfm.ui.Pannel;

public class RenderGuiHandler {

    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }
        //new Pannel(Minecraft.getMinecraft());
    }
}
