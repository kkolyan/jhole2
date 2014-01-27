package net.kkolyan.jhole2.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * @author NPlekhanov
 */
public class ByteBufferUtils {
    public static final byte OBJECT_TYPE_CODE_NULL = 0;
    public static final byte OBJECT_TYPE_CODE_BYTE_ARRAY = 105;
    public static final byte OBJECT_TYPE_CODE_SERIALIZED = 106;
    public static final byte OBJECT_TYPE_CODE_OBJECT_ARRAY = 107;

    public static void putString(ByteBuffer b, String s) {
        byte[] bytes = s.getBytes(Charset.forName("utf8"));
        b.putInt(bytes.length);
        b.put(bytes);
    }

    public static String getString(ByteBuffer b) {
        int length = b.getInt();
        byte[] bytes = new byte[length];
        b.get(bytes);
        return new String(bytes, 0, length, Charset.forName("utf8"));
    }

    public static void putObject(final ByteBuffer buf, Object o) {
        if (o == null) {
            buf.put(OBJECT_TYPE_CODE_NULL);
        }
        else if (o instanceof byte[]) {
            buf.put(OBJECT_TYPE_CODE_BYTE_ARRAY);
            buf.putInt(((byte[]) o).length);
            buf.put((byte[]) o);
        }
        else if (o instanceof Throwable) {
            buf.put(OBJECT_TYPE_CODE_SERIALIZED);
            serialize(buf, o);
        }
        else if (o instanceof Object[]) {
            buf.put(OBJECT_TYPE_CODE_OBJECT_ARRAY);
            buf.putInt(((Object[]) o).length);
            for (Object ob: (Object[])o) {
                putObject(buf, ob);
            }
        }
        else if (o instanceof Number || o instanceof String || o instanceof Boolean || o instanceof Enum) {
            buf.put(OBJECT_TYPE_CODE_SERIALIZED);
            serialize(buf, o);
        }
        else throw new IllegalStateException();
    }

    public static Object getObject(final ByteBuffer buf) {
        byte typeCode = buf.get();
        if (typeCode == OBJECT_TYPE_CODE_NULL) {
            return null;
        }
        if (typeCode == OBJECT_TYPE_CODE_BYTE_ARRAY) {
            int length = buf.getInt();
            byte[] bytes = new byte[length];
            buf.get(bytes);
            return bytes;
        }
        if (typeCode == OBJECT_TYPE_CODE_SERIALIZED) {
            return deserialize(buf);
        }
        if (typeCode == OBJECT_TYPE_CODE_OBJECT_ARRAY) {
            int length = buf.getInt();
            Object[] objects = new Object[length];
            for (int i = 0; i < length; i ++) {
                objects[i] = getObject(buf);
            }
            return objects;
        }
        throw new IllegalArgumentException(typeCode+"");
    }

    private static void serialize(final ByteBuffer buf, Object o) {
        try {
            ObjectOutputStream stream = new ObjectOutputStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    buf.put(b, off, len);
                }
            });
            stream.writeObject(o);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Object deserialize(final ByteBuffer buf) {
        try {
            ObjectInputStream stream = new ObjectInputStream(new InputStream() {
                @Override
                public int read() throws IOException {
                    if (buf.remaining() == 0) {
                        return -1;
                    }
                    int b = buf.get();
                    if (b < 0) {
                        b += 256;
                    }
                    return b;
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    int n = Math.min(len, buf.remaining());
                    buf.get(b, off, n);
                    return n;
                }
            });
            return stream.readObject();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private static final int THREAD_LOCAL_BUFFERS_SIZE = 1024*1024;

    private static final ThreadLocal<ByteBuffer> buffers = new ThreadLocal<ByteBuffer>() {
        @Override
        protected ByteBuffer initialValue() {
            return ByteBuffer.allocate(THREAD_LOCAL_BUFFERS_SIZE);
        }
    };

    public static ByteBuffer getThreadLocalBuffer() {
        return buffers.get();
    }
}
