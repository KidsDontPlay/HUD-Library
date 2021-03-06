package kdp.hudlibrary.element;

import java.awt.*;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

public class HUDFluidStack extends HUDElement {

    private FluidStack stack = FluidStack.EMPTY;
    private final int width, height;

    public HUDFluidStack(FluidStack stack, int width, int height) {
        super();
        this.stack = stack;
        this.width = width;
        this.height = height;
    }

    public HUDFluidStack setStack(FluidStack stack) {
        this.stack = stack;
        return this;
    }

    @Override
    protected Dimension dimension(int maxWidth) {
        if (maxWidth >= 0 && width > maxWidth || stack.isEmpty()) {
            return new Dimension();
        }
        return new Dimension(width < 0 ? maxWidth : width, height);
    }

    @Override
    public void draw(int maxWidth) {
        if (maxWidth >= 0 && width > maxWidth || stack.isEmpty()) {
            return;
        }
        int w = width < 0 ? maxWidth : width;
        int h = height;
        Minecraft mc = Minecraft.getInstance();
        TextureAtlasSprite icon = mc.getTextureMap().getSprite(stack.getFluid().getAttributes().getStillTexture());
        if (icon == null) {
            return;
        }
        int renderAmount = Math.max(h, 1);
        int posY = h - renderAmount;

        mc.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
        int color = stack.getFluid().getAttributes().getColor(stack);
        GL11.glColor3ub((byte) (color >> 16 & 0xFF), (byte) (color >> 8 & 0xFF), (byte) (color & 0xFF));

        GlStateManager.enableBlend();
        for (int i = 0; i < w; i += 16) {
            for (int j = 0; j < renderAmount; j += 16) {
                int drawWidth = Math.min(w - i, 16);
                int drawHeight = Math.min(renderAmount - j, 16);

                int drawX = i;
                int drawY = posY + j;

                double minU = icon.getMinU();
                double maxU = icon.getMaxU();
                double minV = icon.getMinV();
                double maxV = icon.getMaxV();

                Tessellator tessellator = Tessellator.getInstance();
                BufferBuilder tes = tessellator.getBuffer();
                tes.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
                double v = minV + (maxV - minV) * drawHeight / 16F;
                double u = minU + (maxU - minU) * drawWidth / 16F;
                tes.pos(drawX, drawY + drawHeight, 0).tex(minU, v).endVertex();
                tes.pos(drawX + drawWidth, drawY + drawHeight, 0).tex(u, v).endVertex();
                tes.pos(drawX + drawWidth, drawY, 0).tex(u, minV).endVertex();
                tes.pos(drawX, drawY, 0).tex(minU, minV).endVertex();
                tessellator.draw();
            }
        }
        GlStateManager.disableBlend();
        GlStateManager.color3f(1, 1, 1);
    }

}
