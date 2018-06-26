package mrriegel.hudlibrary.tehud.element;

import java.awt.Dimension;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class HUDText extends HUDElement {
	private String text;
	private boolean shadow, unicode;
	private final boolean lineBreak;
	private final FontRenderer fr;
	private int color = 0xFFCCCCCC;

	public HUDText(String text, boolean lineBreak) {
		super();
		this.text = text;
		this.lineBreak = lineBreak;
		this.fr = Minecraft.getMinecraft().fontRenderer;
		//			this.color = 0xFF443322;
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
