package net.kkolyan.jhole2.monitoring;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class HistorySectionImpl implements HistorySection {
    private final String name;
    private ByteArrayOutputStream content = new ByteArrayOutputStream();

    public HistorySectionImpl(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContent() {
        return content.toString();
    }

    @Override
    public OutputStream getOutputStream() {
        return content;
    }
}
