package net.kkolyan.jhole2.core;

import net.kkolyan.jhole2.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
* @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
*/
public class ConnectionGlue implements Runnable {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Connection origin;
    private Connection destination;
    private String threadLabel;
    private byte[] beginning;

    public ConnectionGlue(Connection origin, Connection destination, byte[] beginning, String threadLabel) {
        this.origin = origin;
        this.destination = destination;
        this.beginning = beginning;
        this.threadLabel = threadLabel;
    }

    public ConnectionGlue(Connection origin, Connection destination, String threadLabel) {
        this.origin = origin;
        this.destination = destination;
        this.threadLabel = threadLabel;
    }

    @Override
    public void run() {
        ThreadUtils.label(threadLabel);
        try {
            while (true) {
                byte[] bytes;
                try {
                    if (beginning != null) {
                        bytes = beginning;
                        beginning = null;
                    } else {
                        bytes = origin.read();
                    }
                } catch (IOException e) {
                    destination.close();
                    break;
                }
                if (bytes == null) {
                    destination.sendEof();
                    break;
                }
                try {
                    destination.write(bytes);
                } catch (IOException e) {
                    origin.close();
                }
            }
        } catch (Exception e) {
            logger.error(e.toString(), e);
        }
    }
}
