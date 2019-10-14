package kdp.hudlibrary;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;

public class BlockData {

    private final IWorldReader world;
    private final BlockPos pos;
    private final BlockState state;
    private final TileEntity tileEntity;

    public BlockData(IWorldReader world, BlockPos pos, BlockState state, TileEntity tileEntity) {
        this.world = world;
        this.pos = pos;
        this.state = state;
        this.tileEntity = tileEntity;
    }

    public IWorldReader getWorld() {
        return world;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }
}
