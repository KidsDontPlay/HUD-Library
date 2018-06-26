package mrriegel.hudlibrary.tehud.element;

import java.awt.Dimension;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class HUDItemStack extends HUDElement {
	private static final Dimension dim = new Dimension(16, 16);
	private ItemStack stack;
	private boolean overlay = true;

	public HUDItemStack(ItemStack stack) {
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
