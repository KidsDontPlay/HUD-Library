package mrriegel.hudlibrary.tehud;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;

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
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.TextTable.Alignment;
import net.minecraftforge.fml.client.config.GuiUtils;

public abstract class HUDElement {

	Function<TileEntity, NBTTagCompound> writer = t -> new NBTTagCompound();
	BiConsumer<HUDElement, NBTTagCompound> reader = (h, n) -> {
	};

	protected @Nonnull Alignment align = Alignment.LEFT;
	protected Int2IntMap padding = new Int2IntOpenHashMap(4);
	protected Int2ObjectMap<Dimension> dims = new Int2ObjectOpenHashMap<>();
	protected boolean newLine = true;

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

	public HUDElement setPadding(Direction dir, int padding) {
		this.padding.put(dir.ordinal(), padding);
		return this;
	}

	public HUDElement setPadding(int padding) {
		for (int i = 0; i < 4; i++)
			this.padding.put(i, padding);
		return this;
	}

	public boolean isNewLine() {
		return newLine;
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

		public HUDCompound(HUDElement... elements) {
			super();
			this.elements = elements;
			this.lineBreak = !false;
			Validate.isTrue(elements != null && elements.length != 0);
		}

		public HUDCompound(Collection<HUDElement> lis) {
			this(lis.toArray(new HUDElement[lis.size()]));
		}

		@Override
		public Dimension dimension(int maxWidth) {
			Dimension d = dims.get(maxWidth);
			if (d != null)
				return d;
			if (lineBreak) {
				HUDElement[][] grid = new HUDElement[elements.length][elements.length];
				int rowWidth = 0;
				int row = 0, column = 0;
				for (HUDElement e : elements) {
					rowWidth += e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width/* + e.getPadding(Direction.LEFT) + e.getPadding(Direction.RIGHT)*/;
					if (rowWidth > maxWidth) {
						row++;
						rowWidth = 0;
						column = 0;
					}
					grid[column][row] = e;
					column++;
				}
				int totalWidth = 0, totalHeight = 0;
				for (int x = 0; x < elements.length; x++) {
					totalHeight = Math.max(totalHeight, Arrays.stream(grid[x]).filter(Objects::nonNull).mapToInt(e -> e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).height).sum());
				}
				HUDElement[][] gridNew = new HUDElement[elements.length][elements.length];
				for (int i = 0; i < elements.length; i++)
					for (int j = 0; j < elements.length; j++)
						gridNew[i][j] = grid[j][i];
				for (int x = 0; x < elements.length; x++) {
					totalWidth = Math.max(totalWidth, Arrays.stream(gridNew[x]).filter(Objects::nonNull).mapToInt(e -> e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width).sum());
				}
				d = new Dimension(totalWidth, totalHeight);
			} else {
				int part = maxWidth / elements.length;
				if (true)
					part = maxWidth;
				int totalWidth = 0, totalHeight = 0;
				for (HUDElement e : elements) {
					totalWidth += e.dimension(part - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width + e.getPadding(Direction.LEFT) + e.getPadding(Direction.RIGHT);
					totalHeight = Math.max(totalHeight, e.dimension(part - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).height /*+ e.getPadding(Direction.UP) + e.getPadding(Direction.DOWN)*/);
				}
				totalWidth -= elements[0].getPadding(Direction.LEFT);
				totalWidth -= elements[elements.length - 1].getPadding(Direction.RIGHT);
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
				HUDElement[][] grid = new HUDElement[elements.length][elements.length];
				int rowWidth = 0;
				int row = 0, column = 0;
				for (HUDElement e : elements) {
					rowWidth += e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width;
					if (rowWidth > maxWidth) {
						row++;
						rowWidth = 0;
						column = 0;
					}
					grid[column][row] = e;
					column++;
				}
				HUDElement[][] gridNew = new HUDElement[elements.length][elements.length];
				for (int i = 0; i < elements.length; i++)
					for (int j = 0; j < elements.length; j++)
						gridNew[i][j] = grid[j][i];
				int last = 0;
				for (int x = 0; x < elements.length; x++) {
					int back = 0;
					for (int y = 0; y < elements.length; y++) {
						GlStateManager.depthMask(false);
						HUDElement e = grid[y][x];
						if (e == null)
							continue;
						e.draw(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT));
						int w = e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width + e.getPadding(Direction.LEFT) + e.getPadding(Direction.RIGHT);
						back += w;
						GlStateManager.translate(w, 0, 0);
					}
					GlStateManager.translate(-back, 0, 0);
					OptionalInt op = Arrays.stream(gridNew[x]).filter(Objects::nonNull).mapToInt(e -> e.dimension(maxWidth - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).height).max();
					//					if (op.isPresent())
					//						System.out.println(op.getAsInt() + "");
					if (op.isPresent())
						GlStateManager.translate(0, last = op.getAsInt(), 0);
				}
				GlStateManager.translate(0, -last, 0);
				//				System.out.println("zip");
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
				int back = 0;
				for (HUDElement e : elements) {
					GlStateManager.depthMask(false);
					e.draw(part - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT));
					int w = e.dimension(part - e.getPadding(Direction.LEFT) - e.getPadding(Direction.RIGHT)).width + e.getPadding(Direction.LEFT) + e.getPadding(Direction.RIGHT);
					back += w;
					GlStateManager.translate(w, 0, 0);
				}
				GlStateManager.translate(-back, 0, 0);
				if (tooLong) {
					GlStateManager.scale(1 / fac, 1 / fac, 1);
				}
			}
		}

		@Override
		public int getPadding(Direction dir) {
			if (elements != null)
				return super.getPadding(dir);
			if (dir == Direction.LEFT)
				return elements[0].getPadding(dir);
			if (dir == Direction.RIGHT)
				return elements[elements.length - 1].getPadding(dir);
			return Arrays.stream(elements).mapToInt(e -> e.getPadding(dir)).max().getAsInt();
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

		@Override
		public NBTTagCompound writeSyncTag(TileEntity tile) {
			NBTTagCompound n = new NBTTagCompound();
			if (tile instanceof TileEntityFurnace) {
				TileEntityFurnace t = (TileEntityFurnace) tile;
				n.setDouble("fill", t.getField(0) / (double) t.getField(1));
			}
			return n;
		}

		@Override
		public void readSyncTag(NBTTagCompound tag) {
			filling = tag.getDouble("fill");
		}

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
