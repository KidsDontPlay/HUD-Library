package mrriegel.hudlibrary.tehud;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import mrriegel.hudlibrary.tehud.element.HUDElement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
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

	default boolean isVisible(EntityPlayer player, EnumFacing facing, TileEntity tile) {
		return tile.getPos().getDistance((int) player.posX, (int) player.posY, (int) player.posZ) <= 24;
	}

	List<HUDElement> getElements(EntityPlayer player, EnumFacing facing);

	Map<Integer, Function<TileEntity, NBTTagCompound>> getNBTData(EntityPlayer player, EnumFacing facing);

	default double offset(EntityPlayer player, EnumFacing facing, Axis axis) {
		return 0;
	}

	default int width(EntityPlayer player, EnumFacing facing) {
		return 100;
	}

	default boolean needsSync() {
		return true;
	}

	default int getMargin(Direction dir) {
		return 2;
	}

	default boolean is360degrees(EntityPlayer player) {
		return false;
	}

	public enum Axis {
		/** up-down */
		VERTICAL,
		/** left-right */
		HORIZONTAL,
		/** front-back */
		NORMAL;
	}

	public enum Direction {
		UP, RIGHT, DOWN, LEFT;

		public boolean isHorizontal() {
			return this == LEFT || this == RIGHT;
		}

		public boolean isVertical() {
			return this == UP || this == DOWN;
		}
	}

}
