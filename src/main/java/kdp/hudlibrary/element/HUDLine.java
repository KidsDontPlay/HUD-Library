package kdp.hudlibrary.element;

import java.awt.*;

import net.minecraftforge.fml.client.config.GuiUtils;

public class HUDLine extends HUDElement {
    private int color = 0xFFCCCCCC;

    public HUDLine() {
    }

    public HUDLine(int color) {
        super();
        this.color = color;
    }

    @Override
    protected Dimension dimension(int maxWidth) {
        return new Dimension(maxWidth, 1);
    }

    @Override
    public void draw(int maxWidth) {
        GuiUtils.drawGradientRect(0, 0, 0, maxWidth, 1, color, color);
    }
}
