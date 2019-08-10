package kdp.hudlibrary.tehud.element;

import java.awt.*;

import net.minecraft.nbt.DoubleNBT;
import net.minecraftforge.fml.client.config.GuiUtils;

public class HUDProgressBar extends HUDElement<DoubleNBT> {

    private final int width, height, frameColor;
    private final int c1, c2, background;
    private double filling;

    public HUDProgressBar(int width, int height, int frameColor, int color) {
        super();
        this.width = width;
        this.height = height;
        this.frameColor = frameColor;
        Color c = new Color(color, true);
        Color background = c.darker();
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        boolean swap;
        if (swap = hsb[2] > .5f)
            hsb[2] += -.2f;
        else
            hsb[2] += .2f;
        int c2 = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        int c1 = color;
        if (!swap) {
            int tmp = c1;
            c1 = c2;
            c2 = tmp;
        }
        this.c1 = c1;
        this.c2 = c2;
        this.background = background.getRGB();
    }

    public HUDProgressBar setFilling(double filling) {
        this.filling = filling;
        return this;
    }

    @Override
    protected Dimension dimension(int maxWidth) {
        return new Dimension(Math.min(width < 0 ? maxWidth : width, maxWidth), height);
    }

    @Override
    public HUDElement read(DoubleNBT tag) {
        if (reader != null) {
            reader.accept(this, tag);
            return this;
        }
        filling = tag.getDouble();
        return this;
    }

    @Override
    public void draw(int maxWidth) {
        int w = Math.min(this.width < 0 ? maxWidth : this.width, maxWidth);

        GuiUtils.drawGradientRect(0, 1, 1, w - 1, height - 1, background, background);
        int del = height - 2;
        int del1 = del / 2;
        GuiUtils.drawGradientRect(0, 1, 1, (int) ((w - 1) * filling), del1 + 1, c2, c1);
        GuiUtils.drawGradientRect(0, 1, del1 + 1, (int) ((w - 1) * filling), del + 1, c1, c2);
        //frame
        GuiUtils.drawGradientRect(0, 0, 0, 1, height, frameColor, frameColor);
        GuiUtils.drawGradientRect(0, w - 1, 0, w, height, frameColor, frameColor);
        GuiUtils.drawGradientRect(0, 1, 0, w - 1, 1, frameColor, frameColor);
        GuiUtils.drawGradientRect(0, 1, height - 1, w - 1, height, frameColor, frameColor);
    }

}
