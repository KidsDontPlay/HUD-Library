package mrriegel.hudlibrary.tehud.element;

import java.awt.Dimension;

import mrriegel.hudlibrary.tehud.IHUDProvider.Direction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class HUDItemStack extends HUDElement {
	private static final Dimension dim16 = new Dimension(16, 16);
	private static final Dimension dim0 = new Dimension();

	private ItemStack stack;
	private boolean overlay = true;
	private int customSize = -1;

	public HUDItemStack(ItemStack stack) {
		super();
		this.stack = stack;
	}

	public ItemStack getStack() {
		return stack;
	}

	public void setStack(ItemStack stack) {
		this.stack = stack;
	}

	public boolean isOverlay() {
		return overlay;
	}

	public void setOverlay(boolean overlay) {
		this.overlay = overlay;
	}

	@Override
	public Dimension dimension(int maxWidth) {
		return stack.isEmpty() ? dim0 : dim16;
	}

	@Override
	public void readSyncTag(NBTTagCompound tag) {
		if (reader != null) {
			reader.accept(this, tag);
			return;
		}
		if (tag.hasKey("stack"))
			stack = new ItemStack(tag.getCompoundTag("stack"));
		if (tag.hasKey("customSize"))
			customSize = tag.getInteger("customSize");
		if (customSize > 0)
			stack.setCount(customSize);
	}

	@Override
	public void draw(int maxWidth) {
		if (stack.isEmpty())
			return;
		RenderHelper.enableGUIStandardItemLighting();
		int s = 1000;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.pushMatrix();
		GlStateManager.scale(1, 1, 1. / s);
		RenderItem render = Minecraft.getMinecraft().getRenderItem();
		render.renderItemAndEffectIntoGUI(stack, 0, 0);
		GlStateManager.enableRescaleNormal();
		if (overlay) {
			render.renderItemOverlayIntoGUI(Minecraft.getMinecraft().fontRenderer, stack, 0, 0, null);
		}
		GlStateManager.scale(1, 1, s);
		GlStateManager.popMatrix();
		RenderHelper.disableStandardItemLighting();
	}

	@Override
	public int getPadding(Direction dir) {
		return stack.isEmpty() ? 0 : super.getPadding(dir);
	}

}
