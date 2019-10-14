package kdp.hudlibrary.element;

import java.awt.*;

import net.minecraftforge.fluids.FluidStack;

import kdp.hudlibrary.ClientHelper;

public class HUDFluidStack extends HUDElement {

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
        if (maxWidth >= 0 && width > maxWidth) {
            return new Dimension();
        }
        return new Dimension(width < 0 ? maxWidth : width, height);
    }

    @Override
    public void draw(int maxWidth) {
        if (maxWidth >= 0 && width > maxWidth) {
            return;
        }
        ClientHelper.drawFluidStack(stack, 0, 0, width < 0 ? maxWidth : width, height);
    }

}
