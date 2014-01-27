package net.kkolyan.jhole2.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class LoggingOutputStream extends OutputStream {
    private OutputStream outputStream;
    private OutputStream log;

    public LoggingOutputStream(OutputStream outputStream, OutputStream log) {
        this.outputStream = outputStream;
        this.log = log;
    }

    @Override
    public void write(int b) throws IOException {
        outputStream.write(b);
        log.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        outputStream.write(b);
        log.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
        log.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }
}
