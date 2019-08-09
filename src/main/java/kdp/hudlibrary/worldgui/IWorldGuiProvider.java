package kdp.hudlibrary.worldgui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public interface IWorldGuiProvider {

    WorldGui getGui(PlayerEntity player, BlockPos pos);

    ContainerWG getContainer(PlayerEntity player, BlockPos pos);
}
