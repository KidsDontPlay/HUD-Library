package kdp.hudlibrary.element;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraftforge.common.util.TextTable.Alignment;

import org.apache.commons.lang3.Validate;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import kdp.hudlibrary.api.enums.MarginDirection;

public abstract class HUDElement {

    protected Alignment align = Alignment.LEFT;
    protected final Int2IntOpenHashMap margin = new Int2IntOpenHashMap(4);
    protected Integer backgroundColor;
    protected final Cache<Integer, Dimension> dimensionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(500, TimeUnit.MILLISECONDS).build();

    protected HUDElement() {
        margin.defaultReturnValue(1);
    }

    public Alignment getAlignment() {
        return align;
    }

    public HUDElement setAlignment(Alignment align) {
        this.align = Objects.requireNonNull(align);
        return this;
    }

    public int getMargin(MarginDirection dir) {
        return margin.get(dir.ordinal());
    }

    public final int getMarginHorizontal() {
        return getMargin(MarginDirection.LEFT) + getMargin(MarginDirection.RIGHT);
    }

    public final int getMarginVertical() {
        return getMargin(MarginDirection.TOP) + getMargin(MarginDirection.BOTTOM);
    }

    public HUDElement setMargin(MarginDirection dir, int margin) {
        this.margin.put(dir.ordinal(), margin);
        return this;
    }

    public HUDElement setMargin(int margin) {
        for (int i = 0; i < 4; i++)
            this.margin.put(i, margin);
        return this;
    }

    public Integer getBackgroundColor() {
        return backgroundColor;
    }

    public HUDElement setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public final Dimension getDimension(int maxWidth) {
        try {
            Dimension d = dimensionCache.get(maxWidth, () -> dimension(maxWidth));
            Validate.isTrue(d.width <= maxWidth, "Width of %s is greater than allowed. max: %d, actual: %d",
                    getClass().toString(), maxWidth, d.width);
            return d;
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /** @return Dimension without margin */

    @Nonnull
    protected abstract Dimension dimension(int maxWidth);

    public abstract void draw(int maxWidth);

}
