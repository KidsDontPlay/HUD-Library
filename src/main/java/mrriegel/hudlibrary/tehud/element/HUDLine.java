package mrriegel.hudlibrary.tehud.element;

import java.awt.Dimension;

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
	public Dimension dimension(int maxWidth) {
		Dimension d = dims.get(maxWidth);
		if (d != null)
			return d;
		d = new Dimension(maxWidth, 1);
		dims.put(maxWidth, d);
		return d;
	}

	@Override
	public void draw(int maxWidth) {
		GuiUtils.drawGradientRect(0, 0, 0, maxWidth, 1, color, color);
	}
}
