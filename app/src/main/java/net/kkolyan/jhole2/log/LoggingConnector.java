package net.kkolyan.jhole2.log;

import net.kkolyan.jhole2.core.Connection;
import net.kkolyan.jhole2.core.Connector;
import net.kkolyan.jhole2.utils.Address;

import java.io.IOException;

/**
 * @author nplekhanov
 */
public class LoggingConnector implements Connector {
    private Connector connector;
    private ApplicationLogger applicationLogger;

    public LoggingConnector(Connector connector, ApplicationLogger applicationLogger) {
        this.connector = connector;
        this.applicationLogger = applicationLogger;
    }

    @Override
    public Connection connect(Address address) throws IOException {
        Connection connection = connector.connect(address);
        ConnectionLogger connLogger = applicationLogger.logConnection(address.toString());
        connection = new LoggingConnectionWrapper(connection, connLogger);
        return connection;
    }
}
