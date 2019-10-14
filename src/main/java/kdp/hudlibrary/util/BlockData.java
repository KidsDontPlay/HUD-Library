package kdp.hudlibrary.util;

import net.minecraft.block.BlockState;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

import kdp.hudlibrary.api.IBlockData;

public class BlockData implements IBlockData {

    private final IWorld world;
    private final BlockPos pos;
    private final BlockState blockState;
    private final TileEntity tileEntity;
    private final FluidState fluidState;

    public BlockData(IWorld world, BlockPos pos, BlockState blockState, TileEntity tileEntity, FluidState fluidState) {
        this.world = world;
        this.pos = pos;
        this.blockState = blockState;
        this.tileEntity = tileEntity;
        this.fluidState = fluidState;
    }

    @Override
    public IWorld getWorld() {
        return world;
    }

    @Override
    public BlockPos getPos() {
        return pos;
    }

    @Override
    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public TileEntity getTileEntity() {
        return tileEntity;
    }

    @Override
    public FluidState getFluidState() {
        return fluidState;
    }
}
