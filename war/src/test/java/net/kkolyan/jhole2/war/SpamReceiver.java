package net.kkolyan.jhole2.war;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class SpamReceiver {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8085);
        while (true) {
            Socket socket = serverSocket.accept();
            receiveSpam("SpamReceiver", socket);
        }
    }

    public static void receiveSpam(String logPrefix, Socket socket) throws IOException {
        int total = 0;
        System.out.println(socket);
        byte[] bytes = new byte[65*1024];
        while (true) {
            int n = socket.getInputStream().read(bytes);
            if (n < 0) {
                break;
            }
            total += n;
            System.out.println(logPrefix+total);
        }
        System.out.println(logPrefix+"EOF");
        System.out.println();
    }
}
