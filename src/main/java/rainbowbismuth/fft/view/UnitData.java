package rainbowbismuth.fft.view;

import rainbowbismuth.fft.TacticsInspector;
import rainbowbismuth.fft.WordSize;
import rainbowbismuth.fft.enums.Facing;

public class UnitData {
    public static final int NUM = 20;
    public static final int SIZE = 0x1C0;
    public static final int TOTAL_SIZE = NUM * SIZE;

    private final TacticsInspector inspector;
    private final int address;
    private final int index;
    private final StatusArray autoStatus;
    private final StatusArray statusImmunity;
    private final StatusArray status;
    private final StatusArray attackAddStatus;
    private final StatusArray attackRemoveStatus;
    private final StatusArray attackInflictedStatus;

    private static final int AUTO_STATUS = 0x004E;
    private static final int STATUS_IMMUNITY = 0x0053;
    private static final int STATUS = 0x0058;
    private static final int ATTACK_ADD_STATUS = 0x01A7;
    private static final int ATTACK_REMOVE_STATUS = 0x01AC;
    private static final int ATTACK_INFLICTED_STATUS = 0x1BB;

    public enum Field {
        EXISTS(0x01, "Exists", WordSize.BYTE),
        EXPERIENCE(0x21, "Experience", WordSize.BYTE),
        LEVEL(0x22, "Level", WordSize.BYTE),
        BRAVE(0x24, "Brave", WordSize.BYTE),
        FAITH(0x26, "Faith", WordSize.BYTE),
        HP(0x28, "HP", WordSize.SHORT),
        MAX_HP(0x2A, "Max HP", WordSize.SHORT),
        MP(0x2C, "MP", WordSize.SHORT),
        MAX_MP(0x2E, "Max MP", WordSize.SHORT),
        ORIGINAL_PA(0x30, "Original PA", WordSize.BYTE),
        ORIGINAL_MA(0x31, "Original MA", WordSize.BYTE),
        ORIGINAL_SP(0x32, "Original SP", WordSize.BYTE),
        EQUIP_PA(0x33, "Equipment PA", WordSize.BYTE),
        EQUIP_MA(0x34, "Equipment MA", WordSize.BYTE),
        EQUIP_SP(0x35, "Equipment SP", WordSize.BYTE),
        PA(0x36, "PA", WordSize.BYTE),
        MA(0x37, "MA", WordSize.BYTE),
        SP(0x38, "SP", WordSize.BYTE),
        CT(0x39, "CT", WordSize.BYTE),
        MOVE(0x3A, "Move", WordSize.BYTE),
        JUMP(0x3B, "Jump", WordSize.BYTE),
        X_COORD(0x47, "Map X Coordinate", WordSize.BYTE),
        Y_COORD(0x48, "Map Y Coordinate", WordSize.BYTE),
        ELEVATION_FACING(0x49, "Elevation & Facing", WordSize.BYTE),
        LAST_USED_ABILITY_ID(0x170, "Last Used Ability ID", WordSize.SHORT),
        IS_TAKING_TURN(0x186, "Is Taking Turn", WordSize.BYTE);

        private final int offset;
        private final String displayName;
        private final WordSize wordSize;

        Field(int offset, String displayName, WordSize wordSize) {
            this.offset = offset;
            this.displayName = displayName;
            this.wordSize = wordSize;
        }

        public int getOffset() {
            return offset;
        }

        public String getDisplayName() {
            return displayName;
        }

        public WordSize getWordSize() {
            return wordSize;
        }
    }

    public UnitData(final TacticsInspector inspector, final int address, final int index) {
        this.inspector = inspector;
        this.address = address;
        this.index = index;

        autoStatus = new StatusArray(inspector, address + AUTO_STATUS);
        statusImmunity = new StatusArray(inspector, address + STATUS_IMMUNITY);
        status = new StatusArray(inspector, address + STATUS);
        attackAddStatus = new StatusArray(inspector, address + ATTACK_ADD_STATUS);
        attackRemoveStatus = new StatusArray(inspector, address + ATTACK_REMOVE_STATUS);
        attackInflictedStatus = new StatusArray(inspector, address + ATTACK_INFLICTED_STATUS);
    }

    public long read(final Field field) {
        return inspector.read(address + field.getOffset(), field.getWordSize());
    }

    public void write(final Field field, final long value) {
        inspector.write(address + field.getOffset(), value, field.getWordSize());
    }

    public int getAddress() {
        return address;
    }

    public int getIndex() {
        return index;
    }

    public boolean isInvalid() {
        return read(Field.EXISTS) == 0xFF;
    }

    public boolean isOnHigherElevation() {
        return (read(Field.ELEVATION_FACING) & 0x80) != 0;
    }

    private static final int UNIT_NAME = 0x012C;
    private static final int UNIT_NAME_END = 0x013B;
    private static final int JOB_NAME = 0x013C;
    private static final int JOB_NAME_END = 0x0149;

    /**
     * @return The unit's name, e.g. "Rad"
     */
    public String getName() {
        return inspector.readString(address + UNIT_NAME, address + UNIT_NAME_END);
    }

    /**
     * @return The unit's job, e.g. "Squire"
     */
    public String getJobName() {
        return inspector.readString(address + JOB_NAME, address + JOB_NAME_END);
    }

    public Facing getFacing() {
        final int index = (int) (read(Field.ELEVATION_FACING) & 0b11L);
        return Facing.VALUES[index];
    }

    public StatusArray getAutoStatus() {
        return autoStatus;
    }

    public StatusArray getStatusImmunity() {
        return statusImmunity;
    }

    public StatusArray getStatus() {
        return status;
    }

    public StatusArray getAttackAddStatus() {
        return attackAddStatus;
    }

    public StatusArray getAttackRemoveStatus() {
        return attackRemoveStatus;
    }

    public StatusArray getAttackInflictedStatus() {
        return attackInflictedStatus;
    }

    /**
     * @return True if the unit is currently taking their turn.
     */
    public boolean isTakingTurn() {
        return read(Field.IS_TAKING_TURN) == 0x01;
    }
}
