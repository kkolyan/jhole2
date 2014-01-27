package net.kkolyan.jhole2.core;

import net.kkolyan.jhole2.log.ApplicationLogger;
import net.kkolyan.jhole2.log.LoggingConnectionWrapper;
import net.kkolyan.jhole2.utils.Address;
import net.kkolyan.jhole2.utils.HttpRequest;
import net.kkolyan.jhole2.utils.LineReader;
import net.kkolyan.jhole2.utils.ThreadUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executor;

public class ConnectionProcessor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ConnectionProcessor.class);
    private Connection origin;
    private Connector outgoingConnector;
    private Executor executor;

    public ConnectionProcessor(Connection origin, Connector outgoingConnector, Executor executor) {
        this.origin = origin;
        this.outgoingConnector = outgoingConnector;
        this.executor = executor;
    }

    @Override
    public void run() {
        ThreadUtils.label("JHole Connection Processor " + origin.getDescription());

        try {

            final LineReader originReader = new LineReader(origin);

            HttpRequest request;
            try {
                request = HttpRequest.parseRequest(originReader);
            } catch (Exception e) {
                respondSilently(origin, "400 Bad Request", null);
                throw e;
            }

            try {
                if (request.getMethod().equals("CONNECT")) {
                    final Address address = Address.parseAddress(request.getAddress());

                    Connection destination = outgoingConnector.connect(address);

                    origin.write("HTTP/1.1 200 Connection established\r\n\r\n".getBytes());

                    logger.info("connection established: {}", address);


                    executor.execute(new ConnectionGlue(destination, origin,
                            "JHole Outgoing Reader " + origin.getDescription() + " <=> " + address));

                    executor.execute(new ConnectionGlue(origin, destination,
                            originReader.getRemainder(),
                            "JHole Outgoing Writer " + origin.getDescription() + " <=> "+address));

                } else {
                    respondSilently(origin, "405 Method Not Allowed", null);
                }
            } catch (IOException e) {
                respondSilently(origin, "500 Internal Server Error", e.getMessage());
            } catch (Exception e) {
                respondSilently(origin, "500 Internal Server Error", null);
                throw e;
            }

        } catch (Exception e) {
            logger.error(e.toString(), e);
        } catch (Error e) {
            e.printStackTrace();
        }
    }

    private void respondSilently(Connection outputStream, String status, String details) {
        try {
            outputStream.write(("HTTP/1.1 " + status + "\r\n\r\n").getBytes());
            if (details != null) {
                outputStream.write(details.getBytes("utf8"));
            }
            outputStream.close();
        } catch (Exception e) {
            //
        }
    }

}
