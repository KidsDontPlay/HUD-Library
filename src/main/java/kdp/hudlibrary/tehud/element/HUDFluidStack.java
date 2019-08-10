package kdp.hudlibrary.tehud.element;

import java.awt.*;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

import kdp.hudlibrary.ClientHelper;

public class HUDFluidStack extends HUDElement<CompoundNBT> {

    private FluidStack stack;
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
        return new Dimension(width < 0 ? maxWidth : width, height);
    }

    @Override
    public HUDElement read(CompoundNBT tag) {
        if (reader != null) {
            reader.accept(this, tag);
            return this;
        }
        stack = FluidStack.loadFluidStackFromNBT(tag);
        return this;
    }

    @Override
    public void draw(int maxWidth) {
        ClientHelper.drawFluidStack(stack, 0, 0, width < 0 ? maxWidth : width, height);
    }

}
