package kdp.hudlibrary.element;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

import com.mojang.blaze3d.platform.GlStateManager;

import kdp.hudlibrary.api.enums.MarginDirection;

public class HUDVerticalCompound extends HUDElement {
    protected final HUDElement[] elements;

    public HUDVerticalCompound(HUDElement... elements) {
        super();
        this.elements = Objects.requireNonNull(elements);
        setMargin(0);
    }

    public HUDVerticalCompound(Collection<? extends HUDElement> lis) {
        this(lis.toArray(new HUDElement[lis.size()]));
    }

    @Override
    protected Dimension dimension(int maxWidth) {
        if (elements.length == 0) {
            return new Dimension();
        }
        int width = Arrays.stream(elements).mapToInt(e -> e.dimension(maxWidth).width).max().orElse(0);
        int height = Arrays.stream(elements).mapToInt(e -> e.dimension(maxWidth).height).sum();
        return new Dimension(width, height);
    }

    @Override
    public void draw(int maxWidth) {
        int translateY = 0;
        for (HUDElement e : elements) {
            Dimension d = e.getDimension(maxWidth);
            GlStateManager.translated(0, e.getMargin(MarginDirection.TOP), 0);
            e.draw(maxWidth);
            GlStateManager.translated(0, d.height, 0);
            GlStateManager.translated(0, e.getMargin(MarginDirection.BOTTOM), 0);
            translateY += d.height + e.getMarginVertical();
        }
        GlStateManager.translated(0, -translateY, 0);
    }
}
