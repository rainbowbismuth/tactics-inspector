package rainbowbismuth.fft.view;

import rainbowbismuth.fft.TacticsInspector;
import rainbowbismuth.fft.WordSize;
import rainbowbismuth.fft.enums.Status;

public class StatusArray {
    public static final int SIZE = 0x05;

    private final TacticsInspector inspector;
    private final int address;

    public StatusArray(final TacticsInspector inspector, final int address) {
        this.inspector = inspector;
        this.address = address;
    }

    public boolean hasStatus(final Status status) {
        final int statusByte = inspector.readByte(address + status.getOffset());
        return (statusByte & status.getFlag()) != 0;
    }

    public void setStatus(final Status status, final boolean value) {
        final int statusByteAddr = address + status.getOffset();
        final int statusByte = inspector.readByte(statusByteAddr);
        if (value) {
            inspector.write(statusByteAddr, statusByte | status.getFlag(), WordSize.BYTE);
        } else {
            inspector.write(statusByteAddr, statusByte & (~status.getFlag()), WordSize.BYTE);
        }
    }
}
