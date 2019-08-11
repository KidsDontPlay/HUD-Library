package kdp.hudlibrary.tehud;

import java.util.List;
import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;

import kdp.hudlibrary.tehud.element.HUDElement;

public interface IHUDProvider {

    /**
     * Returns the background color for the HUD.
     *
     * Called on client.
     *
     * @return the background color
     */
    default int getBackgroundColor(PlayerEntity player, Direction facing) {
        return defaultBack;
    }

    /**
     * Returns the scale of the HUD.
     * By default the HUD is as wide as one block.
     *
     * Called on client.
     *
     * @return the scale
     */
    default double getScale(PlayerEntity player, Direction facing) {
        return 1;
    }

    /**
     * Determines if the HUD is visible to the player.
     *
     * Called on client.
     *
     * @return true if HUD is visible
     */
    default boolean isVisible(PlayerEntity player, Direction facing) {
        return true;
    }

    /**
     * Returns the offset for the given axis.
     *
     * Called on client.
     *
     * @return the offset
     */
    default double getOffset(PlayerEntity player, Direction facing, Axis axis) {
        return 0;
    }

    /**
     * Returns the width in pixels.
     *
     * Called on client.
     *
     * @return the width in pixels
     */
    default int getWidth(PlayerEntity player, Direction facing) {
        return 120;
    }

    /**
     * Returns the margin of the HUD to the elements.
     *
     * Called on client.
     *
     * @return the margin
     */
    default int getMargin(MarginDirection dir) {
        return 2;
    }

    /**
     * Determines if the HUD is always directed to the player.
     * By default the HUD only faces the cardinal points.
     *
     * Called on client.
     *
     * @return true if
     */
    default boolean smoothRotation(PlayerEntity player) {
        return false;
    }

    /**
     * Returns the list of elements in the HUD.
     * Every element uses the whole width of the HUD.
     * To use more elements in one line you need {@link kdp.hudlibrary.tehud.element.HUDCompound}.
     * data is the result of {@link #getNBTData}
     *
     * Called on client.
     *
     * @param data is null if {@link #usesServerData()} returns false
     * @return the elements
     */
    List<HUDElement> getElements(PlayerEntity player, Direction facing, @Nullable CompoundNBT data);

    /**
     * Determines if the HUD needs information from the server.
     *
     * Called on client and server.
     *
     * @return true if data from client is needed on server
     * to be displayed
     */
    default boolean usesServerData() {
        return false;
    }

    /**
     * Returns the compound nbt that is sent to the client
     * and given as parameter to {@link #getElements}
     *
     * Called on server if {@link #usesServerData} and {@link #needsSync}
     * return true.
     *
     * @return the compound nbt for synchronization
     */
    default CompoundNBT getNBTData(PlayerEntity player, Direction facing) {
        if (usesServerData()) {
            throw new UnsupportedOperationException();
        }
        return null;
    }

    /**
     * Called on server if {@link #usesServerData()} returns true.
     *
     * @return true if {@link #usesServerData()} returns true
     * and data on server changed and client needs to know about that.
     */
    default boolean needsSync() {
        return true;
    }

    enum Axis {
        /** up - down */
        VERTICAL,
        /** left - right */
        HORIZONTAL,
        /** front - back */
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
