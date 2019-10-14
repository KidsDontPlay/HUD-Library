package kdp.hudlibrary.api.enums;

public enum MarginDirection {
    TOP, RIGHT, BOTTOM, LEFT;

    public boolean isHorizontal() {
        return this == LEFT || this == RIGHT;
    }

    public boolean isVertical() {
        return this == TOP || this == BOTTOM;
    }
}
