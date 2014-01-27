package net.kkolyan.jhole2.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LoggingInputStream extends InputStream {
    private final InputStream inputStream;
    private final OutputStream log;

    public LoggingInputStream(InputStream inputStream, OutputStream log) {
        this.inputStream = inputStream;
        this.log = log;
    }

    @Override
    public int read() throws IOException {
        int value = inputStream.read();
        if (value >= 0) {
            log.write(value);
        }
        return value;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int n = inputStream.read(b);
        if (n > 0) {
            log.write(b, 0, n);
        }
        return n;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = inputStream.read(b, off, len);
        if (n > 0) {
            log.write(b, off, n);
        }
        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        if (n > 0) {
            throw new UnsupportedOperationException();
        }
        return inputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return inputStream.available();
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    @Override
    public void mark(int readlimit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void reset() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

}
