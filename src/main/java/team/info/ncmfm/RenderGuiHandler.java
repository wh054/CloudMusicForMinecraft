package team.info.ncmfm;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.info.ncmfm.ui.Pannel;

@Mod.EventBusSubscriber
public class RenderGuiHandler {
    @SubscribeEvent
    public static void onRenderGui(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }
        new Pannel(Minecraft.getMinecraft());
    }
}
