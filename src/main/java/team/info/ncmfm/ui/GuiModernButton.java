package team.info.ncmfm.ui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

public class GuiModernButton extends GuiButton {
    private int bgColor;
    private int hoverBgColor;
    private int textColor;
    private int hoverTextColor;
    private int borderColor;

    public GuiModernButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.bgColor = 0x1A222D;
        this.hoverBgColor = 0x2A3544;
        this.textColor = 0xCCCCCC;
        this.hoverTextColor = 0xFF4B4B; // NetEase Red
        this.borderColor = 0x2A3544;
    }

    public GuiModernButton(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, int bgColor, int hoverBgColor, int hoverTextColor) {
        super(buttonId, x, y, widthIn, heightIn, buttonText);
        this.bgColor = bgColor;
        this.hoverBgColor = hoverBgColor;
        this.textColor = 0xCCCCCC;
        this.hoverTextColor = hoverTextColor;
        this.borderColor = 0x2A3544;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            FontRenderer fontrenderer = mc.fontRenderer;
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int bg = this.hovered ? this.hoverBgColor : this.bgColor;
            int textCol = this.hovered ? this.hoverTextColor : this.textColor;

            if (!this.enabled) {
                textCol = 0x555555;
                bg = 0x10151C;
            }

            // Draw flat background
            drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000 | bg);

            // Draw border
            drawHorizontalLine(this.x, this.x + this.width - 1, this.y, 0xFF000000 | this.borderColor);
            drawHorizontalLine(this.x, this.x + this.width - 1, this.y + this.height - 1, 0xFF000000 | this.borderColor);
            drawVerticalLine(this.x, this.y, this.y + this.height - 1, 0xFF000000 | this.borderColor);
            drawVerticalLine(this.x + this.width - 1, this.y, this.y + this.height - 1, 0xFF000000 | this.borderColor);

            this.mouseDragged(mc, mouseX, mouseY);
            this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, textCol);
        }
    }
}
