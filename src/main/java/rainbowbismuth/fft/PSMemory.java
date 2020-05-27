package rainbowbismuth.fft;

public interface PSMemory {
    int RAM_SIZE = 1024 * 1024 * 2;

    void read(byte[] buffer) throws PSMemoryReadException;

    void write(long address, byte[] memory) throws PSMemoryWriteException;
}
