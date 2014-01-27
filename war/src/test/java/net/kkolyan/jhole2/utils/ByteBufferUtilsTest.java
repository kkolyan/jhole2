package net.kkolyan.jhole2.utils;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class ByteBufferUtilsTest {

    @Test
    public void testSerializedSize() {
        ByteBuffer buf = ByteBuffer.allocate(1024*1024*65);
        byte[] bytes = new byte[1024*1024];
        new Random().nextBytes(bytes);
        ByteBufferUtils.putObject(buf, bytes);
        buf.flip();
        System.out.println(buf.limit() - bytes.length);

        buf = ByteBuffer.allocate(100);

        ByteBufferUtils.putObject(buf, "Hello".getBytes());
        System.out.println("["+new String(buf.array())+"]");
        buf.flip();
        System.out.println(buf.limit() - "Hello".getBytes().length);
    }
}
