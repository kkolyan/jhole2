package net.kkolyan.jhole2.core;

import java.io.IOException;

/**
 * @author NPlekhanov
 */
public interface Connection {
    byte[] read() throws IOException;
    void write(byte [] bytes) throws IOException;
    void sendEof() throws IOException;
    void close() throws IOException;
    String getDescription();
}
