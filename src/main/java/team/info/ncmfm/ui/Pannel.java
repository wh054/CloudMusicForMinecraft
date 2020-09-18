package team.info.ncmfm.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;

public class Pannel extends Gui {
    public Pannel(Minecraft mc)
    {
        ScaledResolution scaled = new ScaledResolution(mc);
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();

        drawCenteredString(mc.fontRenderer, "ÄãÂèµÄ£¬ÎªÊ²Ã´", width / 2, (height / 2) - 4, Integer.parseInt("FFAA00", 16));
    }
}
