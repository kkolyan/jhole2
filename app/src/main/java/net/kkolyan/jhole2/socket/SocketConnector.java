package net.kkolyan.jhole2.socket;

import net.kkolyan.jhole2.core.Connection;
import net.kkolyan.jhole2.core.Connector;
import net.kkolyan.jhole2.utils.Address;

import java.io.IOException;
import java.net.Socket;

/**
 * @author NPlekhanov
 */
public class SocketConnector implements Connector {
    @Override
    public Connection connect(Address address) throws IOException {
        Socket socket;
        try {
            socket = new Socket(address.getHost(), address.getPort());
        } catch (IOException e) {
            throw new IOException("failed to connect to "+address, e);
        }
        return new SocketConnection(socket);
    }
}
