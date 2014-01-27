package net.kkolyan.jhole2.utils;

import net.kkolyan.jhole2.core.Connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

public class StreamUtils {
    public static void pump(InputStream from, OutputStream to) throws IOException {
        byte[] bytes = new byte[1024];
        int pumped = 0;
        while (true) {
            int n = from.read(bytes);
            if (n < 0) {
                break;
            }
            pumped += n;
            to.write(bytes, 0, n);
        }
    }
    public static void pump(Connection from, OutputStream to) throws IOException {
        while (true) {
            byte[] bytes = from.read();
            if (bytes == null) {
                break;
            }
            to.write(bytes);
        }
    }

    public static String readAvailable(InputStream stream) throws IOException {
        byte[] bytes = new byte[stream.available()];
        int n = stream.read(bytes);
        return new String(bytes, 0, n);
    }

    public static boolean tryReadFullBuffer(ReadableByteChannel channel, ByteBuffer buf) throws IOException {
        try {
            while (buf.remaining() > 0) {
                if (channel.read(buf) < 0) {
                    return false;
                }
            }
            return true;
        } catch (IOException e) {
            if ("chunked stream ended unexpectedly".equals(e.getMessage())) {
                return false;
            }
            throw e;
        }
    }

    public static boolean tryReadFullBuffer(InputStream stream, byte[] buf) throws IOException {
        int position = 0;
        while (position < buf.length) {
            int n = stream.read(buf, position, buf.length - position);
            if (n < 0) {
                return false;
            }
            position += n;
        }
        return true;
    }
}
