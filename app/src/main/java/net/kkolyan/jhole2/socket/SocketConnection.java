package net.kkolyan.jhole2.socket;

import net.kkolyan.jhole2.core.Connection;
import net.kkolyan.jhole2.remoting.Mortal;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

/**
 * @author NPlekhanov
 */
public class SocketConnection implements Connection, Mortal {
    private Socket socket;
    private static final ThreadLocal<byte[]> inputBuffers = new ThreadLocal<byte[]>() {
        @Override
        protected byte[] initialValue() {
            return new byte[1024*64];
        }
    };

    public SocketConnection(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public byte[] read() throws IOException {
        byte [] bytes = inputBuffers.get();
        int n = socket.getInputStream().read(bytes);
        if (n < 0) {
            return null;
        }
        return Arrays.copyOf(bytes, n);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        socket.getOutputStream().write(bytes);
    }

    @Override
    public void sendEof() throws IOException {
        socket.shutdownOutput();
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }

    @Override
    public String getDescription() {
        return socket.toString();
    }

    @Override
    public String toString() {
        return "SocketConnection{" +
                "socket=" + socket +
                '}';
    }

    @Override
    public boolean isDead() {
        return socket.isConnected() && socket.isClosed();
    }
}
