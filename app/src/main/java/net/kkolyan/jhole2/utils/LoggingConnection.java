package net.kkolyan.jhole2.utils;

import net.kkolyan.jhole2.core.Connection;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author NPlekhanov
 */
public class LoggingConnection implements Connection {
    private Connection original;
    private OutputStream readLog;
    private OutputStream writeLog;

    public LoggingConnection(Connection original, OutputStream writeLog, OutputStream readLog) {
        this.original = original;
        this.readLog = readLog;
        this.writeLog = writeLog;
    }

    @Override
    public byte[] read() throws IOException {
        byte[] read = original.read();
        if (read != null) {
            readLog.write(read);
        }
        return read;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        writeLog.write(bytes);
        original.write(bytes);
    }

    @Override
    public void sendEof() throws IOException {
        original.sendEof();
    }

    @Override
    public void close() throws IOException {
        original.close();
    }

    @Override
    public String getDescription() {
        return original.getDescription();
    }
}
