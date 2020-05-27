package rainbowbismuth.fft.view;

import rainbowbismuth.fft.TacticsInspector;
import rainbowbismuth.fft.WordSize;

public class MiscUnitData {
    public static final int NUM = UnitData.NUM;
    public static final int SIZE = 0x440;
    public static final int TOTAL_SIZE = NUM * SIZE;
    private static final int STATUS_TO_ADD = 0x148;
    private static final int STATUS_TO_REMOVE = 0x150;
    private final TacticsInspector inspector;
    private final int address;
    private final int index;
    private final StatusArray statusToAdd;
    private final StatusArray statusToRemove;

    public MiscUnitData(final TacticsInspector inspector, final int address, final int index) {
        this.inspector = inspector;
        this.address = address;
        this.index = index;
        this.statusToAdd = new StatusArray(inspector, address + STATUS_TO_ADD);
        this.statusToRemove = new StatusArray(inspector, address + STATUS_TO_REMOVE);
    }

    public int getAddress() {
        return address;
    }

    public int getIndex() {
        return index;
    }

    public StatusArray getStatusToAdd() {
        return statusToAdd;
    }

    public StatusArray getStatusToRemove() {
        return statusToRemove;
    }

    public long read(final MiscUnitData.Field field) {
        return inspector.read(address + field.getOffset(), field.getWordSize());
    }

    public void write(final MiscUnitData.Field field, final long value) {
        inspector.write(address + field.getOffset(), value, field.getWordSize());
    }

    public enum Field {
        PREV(0x000, "Previous Pointer", WordSize.WORD),
        ID(0x004, "Misc Unit Data ID", WordSize.BYTE),
        PORTRAIT_VRAM_SLOT(0x005, "Portrait VRAM Slot", WordSize.BYTE),
        PORTRAIT_SPRITESHEET_ID(0x006, "Portrait ID", WordSize.BYTE),
        STORED_PALETTE(0x007, "Stored Palette", WordSize.BYTE),
        UNIT_SPRITESHEET_ID(0x00E, "Unit Spritesheet ID", WordSize.SHORT),
        UNIT_PALETTE(0x010, "Unit Palette", WordSize.SHORT),
        UNIT_DATA_POINTER(0x134, "Unit Data Pointer", WordSize.WORD),
        MODIFIED_PALETTE(0x13E, "Modified Palette", WordSize.SHORT);

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
