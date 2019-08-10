package kdp.hudlibrary.tehud;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.fml.LogicalSide;

import kdp.hudlibrary.tehud.element.HUDElement;

public interface IHUDProvider {

    //<client>

    default int getBackgroundColor(PlayerEntity player, Direction facing) {
        return defaultBack;
    }

    default double getScale(PlayerEntity player, Direction facing) {
        return 1;
    }

    default boolean isVisible(PlayerEntity player, Direction facing) {
        return true;
    }

    default double getOffset(PlayerEntity player, Direction facing, Axis axis) {
        return 0;
    }

    default int getWidth(PlayerEntity player, Direction facing) {
        return 120;
    }

    default int getMargin(MarginDirection dir) {
        return 2;
    }

    default boolean is360degrees(PlayerEntity player) {
        return false;
    }

    /**
     *
     * @param player
     * @param facing
     * @param data is null if {@link #readingSide()} returns {@link LogicalSide#CLIENT}
     * @return
     */
    List<HUDElement> getElements(PlayerEntity player, Direction facing, @Nullable Map<Integer, INBT> data);

    //</client>

    default LogicalSide readingSide() {
        return LogicalSide.CLIENT;
    }

    @Nonnull
    default Map<Integer, INBT> getNBTData(PlayerEntity player, Direction facing) {
        return Collections.emptyMap();
    }

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

    enum MarginDirection {
        TOP, RIGHT, BOTTOM, LEFT;

        public boolean isHorizontal() {
            return this == LEFT || this == RIGHT;
        }

        public boolean isVertical() {
            return this == TOP || this == BOTTOM;
        }
    }

    int defaultBack = 0xBBBBBBDD;

}
