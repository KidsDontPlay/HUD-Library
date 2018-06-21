package mrriegel.hudlibrary.tehud;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;

public interface IHUDProvider {

	default int getBackgroundColor(EntityPlayer player, EnumFacing facing) {
		return 0x44CCCCFF;
	}

	default Side readingSide() {
		return Side.CLIENT;
	}

	default double totalScale(EntityPlayer player, EnumFacing facing) {
		return 1;
	}

	default boolean requireFocus(EntityPlayer player, EnumFacing facing) {
		return true;
	}

	List<IHUDElement> elements(EntityPlayer player, EnumFacing facing);

	default double offset(EntityPlayer player, Axis axis, EnumFacing facing) {
		return 0;
	}

	default int width(EntityPlayer player, EnumFacing facing) {
		return 100;
	}

	public enum Axis {
		/** up-down */
		VERTICAL,
		/** left-right */
		HORIZONTAL,
		/** front-back */
		NORMAL;
	}

}
