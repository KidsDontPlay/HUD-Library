package mrriegel.hudlibrary.tehud.element;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;

import mrriegel.hudlibrary.tehud.IHUDProvider.Direction;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class HUDCompound extends HUDElement {
	protected final HUDElement[] elements;
	protected final boolean lineBreak;

	public HUDCompound(boolean lineBreak, HUDElement... elements) {
		super();
		this.elements = elements;
		this.lineBreak = lineBreak;
		Validate.isTrue(elements != null && elements.length != 0);
	}

	public HUDCompound(boolean lineBreak, Collection<? extends HUDElement> lis) {
		this(lineBreak, lis.toArray(new HUDElement[lis.size()]));
	}

	public HUDElement[] getElements() {
		return elements;
	}

	@Override
	public void readSyncTag(NBTTagCompound tag) {
		if (reader != null) {
			reader.accept(this, tag);
			return;
		}
		if (tag.hasKey("elements")) {
			NBTTagList list = tag.getTagList("elements", 10);
			//			Validate.isTrue(elements.length == list.tagCount());
			int size = Math.min(elements.length, list.tagCount());
			for (int i = 0; i < size; i++) {
				HUDElement h = elements[i];
				NBTTagCompound nbt = list.getCompoundTagAt(i);
				//				System.out.println(nbt);
				h.readSyncTag(nbt);
			}
		}
	}

	private List<List<HUDElement>> getElementRows(int maxWidth) {
		List<List<HUDElement>> lines = new ArrayList<>();
		List<HUDElement> line = new ArrayList<>();
		List<HUDElement> ls = Lists.newArrayList(elements);
		while (!ls.isEmpty()) {
			HUDElement el = ls.remove(0);
			int width = el.dimension(maxWidth - el.getPadding(Direction.LEFT) - el.getPadding(Direction.RIGHT)).width + el.getPadding(Direction.LEFT) + el.getPadding(Direction.RIGHT);
			if (maxWidth < line.stream().mapToInt(e -> e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width + e.getPadding(Direction.LEFT) + e.getPadding(Direction.RIGHT)).sum() + width) {
				lines.add(new ArrayList<>(line));
				line.clear();
				line.add(el);
			} else
				line.add(el);
		}
		if (!line.isEmpty())
			lines.add(new ArrayList<>(line));
		lines.removeIf(List::isEmpty);
		return lines;
	}

	@Override
	public Dimension dimension(int maxWidth) {
		Dimension d = dims.get(maxWidth);
		if (d != null)
			return d;
		if (lineBreak) {
			int totalWidth = 0, totalHeight = 0;
			for (List<HUDElement> l : getElementRows(maxWidth)) {
				totalWidth = Math.max(totalWidth, l.stream().mapToInt(e -> e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width + e.getPadding(Direction.LEFT) + e.getPadding(Direction.RIGHT)).sum());
				totalHeight += l.stream().mapToInt(e -> e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).height + e.getPadding(Direction.UP) + e.getPadding(Direction.DOWN)).max().getAsInt();
			}
			d = new Dimension(totalWidth, totalHeight);
		} else {
			int part = maxWidth / elements.length;
			if (true)
				part = maxWidth;
			int totalWidth = 0, totalHeight = 0;
			for (HUDElement e : elements) {
				totalWidth += e.dimension(part - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width + e.getPadding(Direction.LEFT) + e.getPadding(Direction.RIGHT);
				totalHeight = Math.max(totalHeight, e.dimension(part - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).height + e.getPadding(Direction.UP) + e.getPadding(Direction.DOWN));
			}
			//				totalWidth -= elements[0].getPadding(Direction.LEFT);
			//				totalWidth -= elements[elements.length - 1].getPadding(Direction.RIGHT);
			int width = totalWidth;
			boolean tooLong = width > maxWidth;
			if (tooLong) {
				double fac = maxWidth / (double) width;
				totalHeight *= fac;
			}
			d = new Dimension(totalWidth, totalHeight);
			//				if (new Random().nextDouble() < .01)
			//					System.out.println(elements[0].getClass() + " " + d);
		}
		dims.put(maxWidth, d);
		return d;
	}

	@Override
	public void draw(int maxWidth) {
		if (lineBreak) {
			int hei = 0;
			boolean firstH = true;
			for (List<HUDElement> l : getElementRows(maxWidth)) {
				int down = 0;
				if (firstH) {
					firstH = false;
					GlStateManager.translate(0, down = l.stream().mapToInt(e -> e.getPadding(Direction.UP)).max().getAsInt(), 0);
				}
				int back = 0;
				boolean firstW = true;
				for (HUDElement e : l) {
					GlStateManager.depthMask(false);
					if (firstW) {
						firstW = false;
						int pad = e.getPadding(Direction.LEFT);
						back += pad;
						GlStateManager.translate(pad, 0, 0);
					}
					int w = e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width + e.getPadding(Direction.LEFT) + e.getPadding(Direction.RIGHT);
					back += w;
					e.draw(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT));
					GlStateManager.translate(w, 0, 0);
				}
				int h = l.stream().mapToInt(e -> e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).height + e.getPadding(Direction.UP) + e.getPadding(Direction.DOWN)).max().getAsInt();
				hei += h;
				GlStateManager.translate(0, h - down, 0);
				GlStateManager.translate(-back, 0, 0);
			}
			GlStateManager.translate(0, -hei, 0);
		} else {
			int width = dimension(maxWidth).width;
			boolean tooLong = width > maxWidth;
			double fac = maxWidth / (double) width;
			if (tooLong) {
				GlStateManager.scale(fac, fac, 1);
			}
			int part = maxWidth / elements.length;
			if (true)
				part = maxWidth;
			int down = 0;
			GlStateManager.translate(0, down = Arrays.stream(elements).mapToInt(e -> e.getPadding(Direction.UP)).max().getAsInt(), 0);
			int back = 0;
			boolean first = true;
			for (HUDElement e : elements) {
				GlStateManager.depthMask(false);
				if (first) {
					first = false;
					int pad = e.getPadding(Direction.LEFT);
					back += pad;
					GlStateManager.translate(pad, 0, 0);
				}
				e.draw(part - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT));
				int w = e.dimension(part - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width + e.getPadding(Direction.LEFT) + e.getPadding(Direction.RIGHT);
				back += w;

				GlStateManager.translate(w, 0, 0);
			}
			GlStateManager.translate(0, -down, 0);
			GlStateManager.translate(-back, 0, 0);
			if (tooLong) {
				GlStateManager.scale(1 / fac, 1 / fac, 1);
			}
		}
	}

}