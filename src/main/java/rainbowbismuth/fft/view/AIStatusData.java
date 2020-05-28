package rainbowbismuth.fft.view;

import rainbowbismuth.fft.TacticsInspector;
import rainbowbismuth.fft.WordSize;

public class AIStatusData {
    public static final int NUM = 16;
    public static final int SIZE = 0x40;
    public static final int TOTAL_SIZE = NUM * SIZE;
    private static final int STATUS = 0x03;
    private static final int STATUS_CT = 0x08;
    private static final int INFLICTED_STATUS = 0x31;
    private final TacticsInspector inspector;
    private final int address;
    private final int index;
    private final StatusArray status;
    private final StatusCTArray statusCT;
    private final StatusArray inflictedStatus;

    public AIStatusData(final TacticsInspector inspector, final int address, final int index) {
        this.inspector = inspector;
        this.address = address;
        this.index = index;

        status = new StatusArray(inspector, address + STATUS);
        statusCT = new StatusCTArray(inspector, address + STATUS_CT);
        inflictedStatus = new StatusArray(inspector, address + INFLICTED_STATUS);
    }

    public TacticsInspector getInspector() {
        return inspector;
    }

    public int getAddress() {
        return address;
    }

    public int getIndex() {
        return index;
    }

    public StatusArray getStatus() {
        return status;
    }

    public StatusCTArray getStatusCT() {
        return statusCT;
    }

    public StatusArray getInflictedStatus() {
        return inflictedStatus;
    }

    public long read(final AIStatusData.Field field) {
        return inspector.read(address + field.getOffset(), field.getWordSize());
    }

    public void write(final AIStatusData.Field field, final long value) {
        inspector.write(address + field.getOffset(), value, field.getWordSize());
    }

    public int getUnitID() {
        return inspector.readByte(address);
    }

    public enum Field {
        ID(0x00, "Unit ID", WordSize.BYTE),
        DEATH_COUNTER(0x01, "Death Counter", WordSize.BYTE),
        HP(0x1b, "HP", WordSize.SHORT),
        MP(0x1e, "MP", WordSize.SHORT),
        CURRENT_ABILITY_CT(0x20, "Current Ability CT", WordSize.BYTE),
        ORIGINAL_PA(0x21, "Original PA", WordSize.BYTE),
        ORIGINAL_MA(0x22, "Original MA", WordSize.BYTE),
        ORIGINAL_SP(0x23, "Original SP", WordSize.BYTE),
        PA(0x24, "PA", WordSize.BYTE),
        MA(0x25, "MA", WordSize.BYTE),
        SP(0x26, "SP", WordSize.BYTE),
        CT(0x27, "CT", WordSize.BYTE);

        private final int offset;
        private final String displayName;
        private final WordSize wordSize;

        Field(final int offset, final String displayName, final WordSize wordSize) {
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
}
