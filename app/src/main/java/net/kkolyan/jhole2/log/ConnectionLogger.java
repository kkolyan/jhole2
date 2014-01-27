package net.kkolyan.jhole2.log;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author nplekhanov
 */
public interface ConnectionLogger {

    void logClose(String direction);
    void logTransfer(byte[] bytes, String direction);
    void logEof(String direction);
    void logException(Exception ex);
}
