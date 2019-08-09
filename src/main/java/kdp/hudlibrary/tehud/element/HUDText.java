package kdp.hudlibrary.tehud.element;

import java.awt.*;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.nbt.StringNBT;

public class HUDText extends HUDElement<StringNBT> {
    private String text;
    private boolean shadow, unicode;
    private final boolean lineBreak;
    private final FontRenderer fr = Minecraft.getInstance().fontRenderer;
    private int color = 0xFFCCCCCC;

    public HUDText(String text, boolean lineBreak) {
        super();
        this.text = text;
        this.lineBreak = lineBreak;
    }

    public HUDText setText(String text) {
        this.text = text;
        return this;
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
    public HUDElement read(StringNBT tag) {
        if (reader != null) {
            reader.accept(this, tag);
            return this;
        }
        text = tag.getString();
        return this;
    }

    //@Override
    public StringNBT write() {
        return new StringNBT(text);
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
