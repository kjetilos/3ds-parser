package no.myke.parser;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * A reader which maps the entire file into memory. This is the reader which
 * performs best with +8KB models. It will use the same amount of memory as
 * the size of the model so that is the tradeof from being fast.
 *
 * @author Kjetil Østerås
 */
public class MapReader implements TypeReader {

    private MappedByteBuffer buffer;

    public MapReader(FileChannel channel) throws IOException {
        buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        buffer.order(ByteOrder.LITTLE_ENDIAN);
    }

    public short getShort() throws IOException {
        return buffer.getShort();
    }

    public int getInt() throws IOException {
        return buffer.getInt();
    }

    public float getFloat() throws IOException {
        return buffer.getFloat();
    }

    public void skip(int i) throws IOException {
        buffer.position(buffer.position() + i);
    }

    public String readString() throws IOException {
        StringBuilder sb = new StringBuilder(256);
        byte ch = buffer.get();
        while (ch != 0) {
            sb.append((char)ch);
            ch = buffer.get();
        }
        return sb.toString();
    }

    public int position() {
        return buffer.position();
    }

}
