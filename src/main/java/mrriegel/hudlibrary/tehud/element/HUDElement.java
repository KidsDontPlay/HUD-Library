package mrriegel.hudlibrary.tehud.element;

import java.awt.Dimension;
import java.util.function.BiConsumer;

import javax.annotation.Nonnull;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mrriegel.hudlibrary.tehud.IHUDProvider.Direction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.TextTable.Alignment;

public abstract class HUDElement {

	protected BiConsumer<HUDElement, NBTTagCompound> reader = null;
	protected @Nonnull Alignment align = Alignment.LEFT;
	protected final Int2IntMap padding = new Int2IntOpenHashMap(4);
	protected final Int2ObjectMap<Dimension> dims = new Int2ObjectOpenHashMap<>();

	protected HUDElement() {
		padding.defaultReturnValue(1);
	}

	public Alignment getAlignment() {
		return Alignment.LEFT;
	}

	public HUDElement setAlignment(Alignment align) {
		this.align = align;
		return this;
	}

	public int getPadding(Direction dir) {
		return padding.get(dir.ordinal());
	}

	public final int getPaddingHorizontal() {
		return getPadding(Direction.LEFT) + getPadding(Direction.RIGHT);
	}

	public final int getPaddingVertical() {
		return getPadding(Direction.UP) + getPadding(Direction.DOWN);
	}

	public HUDElement setPadding(Direction dir, int padding) {
		this.padding.put(dir.ordinal(), padding);
		return this;
	}

	public HUDElement setPadding(int padding) {
		for (int i = 0; i < 4; i++)
			this.padding.put(i, padding);
		return this;
	}

	public HUDElement setReader(BiConsumer<HUDElement, NBTTagCompound> reader) {
		this.reader = reader;
		return this;
	}

	public void readSyncTag(NBTTagCompound tag) {
		if (reader != null)
			reader.accept(this, tag);
	}

	/** @return Dimension without padding */

	@Nonnull
	public abstract Dimension dimension(int maxWidth);

	public abstract void draw(int maxWidth);

}
