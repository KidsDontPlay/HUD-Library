package kdp.hudlibrary.element;

import java.awt.*;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.GuiUtils;

public class HUDProgressBar extends HUDElement {

    private final int width, height, frameColor;
    private final int startBright, startDark, endBright, endDark, background;
    private double filling;
    private final boolean oneColor;

    public HUDProgressBar(int width, int height, int frameColor, int startColor, int endColor) {
        super();
        this.width = width;
        this.height = height;
        this.frameColor = frameColor;
        Color fc = new Color(frameColor, true);
        Color bg = fc.brighter();
        if (bg.getRGB() == fc.getRGB()) {
            bg = fc.darker();
        }
        this.background = bg.getRGB();

        this.oneColor = startColor == endColor;
        int[] sc = splitColor(startColor);
        this.startBright = sc[0];
        this.startDark = sc[1];
        if (!this.oneColor) {
            int[] ec = splitColor(endColor);
            this.endBright = ec[0];
            this.endDark = ec[1];
        } else {
            this.endBright = this.endDark = 0;
        }
    }

    public HUDProgressBar(int width, int height, int frameColor, int color) {
        this(width, height, frameColor, color, color);
    }

    private int[] splitColor(int color) {
        Color c = new Color(color, true);
        int alpha = c.getAlpha();
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        boolean swap;
        if (swap = hsb[2] > .5f) {
            hsb[2] += -.2f;
        } else {
            hsb[2] += .2f;
        }
        Color nc = new Color(Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]));
        int c1 = new Color(nc.getRed(), nc.getGreen(), nc.getBlue(), alpha).getRGB();
        int c2 = color;
        if (swap) {
            int tmp = c1;
            c1 = c2;
            c2 = tmp;
        }

        return new int[] { c1, c2 };
    }

    public HUDProgressBar setFilling(double filling) {
        this.filling = MathHelper.clamp(filling, 0., 1.);
        return this;
    }

    @Override
    protected Dimension dimension(int maxWidth) {
        return new Dimension(Math.min(width < 0 ? maxWidth : width, maxWidth), height);
    }

    @Override
    public void draw(int maxWidth) {
        int w = Math.min(this.width < 0 ? maxWidth : this.width, maxWidth);
        Color cs1 = new Color(startBright, true);
        Color ce1 = new Color(endBright, true);
        Color cs2 = new Color(startDark, true);
        Color ce2 = new Color(endDark, true);
        int bright = oneColor ?
                startBright :
                new Color((int) (cs1.getRed() + (ce1.getRed() - cs1.getRed()) * filling),
                        (int) (cs1.getGreen() + (ce1.getGreen() - cs1.getGreen()) * filling),
                        (int) (cs1.getBlue() + (ce1.getBlue() - cs1.getBlue()) * filling),
                        (int) (cs1.getAlpha() + (ce1.getAlpha() - cs1.getAlpha()) * filling)).getRGB();
        int dark = oneColor ?
                startDark :
                new Color((int) (cs2.getRed() + (ce2.getRed() - cs2.getRed()) * filling),
                        (int) (cs2.getGreen() + (ce2.getGreen() - cs2.getGreen()) * filling),
                        (int) (cs2.getBlue() + (ce2.getBlue() - cs2.getBlue()) * filling),
                        (int) (cs2.getAlpha() + (ce2.getAlpha() - cs2.getAlpha()) * filling)).getRGB();

        GuiUtils.drawGradientRect(0, 1, 1, w - 1, height - 1, background, background);
        int del = height - 2;
        int del1 = del / 2;
        //GuiUtils.drawGradientRect(0, 1, 1, (int) ((w - 1) * filling), del1 + 1, c2s, c1s);
        //GuiUtils.drawGradientRect(0, 1, del1 + 1, (int) ((w - 1) * filling), del + 1, c1s, c2s);
        GuiUtils.drawGradientRect(0, 1, 1, (int) ((w - 1) * filling), del1 + 1, dark, bright);
        GuiUtils.drawGradientRect(0, 1, del1 + 1, (int) ((w - 1) * filling), del + 1, bright, dark);
        //frame
        GuiUtils.drawGradientRect(0, 0, 0, 1, height, frameColor, frameColor);
        GuiUtils.drawGradientRect(0, w - 1, 0, w, height, frameColor, frameColor);
        GuiUtils.drawGradientRect(0, 1, 0, w - 1, 1, frameColor, frameColor);
        GuiUtils.drawGradientRect(0, 1, height - 1, w - 1, height, frameColor, frameColor);
    }

}
