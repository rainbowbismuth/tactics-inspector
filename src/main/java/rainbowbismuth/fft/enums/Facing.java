package rainbowbismuth.fft.enums;

public enum Facing {
    SOUTH(0),
    EAST(1),
    NORTH(2),
    WEST(3),
    ;

    public static final Facing[] VALUES = Facing.values();
    private final int code;

    Facing(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
