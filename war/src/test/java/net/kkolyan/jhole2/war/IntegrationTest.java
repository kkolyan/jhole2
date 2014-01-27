package net.kkolyan.jhole2.war;

import net.kkolyan.jhole2.JHoleClient;
import net.kkolyan.jhole2.socket.SocketConnection;
import net.kkolyan.jhole2.utils.LineReader;
import net.kkolyan.jhole2.utils.StreamUtils;
import net.kkolyan.jhole2.war.dual.SessionManager;
import net.kkolyan.jhole2.war.dual.StreamingServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.HashSessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class IntegrationTest {
    Logger logger = LoggerFactory.getLogger(getClass());

    Random random = new Random();
    Server server;
    JHoleClient client;
    int serverPort;
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

    @Before
    public void init() throws Exception {
        serverPort = findFreePort();
        System.out.println("server started on port "+serverPort);
        server = new Server(serverPort);

        SessionManager sessionManager = new SessionManager();
        ServletContextHandler app = new ServletContextHandler();
        app.setContextPath("/");
        app.setSessionHandler(new SessionHandler(new HashSessionManager()));
        app.getSessionHandler().addEventListener(sessionManager);
        server.setHandler(app);
        app.addEventListener(sessionManager);
        app.addServlet(StreamingServlet.class, "/dual/*");

        server.start();

        client = new JHoleClient();
        client.setProxyPort(findFreePort());
        client.setServerBaseUrl("http://localhost:" + serverPort + "/");
//        client.setSkipComponentBasedConnector(true);
//        client.setSkipHttpDualChannel(true);
        client.start();
        client.joinBind();

    }

    @After
    public void destroy() throws Exception {
        logger.info("stopping client");
        client.stop();

        logger.info("stopping server");
        server.stop();
    }

    @Test
    public void test1() throws Exception {
        int service1Port = findFreePort();
        final ServerSocket service1 = new ServerSocket(service1Port);

        int length = 1024*8;
        int chunks = 8;

        byte[] requestStream = generateRandomly(length);
        byte[] responseStream = generateRandomly(length);

        List<ArraySegment> requests = splitRandomly(requestStream, chunks);
        final List<ArraySegment> responses = splitRandomly(responseStream, chunks);

        final ByteArrayOutputStream receivedResponses = new ByteArrayOutputStream();
        final ByteArrayOutputStream receivedRequests = new ByteArrayOutputStream();

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(5000);
                        logger.info("req: " + receivedRequests.size() + "; resp: " + receivedResponses.size());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Future<?> serviceActivities = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    final Socket socket = service1.accept();
                    Future<?> requestReceiving = executor.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                StreamUtils.pump(socket.getInputStream(), receivedRequests);
                                logger.info("request receiving finished");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    for (ArraySegment response : responses) {
                        socket.getOutputStream().write(response.array, response.offset, response.length);
                    }
                    socket.shutdownOutput();
                    requestReceiving.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final SocketConnection socket = new SocketConnection(new Socket("localhost", client.getProxyPort()));
        final LineReader reader = new LineReader(socket);
        socket.write(("CONNECT localhost:" + service1Port + " HTTP/1.1\r\n\r\n").getBytes());
        assertEquals("HTTP/1.1 200 Connection established", reader.readLine().trim());
        assertEquals("", reader.readLine().trim());

        Future<?> responseReceiving = executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    receivedResponses.write(reader.getRemainder());
                    StreamUtils.pump(reader.getStream(), receivedResponses);
                    logger.info("response receiving finished");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        for (ArraySegment request: requests) {
            socket.write(Arrays.copyOfRange(request.array, request.offset, request.offset + request.length));
        }
        socket.sendEof();
        responseReceiving.get();
        serviceActivities.get();

        assertArrayEquals(requestStream, receivedRequests.toByteArray());
        assertArrayEquals(responseStream, receivedResponses.toByteArray());


        service1.close();
    }

    private static final class ArraySegment {
        final byte[] array;
        final int offset;
        final int length;

        ArraySegment(byte[] array, int offset, int length) {
            this.array = array;
            this.offset = offset;
            this.length = length;
        }
    }

    private byte[] generateRandomly(int length) {
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    private List<ArraySegment> splitRandomly(byte[] array, int numberOfChunks) {
        int[] cutPlaces = new int[numberOfChunks + 1];
        cutPlaces[0] = 0;
        cutPlaces[numberOfChunks] = array.length;
        for (int i = 1; i < numberOfChunks - 1; i ++) {
            cutPlaces[i] = random.nextInt(array.length);
        }
        Arrays.sort(cutPlaces);

        List<ArraySegment> list = new ArrayList<ArraySegment>();
        for (int i = 1; i < cutPlaces.length; i ++) {
            list.add(new ArraySegment(array, cutPlaces[i-1], cutPlaces[i] - cutPlaces[i-1]));
        }
        return list;
    }
}
