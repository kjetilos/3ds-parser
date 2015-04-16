package no.myke.parser;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * This reader uses an input stream as the source for bytes. This is not the
 * preferred way of reading the mode as the MapReader performs better. But it
 * is included since some api's like the android asset api provides only
 * InputStream sources.
 *
 * @author Kjetil Østerås
 */
public class StreamReader implements TypeReader {

    private BufferedInputStream stream;
    private int position = 0;

    public StreamReader(BufferedInputStream stream) {
        this.stream = stream;
    }

    public StreamReader(InputStream stream) {
        this.stream = new BufferedInputStream(stream);
    }

    public short getShort() throws IOException {
        byte b0 = getByte();
        byte b1 = getByte();
        return makeShort(b1, b0);
    }

    public int getInt() throws IOException {
        byte b0 = getByte();
        byte b1 = getByte();
        byte b2 = getByte();
        byte b3 = getByte();
        return makeInt(b3, b2, b1, b0);
    }

    public float getFloat() throws IOException {
        return Float.intBitsToFloat(getInt());
    }

    private byte getByte() throws IOException {
        int read = stream.read();
        if (read == -1) {
            throw new EOFException();
        }
        position++;
        return (byte) read;
    }

    public void skip(int i) throws IOException {
        int skipped = 0;
        do {
            skipped += stream.skip(i - skipped);
        } while (skipped < i);

        position += i;
    }

    public String readString() throws IOException {
        StringBuilder sb = new StringBuilder(64);
        byte ch = getByte();
        while (ch != 0) {
            sb.append((char)ch);
            ch = getByte();
        }
        return sb.toString();
    }

    public int position() {
        return position;
    }

    static private int makeInt(byte b3, byte b2, byte b1, byte b0) {
        return (((b3       ) << 24) |
                ((b2 & 0xff) << 16) |
                ((b1 & 0xff) <<  8) |
                ((b0 & 0xff)      ));
    }

    static private short makeShort(byte b1, byte b0) {
        return (short)((b1 << 8) | (b0 & 0xff));
    }

}
