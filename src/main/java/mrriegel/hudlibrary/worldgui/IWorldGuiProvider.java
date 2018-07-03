package mrriegel.hudlibrary.worldgui;

import net.minecraft.entity.player.EntityPlayer;

public interface IWorldGuiProvider {

	WorldGui openGui(EntityPlayer player);
}
