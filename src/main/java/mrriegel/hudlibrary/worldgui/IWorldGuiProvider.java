package mrriegel.hudlibrary.worldgui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public interface IWorldGuiProvider {

	WorldGui getGui(EntityPlayer player, BlockPos pos);

	WorldGuiContainer getContainer(EntityPlayer player, BlockPos pos);
}
