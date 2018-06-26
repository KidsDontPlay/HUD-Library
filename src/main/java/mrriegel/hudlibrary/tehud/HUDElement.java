package mrriegel.hudlibrary.tehud;

import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mrriegel.hudlibrary.tehud.IHUDProvider.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.TextTable.Alignment;
import net.minecraftforge.fml.client.config.GuiUtils;

public abstract class HUDElement {

	Function<TileEntity, NBTTagCompound> writer = t -> new NBTTagCompound();
	BiConsumer<HUDElement, NBTTagCompound> reader = (h, n) -> {
	};

	protected @Nonnull Alignment align = Alignment.LEFT;
	protected Int2IntMap padding = new Int2IntOpenHashMap(4);
	protected Int2ObjectMap<Dimension> dims = new Int2ObjectOpenHashMap<>();

	protected HUDElement() {
		padding.defaultReturnValue(1);
	}

	public Alignment getAlignment() {
		return Alignment.LEFT;
	}

	public HUDElement setAlignment(Alignment align) {
		this.align = align;
		return this;
	}

	public int getPadding(Direction dir) {
		return padding.get(dir.ordinal());
	}

	public final int getPaddingHorizontal() {
		return getPadding(Direction.LEFT) + getPadding(Direction.RIGHT);
	}

	public final int getPaddingVertical() {
		return getPadding(Direction.UP) + getPadding(Direction.DOWN);
	}

	public HUDElement setPadding(Direction dir, int padding) {
		this.padding.put(dir.ordinal(), padding);
		return this;
	}

	public HUDElement setPadding(int padding) {
		for (int i = 0; i < 4; i++)
			this.padding.put(i, padding);
		return this;
	}

	/** @return Dimension without padding */

	@Nonnull
	public abstract Dimension dimension(int maxWidth);

	public NBTTagCompound writeSyncTag(TileEntity tile) {
		return writer.apply(tile);
	}

	public void readSyncTag(NBTTagCompound tag) {
		reader.accept(this, tag);
	}

	public abstract void draw(int maxWidth);

	public static class HUDCompound extends HUDElement {
		protected final HUDElement[] elements;
		protected final boolean lineBreak;

		public HUDCompound(boolean lineBreak, HUDElement... elements) {
			super();
			this.elements = elements;
			this.lineBreak = lineBreak;
			Validate.isTrue(elements != null && elements.length != 0);
		}

		public HUDCompound(boolean lineBreak, Collection<HUDElement> lis) {
			this(lineBreak, lis.toArray(new HUDElement[lis.size()]));
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
					totalHeight += l.stream().mapToInt(e -> e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).height + e.getPadding(Direction.LEFT) + e.getPadding(Direction.RIGHT)).max().getAsInt();
				}
				d = new Dimension(totalWidth, totalHeight);
			} else {
				int part = maxWidth / elements.length;
				if (!true)
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
				if (!true)
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

		@Override
		public int getPadding(Direction dir) {
			return super.getPadding(dir);
		}
	}

	public static class HUDText extends HUDElement {
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

	public static class HUDBar extends HUDElement {

		private final int width, height, frameColor, color;
		private double filling;

		public HUDBar(int width, int height, int frameColor, int color) {
			super();
			this.width = width;
			this.height = height;
			this.frameColor = frameColor;
			this.color = color;
			filling = .45;
			//			filling = Math.sin((Minecraft.getMinecraft().player.ticksExisted + Minecraft.getMinecraft().getRenderPartialTicks()) / 10.) / 2 + .5;
			//			System.out.println(filling);
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

		//		@Override
		//		public NBTTagCompound writeSyncTag(TileEntity tile) {
		//			NBTTagCompound n = new NBTTagCompound();
		//			if (tile instanceof TileEntityFurnace) {
		//				TileEntityFurnace t = (TileEntityFurnace) tile;
		//				n.setDouble("fill", t.getField(0) / (double) t.getField(1));
		//			}
		//			return n;
		//		}
		//
		//		@Override
		//		public void readSyncTag(NBTTagCompound tag) {
		//			filling = tag.getDouble("fill");
		//		}

		@Override
		public void draw(int maxWidth) {
			Color c = new Color(color);
			Color background = c.darker();
			float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
			hsb[2] += -.1f;
			int c2 = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);

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

	public static class HUDStack extends HUDElement {
		private static final Dimension dim = new Dimension(16, 16);
		private ItemStack stack;
		private boolean overlay = true;

		public HUDStack(ItemStack stack) {
			super();
			this.stack = stack;
		}

		@Override
		public Dimension dimension(int maxWidth) {
			return dim;
		}

		@Override
		public NBTTagCompound writeSyncTag(TileEntity tile) {
			return super.writeSyncTag(tile);
		}

		@Override
		public void readSyncTag(NBTTagCompound tag) {
		}

		@Override
		public void draw(int maxWidth) {
			//			RenderHelper.enableStandardItemLighting();
			RenderHelper.enableGUIStandardItemLighting();
			int s = 1000;
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			GlStateManager.depthMask(true);
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			GlStateManager.pushMatrix();
			int tr = 0;
			GlStateManager.translate(0, 0, tr);
			GlStateManager.scale(1, 1, 1. / s);
			RenderItem render = Minecraft.getMinecraft().getRenderItem();
			render.renderItemAndEffectIntoGUI(stack, 0, 0);
			//			render.renderItem(stack, TransformType.GUI);
			if (overlay)
				render.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, stack, 0, 0, null);
			GlStateManager.scale(1, 1, s);
			GlStateManager.translate(0, 0, -tr);
			GlStateManager.popMatrix();
			GlStateManager.disableLighting();
			RenderHelper.disableStandardItemLighting();
		}

	}

	public static class HUDLine extends HUDElement {
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

}
