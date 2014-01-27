package net.kkolyan.jhole2;

import net.kkolyan.jhole2.core.Connection;
import net.kkolyan.jhole2.core.ConnectionProcessor;
import net.kkolyan.jhole2.core.Connector;
import net.kkolyan.jhole2.http.DualChannelHttpRawEndpoint;
import net.kkolyan.jhole2.log.H2ApplicationLogger;
import net.kkolyan.jhole2.log.LoggingConnectionWrapper;
import net.kkolyan.jhole2.log.LoggingConnector;
import net.kkolyan.jhole2.monitoring.MonitoringHolder;
import net.kkolyan.jhole2.remoting.ComponentBasedConnector;
import net.kkolyan.jhole2.remoting.LocalRawEndpoint;
import net.kkolyan.jhole2.remoting.RawEndpointBackedComponentManager;
import net.kkolyan.jhole2.socket.SocketConnection;
import net.kkolyan.jhole2.socket.SocketConnector;
import net.kkolyan.jhole2.utils.Disposables;
import net.kkolyan.jhole2.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author NPlekhanov
 */
public class JHoleClient {

    private int consolePort = 8001;
    private int proxyPort = 8000;
    private String serverBaseUrl;
    private Credentials credentials;
    private long serverDownstreamTimeout = 500;
    private boolean skipHttpDualChannel;
    private boolean skipComponentBasedConnector;
    private ExecutorService executor;

    private final Object lock = new Object();
    private ServerSocket acceptor;
    private static final Logger logger = LoggerFactory.getLogger(JHoleClient.class);
    private Connector connector;
    private String onlyHosts;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.getProperties().load(JHoleClient.class.getClassLoader().getResourceAsStream("config.properties"));

        if (Boolean.getBoolean("jhole.monitoring.enabled")) {
            MonitoringHolder.enableMonitoring();
        }
        JHoleClient client = new JHoleClient();
        try {
            client.setConsolePort(Integer.getInteger("jhole.client.console.port", client.getConsolePort()));
            client.setProxyPort(Integer.getInteger("jhole.client.port", client.getProxyPort()));
            client.setServerBaseUrl(System.getProperty("jhole.server.baseUrl"));
            client.setCredentials(Boolean.getBoolean("jhole.server.auth") ? new Credentials(
                    System.getProperty("jhole.server.auth.username"),
                    System.getProperty("jhole.server.auth.password")) : null);
            client.setOnlyHosts(System.getProperty("jhole.client.onlyHosts"));
            client.setServerDownstreamTimeout(Long.getLong("jhole.server.downstream.timeout", client.getServerDownstreamTimeout()));
            client.start();

            client.joinClose();
        } catch (Exception e) {
            logger.error(e.toString(), e);
        } finally {
            client.stop();
        }
    }

    public void joinBind() throws InterruptedException {
    }

    public void joinClose() throws InterruptedException {
        synchronized (lock) {
            while (!acceptor.isClosed()) {
                lock.wait();
            }
        }
    }

    public void start() throws IOException {
        final H2ApplicationLogger applicationLogger = new H2ApplicationLogger();

        if (getServerBaseUrl() == null) {
            throw new IllegalStateException("serverBaseUrl required");
        }

        executor = Executors.newCachedThreadPool();
        Console2.launch(executor, getConsolePort(), applicationLogger);

        TrayStatus.setup();
        acceptor = new ServerSocket(getProxyPort());

        if (skipComponentBasedConnector) {
            connector = new SocketConnector();
        }
        else if (skipHttpDualChannel) {
            connector = new ComponentBasedConnector(
                    new RawEndpointBackedComponentManager(
                            new LocalRawEndpoint("", applicationLogger)));
        }
        else {
            connector = new ComponentBasedConnector(
                    new RawEndpointBackedComponentManager(
                            new DualChannelHttpRawEndpoint(
                                    getServerBaseUrl(),
                                    getCredentials(),
                                    executor, getServerDownstreamTimeout())));
        }
        connector = new LoggingConnector(connector, applicationLogger);
        if (onlyHosts != null) {
            connector = new DispatchingConnector(new SocketConnector()).addRule(onlyHosts, connector);
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ThreadUtils.label("JHole Connection Acceptor");
                    try {
                        while (true) {
                            Socket socket = acceptor.accept();
                            Connection connection = new SocketConnection(socket);
                            connection = new LoggingConnectionWrapper(connection, applicationLogger.logConnection("Local"));
                            executor.execute(new ConnectionProcessor(connection, connector, executor));
                        }
                    } finally {
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                } catch (Throwable e) {
                    logger.error(e.toString(), e);
                }
            }
        });
    }

    public void stop() {
        Disposables.dispose(connector);
        executor.shutdown();
    }

    public int getConsolePort() {
        return consolePort;
    }

    public void setConsolePort(int consolePort) {
        this.consolePort = consolePort;
    }

    public int getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getServerBaseUrl() {
        return serverBaseUrl;
    }

    public void setServerBaseUrl(String serverBaseUrl) {
        this.serverBaseUrl = serverBaseUrl;
    }

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public long getServerDownstreamTimeout() {
        return serverDownstreamTimeout;
    }

    public void setServerDownstreamTimeout(long serverDownstreamTimeout) {
        this.serverDownstreamTimeout = serverDownstreamTimeout;
    }

    public boolean isSkipHttpDualChannel() {
        return skipHttpDualChannel;
    }

    public void setSkipHttpDualChannel(boolean skipHttpDualChannel) {
        this.skipHttpDualChannel = skipHttpDualChannel;
    }

    public boolean isSkipComponentBasedConnector() {
        return skipComponentBasedConnector;
    }

    public void setSkipComponentBasedConnector(boolean skipComponentBasedConnector) {
        this.skipComponentBasedConnector = skipComponentBasedConnector;
    }

    public void setOnlyHosts(String onlyHosts) {
        this.onlyHosts = onlyHosts;
    }
}
