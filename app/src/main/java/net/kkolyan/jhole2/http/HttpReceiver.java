package net.kkolyan.jhole2.http;

import net.kkolyan.jhole2.utils.Disposable;
import net.kkolyan.jhole2.utils.StreamUtils;
import net.kkolyan.jhole2.utils.ThreadUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class HttpReceiver implements Runnable, Disposable {
    private static final Logger logger = LoggerFactory.getLogger(HttpReceiver.class);
    private HttpClient client;
    private String url;
    private DataHandler handler;
    private long timeout;
    private boolean destroyed;

    public HttpReceiver(HttpClient client, String url, DataHandler handler, long timeout) {
        this.client = client;
        this.url = url;
        this.handler = handler;
        this.timeout = timeout;
    }

    @Override
    public void destroy() {
        destroyed = true;
    }

    public interface DataHandler {
        void handleMessage(ByteBuffer message);
    }

    @Override
    public void run() {
        ThreadUtils.label("JHole HTTP Receiver");
        try {
            while (!destroyed) {
                try {
                    PostMethod post = new PostMethod(url
                            +"?timeout="
                            +timeout
                            +"&direction=downstream");

                    try {
                        logger.trace("sending request");
                        int status = client.executeMethod(post);
                        if (status / 100 != 2) {
                            logger.warn(""+post.getStatusLine());
                            Thread.sleep(1000);
                        } else {

                            while (true) {
                                ReadableByteChannel channel = Channels.newChannel(post.getResponseBodyAsStream());
                                ByteBuffer buf = ByteBuffer.allocate(4);
                                if (!StreamUtils.tryReadFullBuffer(channel, buf)) {
                                    break;
                                }
                                buf.flip();
                                int messageLength = buf.getInt();
                                ByteBuffer message = ByteBuffer.allocate(messageLength);
                                while (message.remaining() != 0) {
                                    channel.read(message);
                                }
                                message.flip();
                                logger.debug("received {} bytes", message.capacity());
                                handler.handleMessage(message);
                            }
                        }
                    } finally {
                        logger.trace("releasing connection");
                        post.releaseConnection();
                    }

                } catch (Exception e) {
                    logger.error(e.toString(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.toString(), e);
        } catch (Error e) {
            e.printStackTrace();
        }
    }

}
