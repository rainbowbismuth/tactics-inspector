package rainbowbismuth.fft.view;

import rainbowbismuth.fft.TacticsInspector;
import rainbowbismuth.fft.WordSize;

public class ConsideredActionData {
    public static final int NUM = 20; // UNSURE!
    public static final int SIZE = 0x20;
    public static final int TOTAL_SIZE = NUM * SIZE;
    private final TacticsInspector inspector;
    private final int address;
    private final int index;

    public ConsideredActionData(final TacticsInspector inspector, final int address, final int index) {
        this.inspector = inspector;
        this.address = address;
        this.index = index;
    }

    public long read(final ConsideredActionData.Field field) {
        return inspector.read(address + field.getOffset(), field.getWordSize());
    }

    public void write(final ConsideredActionData.Field field, final long value) {
        inspector.write(address + field.getOffset(), value, field.getWordSize());
    }

    public int getAddress() {
        return address;
    }

    public int getIndex() {
        return index;
    }

    public enum Field {
        HIT_CHANCE(0x01, "Hit Chance?", WordSize.BYTE),
        TARGET_PRIORITY(0x02, "Target Priority Value", WordSize.SHORT),
        UNKNOWN_X7(0x07, "Unknown 0x07", WordSize.BYTE),
        UNKNOWN_X8(0x08, "Unknown 0x08", WordSize.BYTE),
        CASTER_UNIT_ID(0x0c, "Caster Unit ID", WordSize.BYTE),
        SKILLSET_ID(0x0d, "Skillset ID", WordSize.BYTE),
        ABILITY_ID(0x0e, "Ability ID", WordSize.SHORT),
        UNKNOWN_X14(0x14, "Unknown 0x14", WordSize.BYTE),
        UNKNOWN_X16(0x16, "Unknown 0x16", WordSize.BYTE),
        UNKNOWN_X17(0x17, "Unknown 0x17", WordSize.BYTE),
        TARGET_MAP_X(0x18, "Target Map X", WordSize.BYTE),
        TARGET_MAP_Y(0x1c, "Target Map Y", WordSize.BYTE),
        ;

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
