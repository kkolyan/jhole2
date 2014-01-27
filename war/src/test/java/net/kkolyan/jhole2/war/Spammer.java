package net.kkolyan.jhole2.war;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class Spammer {
    public static void main(String[] args) throws IOException, InterruptedException {

        Socket socket = new Socket("localhost", 8085);
        Thread.sleep(10000);
    }

    public static void spam(Socket socket) throws IOException {
        Random random = new Random();

        for (int i = 0; i < 255; i ++) {
            byte[] bytes = new byte[1024*1024];
            random.nextBytes(bytes);
            socket.getOutputStream().write(bytes);
        }
        socket.shutdownOutput();
    }
}
