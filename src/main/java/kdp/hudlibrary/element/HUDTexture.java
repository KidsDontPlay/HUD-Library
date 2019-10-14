package kdp.hudlibrary.element;

import java.awt.*;
import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.config.GuiUtils;

public class HUDTexture extends HUDElement {
    private final ResourceLocation texture;
    private final int u, v, width, height;

    public HUDTexture(ResourceLocation texture, int u, int v, int width, int height) {
        this.texture = texture;
        this.u = u;
        this.v = v;
        this.width = width;
        this.height = height;
    }

    @Nonnull
    @Override
    protected Dimension dimension(int maxWidth) {
        if (width > maxWidth) {
            return new Dimension();
        }
        return new Dimension(width, height);
    }

    @Override
    public void draw(int maxWidth) {
        if (width > maxWidth) {
            return;
        }
        Minecraft.getInstance().getTextureManager().bindTexture(texture);
        GuiUtils.drawTexturedModalRect(0, 0, u, v, width, height, 0);
    }
}
