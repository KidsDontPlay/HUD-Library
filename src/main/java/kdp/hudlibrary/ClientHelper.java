package kdp.hudlibrary;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

public class ClientHelper {

    public static void drawFluidStack(FluidStack stack, int x, int y, int w, int h) {
    	throw new UnsupportedOperationException();
		/*if (stack == null || stack.getFluid() == null)
			return;
		Minecraft mc = Minecraft.getMinecraft();
		TextureAtlasSprite icon = mc.getTextureMapBlocks().getTextureExtry(stack.getFluid().getStill().toString());
		if (icon == null) {
			return;
		}
		int renderAmount = Math.max(h, 1);
		int posY = y + h - renderAmount;

		mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		int color = stack.getFluid().getColor(stack);
		GL11.glColor3ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF));

		GlStateManager.enableBlend();
		for (int i = 0; i < w; i += 16) {
			for (int j = 0; j < renderAmount; j += 16) {
				int drawWidth = Math.min(w - i, 16);
				int drawHeight = Math.min(renderAmount - j, 16);

				int drawX = x + i;
				int drawY = posY + j;

				double minU = icon.getMinU();
				double maxU = icon.getMaxU();
				double minV = icon.getMinV();
				double maxV = icon.getMaxV();

				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder tes = tessellator.getBuffer();
				tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
				tes.pos(drawX, drawY + drawHeight, 0).tex(minU, minV + (maxV - minV) * drawHeight / 16F).endVertex();
				tes.pos(drawX + drawWidth, drawY + drawHeight, 0).tex(minU + (maxU - minU) * drawWidth / 16F, minV + (maxV - minV) * drawHeight / 16F).endVertex();
				tes.pos(drawX + drawWidth, drawY, 0).tex(minU + (maxU - minU) * drawWidth / 16F, minV).endVertex();
				tes.pos(drawX, drawY, 0).tex(minU, minV).endVertex();
				tessellator.draw();
			}
		}
		GlStateManager.disableBlend();
		GlStateManager.color3f(1,1,1);*/
    }

}
