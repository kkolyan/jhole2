package net.kkolyan.jhole2.utils;

import net.kkolyan.jhole2.core.Connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
* @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
*/
public class LineReader {
    private Connection stream;
    private byte[] remainder = new byte[0];

    public byte[] getRemainder() {
        return remainder;
    }

    public Connection getStream() {
        return stream;
    }

    public LineReader(Connection stream) {
        this.stream = stream;
    }

    public String readLine() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        for (int i = 0; i < remainder.length; i ++) {
            if (remainder[i] == '\n') {
                bos.write(remainder, 0, i);
                remainder = Arrays.copyOfRange(remainder, i + 1, remainder.length);
                return bos.toString("utf8");
            }
        }
        bos.write(remainder);
        remainder = new byte[0];

        while (true) {
            byte[] bytes = stream.read();
            if (bytes == null) {
                if (bos.size() > 0) {
                    return bos.toString("utf8");
                }
                throw new IOException("EOF");
            }
            for (int i = 0; i < bytes.length; i ++) {
                if (bytes[i] == '\n') {
                    bos.write(bytes, 0, i);
                    remainder = Arrays.copyOfRange(bytes, i + 1, bytes.length);
                    return bos.toString("utf8");
                }
            }
            bos.write(bytes);
        }
    }
}
