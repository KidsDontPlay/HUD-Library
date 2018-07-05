package mrriegel.hudlibrary.tehud.element;

import java.awt.Dimension;

import mrriegel.hudlibrary.ClientHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class HUDFluidStack extends HUDElement {

	private FluidStack stack;
	private final int width, height;

	public HUDFluidStack(FluidStack stack, int width, int height) {
		super();
		this.stack = stack;
		this.width = width;
		this.height = height;
	}

	public FluidStack getStack() {
		return stack;
	}

	public HUDFluidStack setStack(FluidStack stack) {
		this.stack = stack;
		return this;
	}

	@Override
	public Dimension dimension(int maxWidth) {
		Dimension d = dims.get(maxWidth);
		if (d != null)
			return d;
		d = new Dimension(width < 0 ? maxWidth : width, height);
		dims.put(maxWidth, d);
		return d;
	}

	@Override
	public void readSyncTag(NBTTagCompound tag) {
		if (reader != null) {
			reader.accept(this, tag);
			return;
		}
		if (tag.hasKey("stack"))
			stack = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("stack"));
	}

	@Override
	public void draw(int maxWidth) {
		ClientHelper.drawFluidStack(stack, 0, 0, width < 0 ? maxWidth : width, height);
	}

}
