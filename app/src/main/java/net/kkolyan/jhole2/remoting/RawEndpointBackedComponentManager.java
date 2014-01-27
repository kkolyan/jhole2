package net.kkolyan.jhole2.remoting;

import net.kkolyan.jhole2.utils.ByteBufferUtils;
import net.kkolyan.jhole2.utils.Disposable;
import net.kkolyan.jhole2.utils.Disposables;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * @author NPlekhanov
 */
public class RawEndpointBackedComponentManager implements ComponentManager, Disposable {
    public static final int REQUEST_ID_CREATE_COMPONENT = 1001;
    public static final int REQUEST_ID_INVOKE_COMPONENT = 1002;
    private RawEndpoint destination;

    public RawEndpointBackedComponentManager(RawEndpoint destination) {
        this.destination = destination;
    }

    @Override
    public <T> T createComponent(Class<T> aClass, String descriptor) {
        ByteBuffer request = ByteBufferUtils.getThreadLocalBuffer();
        request.clear();
        request.putInt(REQUEST_ID_CREATE_COMPONENT);
        ByteBufferUtils.putString(request, aClass.getName());
        ByteBufferUtils.putString(request, descriptor);
        request.flip();

        ByteBuffer response = destination.executeRequest(request);
        final int componentId = response.getInt();
        int methodsNumber = response.getInt();
        final Map<String,Integer> methodCodes = new HashMap<String, Integer>();
        for (int i = 0; i < methodsNumber; i ++) {
            int code = response.getInt();
            String method = ByteBufferUtils.getString(response);
            methodCodes.put(method, code);
        }
        return aClass.cast(Enhancer.create(aClass, new ComponentMethodInvocationHandler(componentId, methodCodes)));
    }

    @Override
    public void destroy() {
        Disposables.dispose(destination);
    }

    private class ComponentMethodInvocationHandler implements InvocationHandler {
        private int componentId;
        private final Map<String, Integer> methodCodes;

        public ComponentMethodInvocationHandler(int componentId, Map<String, Integer> methodCodes) {
            this.componentId = componentId;
            this.methodCodes = methodCodes;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Integer methodCode = methodCodes.get(method.toGenericString());
            if (methodCode == null) {
                throw new UnsupportedOperationException(method.toGenericString());
            }
            ByteBuffer request = ByteBufferUtils.getThreadLocalBuffer();
            request.clear();
            request.putInt(REQUEST_ID_INVOKE_COMPONENT);
            request.putInt(componentId);
            request.putInt(methodCode);
            ByteBufferUtils.putObject(request, args);
            request.flip();

            ByteBuffer response = destination.executeRequest(request);
            boolean success = response.get() != 0;
            if (success) {
                if (method.getReturnType() == void.class) {
                    return null;
                }
                return ByteBufferUtils.getObject(response);
            }
            Object ex = ByteBufferUtils.getObject(response);
            if (ex instanceof InvocationTargetException) {
                ex = ((InvocationTargetException) ex).getTargetException();
            }
            throw (Throwable) ex;
        }
    }
}
