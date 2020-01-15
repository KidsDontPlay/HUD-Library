package kdp.hudlibrary.element;

import java.awt.*;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;

import kdp.hudlibrary.api.enums.MarginDirection;

public class HUDItemStack extends HUDElement {

    private ItemStack stack = ItemStack.EMPTY;
    private boolean overlay = true;

    public HUDItemStack() {
    }

    public HUDItemStack(ItemStack stack) {
        super();
        this.stack = stack;
    }

    public HUDItemStack setStack(ItemStack stack) {
        this.stack = stack;
        return this;
    }

    public HUDItemStack setOverlay(boolean overlay) {
        this.overlay = overlay;
        return this;
    }

    @Override
    protected Dimension dimension(int maxWidth) {
        return stack.isEmpty() || maxWidth < 16 ? new Dimension() : new Dimension(16, 16);
    }

    @Override
    public void draw(int maxWidth) {
        if (stack.isEmpty() || maxWidth < 16)
            return;
        int scaleFactor = 1000;
        //GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        //GlStateManager.depthMask(true);
        //GlStateManager.enableDepth();
        GlStateManager.scaled(1, 1, 1. / scaleFactor);
        ItemRenderer render = Minecraft.getInstance().getItemRenderer();
        render.renderItemAndEffectIntoGUI(stack, 0, 0);
        GlStateManager.enableRescaleNormal();
        if (overlay && stack.getCount() != 1) {
            String s = String.valueOf(stack.getCount());
            GlStateManager.disableLighting();
            //GlStateManager.disableDepthTest();
            GlStateManager.disableBlend();
            GlStateManager.translated(0, 0, scaleFactor + 1.);
            Minecraft.getInstance().fontRenderer
                    .drawStringWithShadow(s, (float) (19 - 2 - Minecraft.getInstance().fontRenderer.getStringWidth(s)),
                            (float) (6 + 3), 16777215);
            GlStateManager.translated(0, 0, -(scaleFactor + 1));
            GlStateManager.enableBlend();
            GlStateManager.enableLighting();
            //GlStateManager.enableDepthTest();
            GlStateManager.enableBlend();
        }
        GlStateManager.scaled(1, 1, scaleFactor);
        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();
    }

    @Override
    public int getMargin(MarginDirection dir) {
        return stack.isEmpty() ? 0 : super.getMargin(dir);
    }

}
