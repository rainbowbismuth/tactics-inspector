package rainbowbismuth.fft;

import java.util.ArrayList;
import java.util.List;

/**
 * Read strings from tactic's memory.
 */
public class CharacterSet {
    public static final int SPACE = 0xFA;
    public static final int END_OF_STRING = 0xFE;
    /**
     * A default instance of CharacterSet.
     */
    public static final CharacterSet INSTANCE = new CharacterSet();
    /**
     * Only handling the first part of the character set right now.
     */
    private final static String MAPPING = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!�?�+�/�:";
    private final char[] mapping;

    protected CharacterSet() {
        mapping = new char[MAPPING.length()];
        for (int i = 0; i < MAPPING.length(); i++) {
            mapping[i] = MAPPING.charAt(i);
        }
    }

    public char get(final int i) {
        if (i == SPACE) {
            return ' ';
        }
        if (i >= mapping.length) {
            return '�';
        }
        return mapping[i];
    }

    /**
     * Read a string, up to and including a final byte.
     */
    public String read(final byte[] memory, final int firstByte, final int lastByteInclusive) {
        final StringBuilder builder = new StringBuilder();
        for (int i = firstByte; i <= lastByteInclusive; i++) {
            final int charByte = Byte.toUnsignedInt(memory[i]);
            if (charByte == END_OF_STRING) {
                break;
            }
            builder.append(CharacterSet.INSTANCE.get(charByte));
        }
        return builder.toString();
    }

    /**
     * Read a table of strings that are laid out one after the other's END_OF_STRING marker.
     */
    public List<String> readTable(final byte[] memory, final int firstByte, final int lastByteInclusive) {
        final List<String> table = new ArrayList<>();
        int curByte = firstByte;
        while (curByte <= lastByteInclusive) {
            final String str = read(memory, curByte, lastByteInclusive);
            table.add(str);
            curByte += str.length() + 1;
        }
        return table;
    }
}
