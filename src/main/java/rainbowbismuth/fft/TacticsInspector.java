package rainbowbismuth.fft;

import rainbowbismuth.fft.view.UnitData;

import java.util.ArrayList;
import java.util.List;

import static rainbowbismuth.fft.PSMemory.RAM_SIZE;

public class TacticsInspector {
    private static final long USER_MEMORY_BASE = 0x8000_0000L;
    private static final long UNIT_STATS_ADDR = 0x8019_08ccL;
    private static final long MISC_UNIT_DATA_ADDR = 0x800b_7308L;
    private static final long ABILITY_NAME_TABLE_ADDR = 0x8016_3b88L;

    private final byte[] ram = new byte[RAM_SIZE];
    private final List<UnitData> unitData = new ArrayList<>(UnitData.NUM);

    private final List<Integer> writeAddresses = new ArrayList<>();
    private final List<byte[]> writeBytes = new ArrayList<>();

    public TacticsInspector() {
        for (int i =0; i<UnitData.NUM; i++) {
            unitData.add(unitDataView(i));
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

    public List<UnitData> getUnitData() {
        return unitData;
    }
    
    public int readByte(final int address) {
        return Byte.toUnsignedInt(ram[address]);
    }

    public long read(final int address, WordSize wordSize) {
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
