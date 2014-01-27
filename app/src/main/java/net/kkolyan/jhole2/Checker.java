package net.kkolyan.jhole2;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author nplekhanov
 */
public class Checker {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("kkolyan.net", 8080);
        PrintStream writer = new PrintStream(socket.getOutputStream(), true);
        writer.println("GET / HTTP/1.1");
        writer.println("Host: kkolyan.net");
        writer.println();
//        socket.shutdownOutput();

        Scanner scanner = new Scanner(socket.getInputStream());
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.trim().isEmpty()) {
//                break;
            }
            System.out.println(line);
        }
    }
}
