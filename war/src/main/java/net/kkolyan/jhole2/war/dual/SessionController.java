package net.kkolyan.jhole2.war.dual;

import net.kkolyan.jhole2.remoting.RawEndpoint;
import net.kkolyan.jhole2.utils.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author NPlekhanov
 */
public class SessionController {

    private ExecutorService executor = Executors.newCachedThreadPool();
    private BlockingQueue<Message> downstreamQueue = new ArrayBlockingQueue<Message>(1024*64);
    private RawEndpoint requestService;

    private Logger logger = LoggerFactory.getLogger(getClass());

    public SessionController(RawEndpoint requestService) {
        this.requestService = requestService;
    }

    private void addResponse(int requestId, Message message) {
        message.response = ByteBuffer.allocate(message.data.remaining()+4);
        message.response.putInt(requestId);
        message.response.put(message.data);
        message.response.flip();
        downstreamQueue.offer(message);
    }

    public void handleUpstream(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ReadableByteChannel channel = Channels.newChannel(req.getInputStream());
        while (true) {
            ByteBuffer buf = ByteBuffer.allocate(4);
            if (!StreamUtils.tryReadFullBuffer(channel, buf)) {
                break;
            }
            buf.flip();
            int length = buf.getInt();
            final ByteBuffer request = ByteBuffer.allocate(length);
            while (request.remaining() > 0) {
                channel.read(request);
            }
            request.flip();

            logger.debug("received {} bytes", request.remaining());

            final Message message = new Message();
            message.request = request;
            message.enqueuedForExecution = System.currentTimeMillis();

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    message.dequeuedForExecution = System.currentTimeMillis();
                    int requestId = request.getInt();
                    message.data = requestService.executeRequest(request);

                    message.enqueuedForWrite = System.currentTimeMillis();
                    addResponse(requestId, message);
                }
            });
        }
    }

    private static class Message {
        ByteBuffer data;
        ByteBuffer request;
        ByteBuffer response;
        long enqueuedForExecution;
        long dequeuedForExecution;
        long enqueuedForWrite;
        public long dequeuedForWriting;
    }

    public void handleDownstream(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WritableByteChannel channel = Channels.newChannel(response.getOutputStream());
        long timeout = Long.parseLong(request.getParameter("timeout"));

        while (true) {
            Message message;
            try {
                message = downstreamQueue.poll(timeout, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
            if (message == null) {
                break;
            }
            message.dequeuedForWriting = System.currentTimeMillis();

            logger.debug("sent {} bytes", message.response.remaining());

            channel.write((ByteBuffer) ByteBuffer.allocate(4).putInt(message.response.remaining()).flip());
            channel.write(message.response);
            response.flushBuffer();

            long now = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("conversation finished: total {}, queue1 {}, execution {}, queue2 {}, write {}. w/r {}/{}", new Object[] {
                        now - message.enqueuedForExecution,
                        message.dequeuedForExecution - message.enqueuedForExecution,
                        message.enqueuedForWrite - message.dequeuedForExecution,
                        message.dequeuedForWriting - message.enqueuedForWrite,
                        now - message.dequeuedForWriting,
                        message.response.capacity(),
                        message.request.capacity()
                });
            }
        }
    }

    public void destroy() {
        executor.shutdown();
    }

    public RawEndpoint getRequestService() {
        return requestService;
    }
}
