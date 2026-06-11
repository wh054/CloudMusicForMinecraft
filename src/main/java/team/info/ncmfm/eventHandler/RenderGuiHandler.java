package team.info.ncmfm.eventHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.info.ncmfm.NcmConfig;
import team.info.ncmfm.manager.MusicPlaybackManager;

public class RenderGuiHandler {

    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        if (!NcmConfig.showLyrics) {
            return;
        }

        MusicPlaybackManager manager = MusicPlaybackManager.getInstance();
        if (!manager.isPlaying()) {
            return;
        }

        String lyric = manager.getCurrentLyricText();
        if (lyric == null || lyric.trim().length() == 0) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaled = event.getResolution();
        int width = scaled.getScaledWidth();
        int x = width / 2;
        int y = 8; // Offset from top of the screen

        int textWidth = mc.fontRenderer.getStringWidth(lyric);
        int rectLeft = x - textWidth / 2 - 6;
        int rectRight = x + textWidth / 2 + 6;

        // Draw a clean, rounded/padded dark background box behind lyrics for legibility
        Gui.drawRect(rectLeft, y - 3, rectRight, y + 10, 0x88000000);

        // Draw centered lyric text with shadow
        mc.fontRenderer.drawStringWithShadow(lyric, x - textWidth / 2, y, 0xFFFFFF);
    }
}
