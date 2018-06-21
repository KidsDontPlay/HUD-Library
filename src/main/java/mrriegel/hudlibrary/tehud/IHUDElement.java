package mrriegel.hudlibrary.tehud;

import java.awt.Dimension;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.TextTable.Alignment;

public interface IHUDElement {

	default @Nonnull Alignment alignment() {
		return Alignment.LEFT;
	}

	@Nonnull
	Dimension dimension(int maxWidth);

	NBTTagCompound writeSyncTag();

	void readSyncTag(NBTTagCompound tag);

	default boolean newLine() {
		return true;
	}

	void draw(int x, int y, int maxWidth);

	public static class HUDText implements IHUDElement {
		private final String text;
		private boolean shadow;
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
			List<String> lis = fr.listFormattedStringToWidth(text, maxWidth);
			return new Dimension(lis.stream().mapToInt(s -> fr.getStringWidth(s) + 2).max().getAsInt(), lis.size() * (fr.FONT_HEIGHT + 2));
		}

		@Override
		public NBTTagCompound writeSyncTag() {
			return null;
		}

		@Override
		public void readSyncTag(NBTTagCompound tag) {
		}

		@Override
		public boolean newLine() {
			return lineBreak;
		}

		@Override
		public void draw(int x, int y, int maxWidth) {
			List<String> lis = fr.listFormattedStringToWidth(text, maxWidth);
			for (int i = 0; i < lis.size(); i++) {
				String s = lis.get(i);
				fr.drawString(s, x + 1, y + 1 + (i * (fr.FONT_HEIGHT + 2)), color, shadow);
			}
		}

	}

	public static class HUDBar implements IHUDElement {

		@Override
		public Dimension dimension(int maxWidth) {
			return null;
		}

		@Override
		public NBTTagCompound writeSyncTag() {
			return null;
		}

		@Override
		public void readSyncTag(NBTTagCompound tag) {

		}

		@Override
		public boolean newLine() {
			return true;
		}

		@Override
		public void draw(int x, int y, int maxWidth) {
			// TODO Auto-generated method stub

		}

	}

	public static class HUDStack implements IHUDElement {
		private final ItemStack stack;

		public HUDStack(ItemStack stack) {
			super();
			this.stack = stack;
		}

		@Override
		public Dimension dimension(int maxWidth) {
			return new Dimension(18, 18);
		}

		@Override
		public NBTTagCompound writeSyncTag() {
			return null;
		}

		@Override
		public void readSyncTag(NBTTagCompound tag) {
		}

		@Override
		public boolean newLine() {
			return true;
		}

		@Override
		public void draw(int x, int y, int maxWidth) {
			GlStateManager.disableLighting();
			RenderHelper.enableStandardItemLighting();
			RenderHelper.enableGUIStandardItemLighting();
			int s = 100000;
			GlStateManager.scale(1, 1, 1. / s);
			Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(stack, x + 1, y + 1);
			GlStateManager.scale(1, 1, s);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.enableLighting();
		}

	}

}
