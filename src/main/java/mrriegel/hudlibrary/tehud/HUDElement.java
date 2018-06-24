package mrriegel.hudlibrary.tehud;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;

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

	public @Nonnull Alignment alignment() {
		return Alignment.LEFT;
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

	public int getPadding(Direction dir) {
		return 1;
	}

	public abstract void draw(int maxWidth);

	public static class HUDCompound extends HUDElement {
		private final HUDElement[] elements;

		public HUDCompound(HUDElement[] elements) {
			super();
			this.elements = elements;
			Validate.isTrue(elements != null && elements.length != 0);
		}

		@Override
		public Dimension dimension(int maxWidth) {
			int part = maxWidth / elements.length;
			return new Dimension(Arrays.stream(elements).mapToInt(e -> e.dimension(part).width).sum(), Arrays.stream(elements).mapToInt(e -> e.dimension(part).height).max().getAsInt());
		}

		@Override
		public void draw(int maxWidth) {
			int part = maxWidth / elements.length;
			int back = 0;
			for (HUDElement e : elements) {
				GlStateManager.depthMask(false);
				e.draw(part);
				int w = e.dimension(part).width;
				back += w;
				GlStateManager.translate(w, 0, 0);
			}
			GlStateManager.translate(-back, 0, 0);
		}

		@Override
		public int getPadding(Direction dir) {
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
		}

		@Override
		public Dimension dimension(int maxWidth) {
			if (lineBreak) {
				List<String> lis = fr.listFormattedStringToWidth(text, maxWidth);
				return new Dimension(lis.stream().mapToInt(s -> fr.getStringWidth(s)).max().getAsInt(), lis.size() * (fr.FONT_HEIGHT));
			} else {
				int width = fr.getStringWidth(text);
				boolean tooLong = width > maxWidth;
				double fac = maxWidth / (double) width;
				return new Dimension(Math.min(width, maxWidth), (int) ((fr.FONT_HEIGHT) * (tooLong ? fac : 1)));
			}
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

		private final int height, frameColor, color;
		private double filling;

		public HUDBar(int height, int frameColor, int color) {
			super();
			this.height = height;
			this.frameColor = frameColor;
			this.color = color;
			filling = .45;
			//			filling = Math.sin((Minecraft.getMinecraft().player.ticksExisted + Minecraft.getMinecraft().getRenderPartialTicks()) / 10.) / 2 + .5;
			//			System.out.println(filling);
		}

		@Override
		public Dimension dimension(int maxWidth) {
			return new Dimension(maxWidth, height);
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
			c = c.darker();
			GuiUtils.drawGradientRect(0, 1, 0, maxWidth - 1, height - 2, c.getRGB(), c.getRGB());
			GuiUtils.drawGradientRect(0, 1, 0, (int) ((maxWidth - 1) * filling), height - 2, color, color);
			//frame
			GuiUtils.drawGradientRect(0, 0, -1, 1, height - 1, frameColor, frameColor);
			GuiUtils.drawGradientRect(0, maxWidth - 1, -1, maxWidth, height - 1, frameColor, frameColor);
			GuiUtils.drawGradientRect(0, 1, -1, maxWidth - 1, 0, frameColor, frameColor);
			GuiUtils.drawGradientRect(0, 1, height - 2, maxWidth - 1, height - 1, frameColor, frameColor);
		}

	}

	public static class HUDStack extends HUDElement {
		private ItemStack stack;

		public HUDStack(ItemStack stack) {
			super();
			this.stack = stack;
		}

		@Override
		public Dimension dimension(int maxWidth) {
			return new Dimension(16, 16);
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
			//			render.zLevel -= 1600f;
			render.renderItemAndEffectIntoGUI(stack, 0, 0);
			//			render.zLevel += 1600f;
			GlStateManager.scale(1, 1, s);
			GlStateManager.translate(0, 0, -tr);
			GlStateManager.popMatrix();
			GlStateManager.disableLighting();
			RenderHelper.disableStandardItemLighting();
		}

	}

}
