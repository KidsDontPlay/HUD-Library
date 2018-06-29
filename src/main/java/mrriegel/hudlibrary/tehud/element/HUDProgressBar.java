package mrriegel.hudlibrary.tehud.element;

import java.awt.Color;
import java.awt.Dimension;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.client.config.GuiUtils;

public class HUDProgressBar extends HUDElement {

	private final int width, height, frameColor, color;
	private double filling;

	public HUDProgressBar(int width, int height, int frameColor, int color) {
		super();
		this.width = width;
		this.height = height;
		this.frameColor = frameColor;
		this.color = color;
	}

	public double getFilling() {
		return filling;
	}

	public void setFilling(double filling) {
		this.filling = filling;
	}

	@Override
	public Dimension dimension(int maxWidth) {
		Dimension d = dims.get(maxWidth);
		if (d != null)
			return d;
		d = new Dimension(width, height);
		dims.put(maxWidth, d);
		return d;
	}

	@Override
	public void readSyncTag(NBTTagCompound tag) {
		if (reader != null) {
			reader.accept(this, tag);
			return;
		}
		if (tag.hasKey("filling"))
			filling = tag.getDouble("filling");
	}

	@Override
	public void draw(int maxWidth) {
		Color c = new Color(color, true);
		Color background = c.darker();
		float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
		hsb[2] += -.1f;
		int c2 = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
		int width = this.width < 0 ? maxWidth : this.width;

		GuiUtils.drawGradientRect(0, 1, 0, width - 1, height - 2, background.getRGB(), background.getRGB());
		int del = height - 2;
		int del1 = del / 2;
		GuiUtils.drawGradientRect(0, 1, 0, (int) ((width - 1) * filling), del1, c2, color);
		GuiUtils.drawGradientRect(0, 1, del1, (int) ((width - 1) * filling), del, color, c2);
		//frame
		GuiUtils.drawGradientRect(0, 0, -1, 1, height - 1, frameColor, frameColor);
		GuiUtils.drawGradientRect(0, width - 1, -1, width, height - 1, frameColor, frameColor);
		GuiUtils.drawGradientRect(0, 1, -1, width - 1, 0, frameColor, frameColor);
		GuiUtils.drawGradientRect(0, 1, height - 2, width - 1, height - 1, frameColor, frameColor);
	}

}
