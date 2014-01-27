package net.kkolyan.jhole2.war;

import net.kkolyan.jhole2.utils.StreamUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class TestConnect {
    ExecutorService executor = Executors.newCachedThreadPool();

    private int findFreePort() {
        for (int port = 8080; port < 65000; port ++) {
            try {
                ServerSocket ss = new ServerSocket(port);
                ss.close();
                return port;
            } catch (IOException e) {
                //
            }
        }
        throw new IllegalStateException("can't find free port to bind test server");
    }

    @Test
    public void test1() throws IOException {
        int serverPort = findFreePort();
        final ServerSocket serverSocket = new ServerSocket(serverPort);
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        notifyAll();
                    }
                    Socket socket = serverSocket.accept();
                    PrintStream writer = new PrintStream(socket.getOutputStream(), true);
                    writer.println("fuck yeah");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Socket socket = new Socket("localhost", serverPort);
        PrintStream writer = new PrintStream(socket.getOutputStream(), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer.println("CONNECT localhost:12355");
        writer.println();
        assertEquals("fuck yeah", reader.readLine());
    }

    @Test
    public void testEof() throws IOException, ExecutionException, InterruptedException {
        final ServerSocket serverSocket = new ServerSocket(findFreePort());
        final ByteArrayOutputStream receivedRequests = new ByteArrayOutputStream();
        final AtomicLong sent = new AtomicLong();
        Future<?> serverActivity = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Socket socket = serverSocket.accept();
                    StreamUtils.pump(socket.getInputStream(), receivedRequests);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Random random = new Random();

        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        System.out.println(receivedRequests.size()+" bytes received, "+sent+" sent");
                        Thread.sleep(1000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Socket socket = new Socket("localhost", serverSocket.getLocalPort());

        for (int i = 0; i < 500; i ++) {
            byte[] bytes = new byte[1024*1024];
            random.nextBytes(bytes);
            socket.getOutputStream().write(bytes);
            sent.addAndGet(bytes.length);
        }
        socket.shutdownOutput();
        System.out.println("written");
        serverActivity.get();
    }
}
