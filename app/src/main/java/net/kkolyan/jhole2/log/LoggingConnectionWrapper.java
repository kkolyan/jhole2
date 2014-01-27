package net.kkolyan.jhole2.log;

import net.kkolyan.jhole2.core.Connection;

import java.io.IOException;

/**
 * @author nplekhanov
 */
public class LoggingConnectionWrapper implements Connection {
    private Connection origin;
    private ConnectionLogger connLogger;

    public LoggingConnectionWrapper(Connection origin, ConnectionLogger connLogger) {
        this.origin = origin;
        this.connLogger = connLogger;
    }

    @Override
    public byte[] read() throws IOException {
        byte[] bytes;
        try {
            bytes = origin.read();
        } catch (IOException e) {
            connLogger.logClose("in");
            throw e;
        }

        if (bytes == null) {
            connLogger.logEof("in");
        } else {
            connLogger.logTransfer(bytes, "in");
        }
        return bytes;
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        connLogger.logTransfer(bytes, "out");
        try {
            origin.write(bytes);
        } catch (IOException e) {
            connLogger.logClose("in");
            throw e;
        }
    }

    @Override
    public void sendEof() throws IOException {
        connLogger.logEof("out");
        origin.sendEof();
    }

    @Override
    public void close() throws IOException {
        connLogger.logClose("out");
        origin.close();
    }

    @Override
    public String getDescription() {
        return origin.getDescription();
    }
}
