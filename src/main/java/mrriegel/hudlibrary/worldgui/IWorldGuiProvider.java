package mrriegel.hudlibrary.worldgui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public interface IWorldGuiProvider {

	WorldGui getGui(EntityPlayer player, BlockPos pos);

	ContainerWG getContainer(EntityPlayer player, BlockPos pos);
}
