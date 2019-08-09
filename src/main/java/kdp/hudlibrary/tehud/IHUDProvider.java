package kdp.hudlibrary.tehud;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.LogicalSide;

import kdp.hudlibrary.tehud.element.HUDElement;

public interface IHUDProvider<T> {

    //<client>

    default int getBackgroundColor(PlayerEntity player, Direction facing) {
        return 0x44CCCCFF;
    }

    default double totalScale(PlayerEntity player, Direction facing) {
        return 1;
    }

    default boolean isVisible(PlayerEntity player, Direction facing) {
        return true;
    }

    default double offset(PlayerEntity player, Direction facing, Axis axis) {
        return 0;
    }

    default int width(PlayerEntity player, Direction facing) {
        return 120;
    }

    default int getMargin(SpacingDirection dir) {
        return 2;
    }

    default boolean is360degrees(PlayerEntity player) {
        return false;
    }

    //</client>

    List<HUDElement> getElements(PlayerEntity player, Direction facing);

    default LogicalSide readingSide() {
        return LogicalSide.CLIENT;
    }

    Map<Integer, Function<T, CompoundNBT>> getNBTData(PlayerEntity player, Direction facing);

    /**
     * Called on server
     *
     * @return true if {@link #readingSide()} returns {@link LogicalSide#SERVER}
     * and data on server changed and client needs to know about that.
     */
    default boolean needsSync() {
        return true;
    }

    enum Axis {
        /** up-down */
        VERTICAL,
        /** left-right */
        HORIZONTAL,
        /** front-back */
        NORMAL;
    }

    enum SpacingDirection {
        TOP, RIGHT, BOTTOM, LEFT;

        public boolean isHorizontal() {
            return this == LEFT || this == RIGHT;
        }

        public boolean isVertical() {
            return this == TOP || this == BOTTOM;
        }
    }

}
