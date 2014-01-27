package net.kkolyan.jhole2.http;

import net.kkolyan.jhole2.Credentials;
import net.kkolyan.jhole2.remoting.RawEndpoint;
import net.kkolyan.jhole2.utils.Disposable;
import net.kkolyan.jhole2.utils.Disposables;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthPolicy;
import org.apache.commons.httpclient.auth.AuthScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author NPlekhanov
 */
public class DualChannelHttpRawEndpoint implements RawEndpoint, Disposable {
    private Executor executor;
    private final BlockingQueue<ByteBuffer> messages = new ArrayBlockingQueue<ByteBuffer>(1024*1024);
    private final AtomicInteger counter = new AtomicInteger();
    private HttpReceiver receiver;
    private HttpSender sender;

    private ConcurrentHashMap<Integer,Pending> pendings = new ConcurrentHashMap<Integer, Pending>();
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void destroy() {
        Disposables.dispose(receiver, sender);
    }

    private static class Pending {
        ByteBuffer result;
        final long created = System.currentTimeMillis();
        long blocked;

        synchronized ByteBuffer getResult() {
            blocked = System.currentTimeMillis();
            try {
                while (result == null) {
                    wait();
                }
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            return result;
        }

        synchronized void setResult(ByteBuffer result) {
            this.result = result;
            notifyAll();
        }
    }

    public DualChannelHttpRawEndpoint(String baseUrl, Credentials credentials, Executor executor, long receiverTimeout) {
        this.executor = executor;
        HttpClient client = createHttpClient(credentials);
        receiver = new HttpReceiver(client, baseUrl + "dual/", new HttpReceiver.DataHandler() {
            @Override
            public void handleMessage(ByteBuffer message) {
                int requestId = message.getInt();
                Pending pending = pendings.remove(requestId);
                pending.setResult(message);

            }
        }, receiverTimeout);
        sender = new HttpSender(client, baseUrl + "dual/", messages);

        executor.execute(receiver);
        executor.execute(sender);
    }

    @Override
    public ByteBuffer executeRequest(ByteBuffer request) {
        Pending pending = new Pending();
        int requestId = counter.incrementAndGet();
        pendings.put(requestId, pending);

        int requestSize = request.remaining();
        ByteBuffer message = ByteBuffer.allocate(requestSize +4);
        message.putInt(requestId);
        message.put(request);
        message.flip();

        messages.offer(message);
        ByteBuffer result = pending.getResult();
        if (logger.isDebugEnabled()) {
            logger.debug("conversation finished: {} sent, {} received, {} ms", new Object[]{
                    requestSize, result.remaining(), System.currentTimeMillis() - pending.blocked
            });
        }
        return result;
    }

    public static HttpClient createHttpClient(Credentials credentials) {
        HttpClient client = new HttpClient();
        if (credentials != null) {
            client.getParams().setParameter(AuthPolicy.AUTH_SCHEME_PRIORITY, Arrays.asList(AuthPolicy.DIGEST, AuthPolicy.BASIC));
            client.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
                    credentials.getUsername(), credentials.getPassword()
            ));
        }
        client.setHttpConnectionManager(new MultiThreadedHttpConnectionManager());
        return client;
    }
}
