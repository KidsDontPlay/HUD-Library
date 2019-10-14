package kdp.hudlibrary.element;

import java.awt.*;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class HUDText extends HUDElement {
    private ITextComponent textComponent;
    private String text;
    private boolean shadow, unicode;
    private final boolean lineBreak;
    private final FontRenderer fr = Minecraft.getInstance().fontRenderer;
    private int color = 0xFFCCCCCC;

    public HUDText(ITextComponent textComponent, boolean lineBreak) {
        super();
        this.textComponent = textComponent;
        this.lineBreak = lineBreak;
        this.text = textComponent.getFormattedText();
    }

    public HUDText(String text, boolean lineBreak) {
        this(new StringTextComponent(text), lineBreak);
    }

    public HUDText setTextComponent(ITextComponent textComponent) {
        this.textComponent = textComponent;
        this.text = textComponent.getFormattedText();
        return this;
    }

    public HUDText setText(String text) {
        return setTextComponent(new StringTextComponent(text));
    }

    public HUDText setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    /*public HUDText setUnicode(boolean unicode) {
        this.unicode = unicode;
        return this;
    }*/

    public HUDText setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected Dimension dimension(int maxWidth) {
        if (text.isEmpty()) {
            return new Dimension();
        }
        Dimension d;
        boolean uni = fr.getBidiFlag();
        fr.setBidiFlag(unicode);
        if (lineBreak) {
            List<String> lis = fr.listFormattedStringToWidth(text, maxWidth);
            d = new Dimension(lis.stream().mapToInt(fr::getStringWidth).max().getAsInt(),
                    lis.size() * (fr.FONT_HEIGHT));
        } else {
            int width = fr.getStringWidth(text);
            boolean tooLong = width > maxWidth;
            double fac = maxWidth / (double) width;
            d = new Dimension(Math.min(width, maxWidth), (int) ((fr.FONT_HEIGHT) * (tooLong ? fac : 1)));
        }
        fr.setBidiFlag(uni);
        return d;
    }

    @Override
    public void draw(int maxWidth) {
        if (text.isEmpty()) {
            return;
        }
        boolean uni = fr.getBidiFlag();
        fr.setBidiFlag(unicode);
        if (lineBreak) {
            List<String> lis = fr.listFormattedStringToWidth(text, maxWidth);
            for (int i = 0; i < lis.size(); i++) {
                String s = lis.get(i);
                if (shadow)
                    fr.drawStringWithShadow(s, 0, (i * (fr.FONT_HEIGHT)), color);
                else
                    fr.drawString(s, 0, (i * (fr.FONT_HEIGHT)), color);
            }
        } else {
            int width = fr.getStringWidth(text);
            boolean tooLong = width > maxWidth;
            double fac = maxWidth / (double) width;
            if (tooLong) {
                GlStateManager.scaled(fac, fac, 1);
            }
            if (shadow)
                fr.drawStringWithShadow(text, 0, 0, color);
            else
                fr.drawString(text, 0, 0, color);
            if (tooLong) {
                GlStateManager.scaled(1 / fac, 1 / fac, 1);
            }
        }
        fr.setBidiFlag(uni);
    }

}
