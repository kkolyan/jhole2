package net.kkolyan.jhole2.monitoring;

import java.io.OutputStream;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public interface HistorySection {
    String getName();
    String getContent();
    OutputStream getOutputStream();
}
