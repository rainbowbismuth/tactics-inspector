package rainbowbismuth.fft;

import rainbowbismuth.fft.view.AIStatusData;
import rainbowbismuth.fft.view.MiscUnitData;
import rainbowbismuth.fft.view.UnitData;

import java.util.ArrayList;
import java.util.List;

import static rainbowbismuth.fft.PSMemory.RAM_SIZE;

public class TacticsInspector {
    private static final long USER_MEMORY_BASE = 0x8000_0000L;
    private static final long UNIT_STATS_ADDR = 0x8019_08ccL;
    private static final long MISC_UNIT_DATA_ADDR = 0x800b_7308L;
    private static final long AI_DATA_ADDR = 0x801a_f3c4L;
    private static final long UNIT_AI_EXTENDED_STATUS_ADDR = 0x8019_3924;

    private static final long ABILITY_NAME_TABLE_ADDR = 0x8016_3b88L;

    private final byte[] ram = new byte[RAM_SIZE];
    private final List<UnitData> unitData = new ArrayList<>(UnitData.NUM);
    private final List<MiscUnitData> miscUnitData = new ArrayList<>(MiscUnitData.NUM);
    private final List<AIStatusData> aiStatusData = new ArrayList<>(AIStatusData.NUM);

    private final List<Integer> writeAddresses = new ArrayList<>();
    private final List<byte[]> writeBytes = new ArrayList<>();

    public TacticsInspector() {
        for (int i = 0; i < UnitData.NUM; i++) {
            unitData.add(unitDataView(i));
        }
        for (int i = 0; i < MiscUnitData.NUM; i++) {
            miscUnitData.add(miscUnitDataView(i));
        }
        for (int i = 0; i < AIStatusData.NUM; i++) {
            aiStatusData.add(aiStatusDataView(i));
        }
    }

    public byte[] getRAM() {
        return ram;
    }

    private UnitData unitDataView(final int index) {
        int address = (int) (UNIT_STATS_ADDR - USER_MEMORY_BASE);
        address += index * UnitData.SIZE;
        return new UnitData(this, address, index);
    }

    private MiscUnitData miscUnitDataView(final int index) {
        int address = (int) (MISC_UNIT_DATA_ADDR - USER_MEMORY_BASE);
        address += index * MiscUnitData.SIZE;
        return new MiscUnitData(this, address, index);
    }

    private AIStatusData aiStatusDataView(final int index) {
        int address = (int) (UNIT_AI_EXTENDED_STATUS_ADDR - USER_MEMORY_BASE);
        address += index * AIStatusData.SIZE;
        return new AIStatusData(this, address, index);
    }

    public List<UnitData> getUnitData() {
        return unitData;
    }

    public List<AIStatusData> getAiStatusData() {
        return aiStatusData;
    }

    public List<MiscUnitData> getMiscUnitData() {
        final List<MiscUnitData> tempMiscUnitData = new ArrayList<>(miscUnitData);
        final List<MiscUnitData> sortedMisc = new ArrayList<>(MiscUnitData.NUM);
        long previous = 0;
        for (int i = 0; i < MiscUnitData.NUM; i++) {
            for (int j = 0; j < tempMiscUnitData.size(); j++) {
                final MiscUnitData j_misc = tempMiscUnitData.get(j);
                final long prev_pointer = j_misc.read(MiscUnitData.Field.PREV);
                if (prev_pointer == previous) {
                    previous = USER_MEMORY_BASE + j_misc.getAddress();
                    sortedMisc.add(j_misc);
                    tempMiscUnitData.remove(j);
                    break;
                }
            }
        }
        sortedMisc.addAll(tempMiscUnitData);
        return sortedMisc;
    }

    public int readByte(final int address) {
        return Byte.toUnsignedInt(ram[address]);
    }

    public long read(final int address, final WordSize wordSize) {
        long result = Byte.toUnsignedLong(ram[address]);
        if (wordSize == WordSize.SHORT || wordSize == WordSize.WORD) {
            result += Byte.toUnsignedLong(ram[address + 1]) << 8;
        }
        if (wordSize == WordSize.WORD) {
            result += Byte.toUnsignedLong(ram[address + 2]) << 16;
            result += Byte.toUnsignedLong(ram[address + 3]) << 24;
        }
        return result;
    }

    public String readString(final int start, final int endInclusive) {
        return CharacterSet.INSTANCE.read(ram, start, endInclusive);
    }

    public void write(final int address, final long value, final WordSize wordSize) {
        writeAddresses.add(address);
        switch (wordSize) {
            case BYTE:
                writeBytes.add(new byte[]{(byte) value});
                break;
            case SHORT:
                writeBytes.add(new byte[]{(byte) value, (byte) (value >> 8)});
                break;
            case WORD:
                writeBytes.add(new byte[]{(byte) value, (byte) (value >> 8), (byte) (value >> 16), (byte) (value >> 24)});
                break;
        }
    }

    public void flush(final PSMemory psMemory) throws PSMemoryWriteException {
        try {
            assert writeBytes.size() == writeAddresses.size();
            for (int i = 0; i < writeBytes.size(); i++) {
                final byte[] bytes = writeBytes.get(i);
                final int address = writeAddresses.get(i);
                psMemory.write(address, bytes);
            }
        } finally {
            writeBytes.clear();
            writeAddresses.clear();
        }
    }
}
