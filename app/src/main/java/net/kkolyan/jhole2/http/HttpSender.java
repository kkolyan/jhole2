package net.kkolyan.jhole2.http;

import net.kkolyan.jhole2.utils.Disposable;
import net.kkolyan.jhole2.utils.ThreadUtils;
import org.apache.commons.httpclient.ChunkedOutputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class HttpSender implements Runnable, Disposable {
    private static final Logger logger = LoggerFactory.getLogger(HttpSender.class);
    private HttpClient client;
    private String url;
    private BlockingQueue<ByteBuffer> source;
    private boolean destroyed;

    public HttpSender(HttpClient client, String url, BlockingQueue<ByteBuffer> source) {
        this.client = client;
        this.url = url;
        this.source = source;
    }

    @Override
    public void run() {
        ThreadUtils.label("JHole HTTP Sender");
        try {
            while (!destroyed) {
                try {
                    PostMethod post = new PostMethod(url+"?direction=upstream");
                    post.setRequestEntity(new RequestEntity() {

                        @Override
                        public boolean isRepeatable() {
                            return false;
                        }

                        @Override
                        public void writeRequest(OutputStream out) throws IOException {
                            try {
                                Field cache = ChunkedOutputStream.class.getDeclaredField("cache");
                                cache.setAccessible(true);
                                cache.set(out, new byte[16]);
                            } catch (Exception e) {
                                throw new IllegalStateException(e);
                            }

                            while (true) {
                                ByteBuffer message;
                                try {
                                    message = source.poll(5000, TimeUnit.MILLISECONDS);
                                } catch (InterruptedException e) {
                                    throw new IllegalStateException(e);
                                }
                                if (message == null) {
                                    break;
                                }
                                WritableByteChannel channel = Channels.newChannel(out);
                                int remaining = message.remaining();
                                if (remaining < 0) {
                                    throw new IllegalStateException(""+remaining);
                                }
                                channel.write((ByteBuffer) ByteBuffer.allocate(4).putInt(remaining).flip());
                                channel.write(message);
                                logger.debug("sent {} bytes", message.capacity());
                            }
                        }

                        @Override
                        public long getContentLength() {
                            return -1;
                        }

                        @Override
                        public String getContentType() {
                            return "application/octet-stream";
                        }
                    });

                    logger.trace("sending request");
                    try {
                        int status = client.executeMethod(post);
                        if (status / 100 != 2) {
                            logger.warn(""+post.getStatusLine());
                            Thread.sleep(1000);
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

    @Override
    public void destroy() {
        destroyed = true;
    }
}
