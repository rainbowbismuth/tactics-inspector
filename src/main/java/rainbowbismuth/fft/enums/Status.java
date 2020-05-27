package rainbowbismuth.fft.enums;

public enum Status {
    CRYSTAL("Crystal", 0, 0x40),
    DEAD("Dead", 0, 0x20),
    CHARGING("Charging", 0, 0x08),
    JUMP("Jump", 0, 0x04),
    DEFENDING("Defending", 0, 0x02),
    PERFORMING("Performing", 0, 0x01),
    PETRIFY("Petrify", 1, 0x80),
    INVITE("Invite", 1, 0x40),
    DARKNESS("Darkness", 1, 0x20),
    CONFUSION("Confusion", 1, 0x10),
    SILENCE("Silence", 1, 0x08),
    BLOOD_SUCK("Blood Suck", 1, 0x04),
    CURSED("Cursed", 1, 0x02),
    TREASURE("Treasure", 1, 0x01),
    OIL("Oil", 2, 0x80),
    FLOAT("Float", 2, 0x40),
    RERAISE("Reraise", 2, 0x20),
    TRANSPARENT("Transparent", 2, 0x10),
    BERSERK("Berserk", 2, 0x08),
    CHICKEN("Chicken", 2, 0x04),
    FROG("Frog", 2, 0x02),
    CRITICAL("Critical", 2, 0x01),
    POISON("Poison", 3, 0x80, 0),
    REGEN("Regen", 3, 0x40, 1),
    PROTECT("Protect", 3, 0x20, 2),
    SHELL("Shell", 3, 0x10, 3),
    HASTE("Haste", 3, 0x08, 4),
    SLOW("Slow", 3, 0x04, 5),
    STOP("Stop", 3, 0x02, 6),
    WALL("Wall", 3, 0x01, 7),
    FAITH("Faith", 4, 0x80, 8),
    INNOCENT("Innocent", 4, 0x40, 9),
    CHARM("Charm", 4, 0x20, 10),
    SLEEP("Sleep", 4, 0x10, 11),
    DONT_MOVE("Don't Move", 4, 0x08, 12),
    DONT_ACT("Don't Act", 4, 0x04, 13),
    REFLECT("Reflect", 4, 0x02, 14),
    DEATH_SENTENCE("Death Sentence", 4, 0x01, 15),
    ;

    public static final Status[] VALUES = Status.values();
    private final String displayName;
    private final int offset;
    private final int flag;
    private final Integer ctOffset;


    Status(final String displayName, final int offset, final int flag) {
        this.displayName = displayName;
        this.offset = offset;
        this.flag = flag;
        this.ctOffset = null;
    }

    Status(final String displayName, final int offset, final int flag, final int ctOffset) {
        this.displayName = displayName;
        this.offset = offset;
        this.flag = flag;
        this.ctOffset = ctOffset;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getOffset() {
        return offset;
    }

    public int getFlag() {
        return flag;
    }

    public Integer getCTOffset() {
        return ctOffset;
    }
}
