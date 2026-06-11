package team.info.ncmfm.eventHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import team.info.ncmfm.NcmConfig;
import team.info.ncmfm.manager.MusicPlaybackManager;
import team.info.ncmfm.model.TrackContainer;

public class RenderGuiHandler {

    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }

        MusicPlaybackManager manager = MusicPlaybackManager.getInstance();
        if (!manager.isPlaying()) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();

        // 1. Draw "Now Playing" text in the top-left corner
        TrackContainer track = manager.getCurrentTrack();
        if (track != null) {
            String text = "正在播放: " + track.getName();
            int textWidth = mc.fontRenderer.getStringWidth(text);
            // Draw a clean dark background box for legibility (6px horizontal, 3px vertical padding)
            Gui.drawRect(2, 5, 14 + textWidth, 18, 0x88000000);
            mc.fontRenderer.drawStringWithShadow(text, 8, 8, 0xFFFFFF);
        }

        // 2. Draw lyrics centered at the top of the screen if enabled
        if (!NcmConfig.showLyrics) {
            return;
        }

        String lyric = manager.getCurrentLyricText();
        if (lyric == null || lyric.trim().length() == 0) {
            return;
        }

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
