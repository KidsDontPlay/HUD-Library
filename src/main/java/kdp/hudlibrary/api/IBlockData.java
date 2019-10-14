package kdp.hudlibrary.api;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public interface IBlockData {

    IWorld getWorld();

    BlockPos getPos();

    BlockState getBlockState();

    TileEntity getTileEntity();

    FluidState getFluidState();
}
