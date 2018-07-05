package mrriegel.hudlibrary.tehud.element;

import java.awt.Dimension;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;

public class HUDText extends HUDElement {
	private String text;
	private boolean shadow, unicode;
	private final boolean lineBreak;
	private final FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
	private int color = 0xFFCCCCCC;

	public HUDText(String text, boolean lineBreak) {
		super();
		this.text = text;
		this.lineBreak = lineBreak;
	}

	public String getText() {
		return text;
	}

	public HUDText setText(String text) {
		this.text = text;
		return this;
	}

	public boolean isShadow() {
		return shadow;
	}

	public HUDText setShadow(boolean shadow) {
		this.shadow = shadow;
		return this;
	}

	public boolean isUnicode() {
		return unicode;
	}

	public HUDText setUnicode(boolean unicode) {
		this.unicode = unicode;
		return this;
	}

	public int getColor() {
		return color;
	}

	public HUDText setColor(int color) {
		this.color = color;
		return this;
	}

	@Override
	public void readSyncTag(NBTTagCompound tag) {
		if (reader != null) {
			reader.accept(this, tag);
			return;
		}
		if (tag.hasKey("text"))
			text = tag.getString("text");
	}

	@Override
	public Dimension dimension(int maxWidth) {
		Dimension d = dims.get(maxWidth);
		if (d != null)
			return d;
		if (lineBreak) {
			List<String> lis = fr.listFormattedStringToWidth(text, maxWidth);
			d = new Dimension(lis.stream().mapToInt(s -> fr.getStringWidth(s)).max().getAsInt(), lis.size() * (fr.FONT_HEIGHT));
		} else {
			int width = fr.getStringWidth(text);
			boolean tooLong = width > maxWidth;
			double fac = maxWidth / (double) width;
			d = new Dimension(Math.min(width, maxWidth), (int) ((fr.FONT_HEIGHT) * (tooLong ? fac : 1)));
		}
		dims.put(maxWidth, d);
		return d;
	}

	@Override
	public void draw(int maxWidth) {
		boolean uni = fr.getUnicodeFlag();
		fr.setUnicodeFlag(unicode);
		if (lineBreak) {
			List<String> lis = fr.listFormattedStringToWidth(text, maxWidth);
			for (int i = 0; i < lis.size(); i++) {
				String s = lis.get(i);
				fr.drawString(s, 0, (i * (fr.FONT_HEIGHT)), color, shadow);
			}
		} else {
			int width = fr.getStringWidth(text);
			boolean tooLong = width > maxWidth;
			double fac = maxWidth / (double) width;
			if (tooLong) {
				GlStateManager.scale(fac, fac, 1);
			}
			fr.drawString(text, 0, 0, color, shadow);
			if (tooLong) {
				GlStateManager.scale(1 / fac, 1 / fac, 1);
			}
		}
		fr.setUnicodeFlag(uni);
	}

}
