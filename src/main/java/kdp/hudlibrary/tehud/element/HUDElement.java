package kdp.hudlibrary.tehud.element;

import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.TextTable.Alignment;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import kdp.hudlibrary.tehud.IHUDProvider;

public abstract class HUDElement {

    protected BiConsumer<HUDElement, CompoundNBT> reader = null;
    protected Alignment align = Alignment.LEFT;
    protected final Int2IntMap padding = new Int2IntOpenHashMap(4);
    protected Integer backgroundColor;
    protected final Cache<Integer, Dimension> dimensionCache = CacheBuilder.newBuilder()
            .expireAfterWrite(500, TimeUnit.MILLISECONDS).build();

    protected HUDElement() {
        padding.defaultReturnValue(/*TODO 1*/0);
    }

    public Alignment getAlignment() {
        return align;
    }

    public HUDElement setAlignment(Alignment align) {
        this.align = Objects.requireNonNull(align);
        return this;
    }

    public int getPadding(IHUDProvider.SpacingDirection dir) {
        return padding.get(dir.ordinal());
    }

    public final int getPaddingHorizontal() {
        return getPadding(IHUDProvider.SpacingDirection.LEFT) + getPadding(IHUDProvider.SpacingDirection.RIGHT);
    }

    public final int getPaddingVertical() {
        return getPadding(IHUDProvider.SpacingDirection.TOP) + getPadding(IHUDProvider.SpacingDirection.BOTTOM);
    }

    public HUDElement setPadding(IHUDProvider.SpacingDirection dir, int padding) {
        this.padding.put(dir.ordinal(), padding);
        return this;
    }

    public HUDElement setPadding(int padding) {
        for (int i = 0; i < 4; i++)
            this.padding.put(i, padding);
        return this;
    }

    public Integer getBackgroundColor() {
        return backgroundColor;
    }

    public HUDElement setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public HUDElement setReader(BiConsumer<HUDElement, CompoundNBT> reader) {
        this.reader = reader;
        return this;
    }

    public void readSyncTag(CompoundNBT tag) {
        if (reader != null)
            reader.accept(this, tag);
    }

    public final Dimension getDimension(int maxWidth) {
        try {
            return dimensionCache.get(maxWidth, () -> dimension(maxWidth));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    /** @return Dimension without padding */

    @Nonnull
    protected abstract Dimension dimension(int maxWidth);

    public abstract void draw(int maxWidth);

}
