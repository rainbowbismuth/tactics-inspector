package rainbowbismuth.fft.view;

import rainbowbismuth.fft.TacticsInspector;
import rainbowbismuth.fft.WordSize;
import rainbowbismuth.fft.enums.Status;

public class StatusCTArray {
    public static final int SIZE = 0x10;

    private final TacticsInspector inspector;
    private final int address;

    public StatusCTArray(TacticsInspector inspector, int address) {
        this.inspector = inspector;
        this.address = address;
    }

    public int getCT(final Status status) {
        return inspector.readByte(address + status.getCTOffset());
    }

    public void setCT(final Status status, final int value) {
        inspector.write(address + status.getCTOffset(), value, WordSize.BYTE);
    }
}
