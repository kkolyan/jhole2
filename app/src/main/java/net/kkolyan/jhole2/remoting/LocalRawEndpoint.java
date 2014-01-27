package net.kkolyan.jhole2.remoting;

import net.kkolyan.jhole2.log.ApplicationLogger;
import net.kkolyan.jhole2.log.LoggingConnectionWrapper;
import net.kkolyan.jhole2.utils.Address;
import net.kkolyan.jhole2.utils.ByteBufferUtils;
import net.kkolyan.jhole2.socket.SocketConnection;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author NPlekhanov
 */
public class LocalRawEndpoint implements RawEndpoint {
    private Map<Integer,Component> components = new ConcurrentHashMap<Integer, Component>();
    private final AtomicInteger counter = new AtomicInteger();
    private Object sessionName;
    private ApplicationLogger applicationLogger;

    public LocalRawEndpoint(Object sessionName, ApplicationLogger applicationLogger) {
        this.sessionName = sessionName;
        this.applicationLogger = applicationLogger;
    }

    public Map<Integer, Component> getComponents() {
        return components;
    }

    public static class Component {
        Object instance;
        Class<?> type;
    }

    public void clean() {
        for (Map.Entry<Integer, Component> entry: components.entrySet()) {
            if (entry.getValue() instanceof Mortal && ((Mortal) entry.getValue()).isDead()) {
                components.remove(entry.getKey());
            }
        }
    }

    protected Object createComponentInstance(Class<?> type, String descriptor, int componentId) {
        if (descriptor.startsWith("connection:tcp:")) {
            Address address = Address.parseAddress(descriptor.substring("connection:tcp:".length()));
            try {
                SocketConnection connection = new SocketConnection(new Socket(address.getHost(), address.getPort()));
                return new LoggingConnectionWrapper(connection, applicationLogger.logConnection(sessionName+": "+address));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        throw new IllegalArgumentException(descriptor);
    }

    @Override
    public ByteBuffer executeRequest(ByteBuffer request) {
        int actionId = request.getInt();
        if (actionId == RawEndpointBackedComponentManager.REQUEST_ID_CREATE_COMPONENT) {
            return createComponent(request);
        }
        if (actionId == RawEndpointBackedComponentManager.REQUEST_ID_INVOKE_COMPONENT) {
            return invokeComponent(request);
        }
        throw new IllegalArgumentException(""+actionId);
    }

    private ByteBuffer invokeComponent(ByteBuffer request) {
        int componentId = request.getInt();
        int methodCode = request.getInt();
        Component component = components.get(componentId);
        if (component == null) {
            throw new IllegalArgumentException(""+componentId);
        }
        Method method = component.type.getMethods()[methodCode];
        Object[] args = (Object[]) ByteBufferUtils.getObject(request);
        boolean success;
        Object result;
        try {
            result = method.invoke(component.instance, args);
            success = true;
        } catch (Exception e) {
            result = e;
            success = false;
        }
        ByteBuffer response = ByteBufferUtils.getThreadLocalBuffer();
        response.clear();

        response.put((byte) (success ? 1 : 0));
        if (!success || method.getReturnType() != void.class) {
            ByteBufferUtils.putObject(response, result);
        }
        response.flip();
        return response;
    }

    private ByteBuffer createComponent(ByteBuffer request) {
        String typeName = ByteBufferUtils.getString(request);
        String descriptor = ByteBufferUtils.getString(request);

        Component component = new Component();
        try {
            component.type = Class.forName(typeName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
        int componentId = counter.incrementAndGet();
        component.instance = createComponentInstance(component.type, descriptor, componentId);
        if (!component.type.isInstance(component.instance)) {
            throw new ClassCastException(component.instance+" is not instance of "+component.type);
        }
        components.put(componentId, component);

        ByteBuffer response = ByteBufferUtils.getThreadLocalBuffer();
        response.clear();

        response.putInt(componentId);
        Method[] methods = component.type.getMethods();
        response.putInt(methods.length);
        for (int i = 0; i < methods.length; i ++) {
            response.putInt(i);
            ByteBufferUtils.putString(response, methods[i].toGenericString());
        }
        response.flip();
        return response;
    }
}
