package net.kkolyan.jhole2.war;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class DualSelfSpammer {
    public static void main(String[] args) throws IOException, InterruptedException {
        final ServerSocket serverSocket = new ServerSocket(8085);
        new Thread() {
            @Override
            public void run() {
                try {
                    final Socket socket = serverSocket.accept();
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                SpamReceiver.receiveSpam("server: ", socket);
                            } catch (IOException e) {
                                throw new IllegalStateException(e);
                            }
                        }
                    }.start();
                    Spammer.spam(socket);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }.start();

        final Socket socket = new Socket("localhost", 8085);
        new Thread() {
            @Override
            public void run() {
                try {
                    SpamReceiver.receiveSpam("client: ", socket);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
        }.start();
        Spammer.spam(socket);

        Thread.sleep(10000);
    }
}
