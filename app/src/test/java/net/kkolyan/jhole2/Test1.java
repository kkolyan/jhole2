package net.kkolyan.jhole2;

import net.kkolyan.jhole2.log.H2ApplicationLogger;
import net.kkolyan.jhole2.monitoring.Monitoring;
import net.kkolyan.jhole2.remoting.ComponentManager;
import net.kkolyan.jhole2.remoting.LocalRawEndpoint;
import net.kkolyan.jhole2.remoting.RawEndpoint;
import net.kkolyan.jhole2.remoting.RawEndpointBackedComponentManager;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author NPlekhanov
 */
public class Test1 {
    @Test
    public void test1() {
        final List<Invocation> invocations = new ArrayList<Invocation>();
        RawEndpoint endpoint = new LocalRawEndpoint("", new H2ApplicationLogger()) {
            @Override
            protected Object createComponentInstance(Class<?> type, String descriptor, int componentId) {
                if (descriptor.startsWith("drivable:")) {
                    final String name = descriptor.substring("drivable:".length());
                    return Enhancer.create(Drivable.class, new MethodInterceptor() {
                        @Override
                        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                            if (method.getName().equals("toString")) {
                                return proxy.toString();
                            }
                            invocations.add(new Invocation(method.getName(), args));
                            if (method.getName().equals("getSpeed")) {
                                return (Integer) args[0] * (Integer) args[1];
                            }
                            if (method.getName().equals("checkFuel")) {
                                return true;
                            }
                            if (method.getName().equals("getName")) {
                                return name;
                            }
                            if (method.getName().equals("crash1")) {
                                throw new IllegalStateException("crash 1");
                            }
                            if (method.getName().equals("crash2")) {
                                throw new IllegalStateException("crash 2");
                            }
                            return null;
                        }
                    });
                }
                return super.createComponentInstance(type, descriptor, componentId);
            }
        };
        ComponentManager componentManager = new RawEndpointBackedComponentManager(endpoint);

        for (String name: new String[] {"bmw","renault"}) {
            Drivable drivable = componentManager.createComponent(Drivable.class, "drivable:"+name);
            drivable.steer(0.15);
            drivable.steer(5.5);
            drivable.stop();
            int speed = drivable.getSpeed(5, 7);
            drivable.beep("Yo");
            boolean fuel = drivable.checkFuel();
            String n = drivable.getName();

            Exception e1 = null;
            try {
                drivable.crash1();
            } catch (Exception e) {
                e1 = e;
            }
            Exception e2 = null;
            try {
                drivable.crash2();
            } catch (Exception e) {
                e2 = e;
            }

            assertEquals(invocations.get(0), new Invocation("steer", 0.15));
            assertEquals(invocations.get(1), new Invocation("steer", 5.5));
            assertEquals(invocations.get(2), new Invocation("stop"));
            assertEquals(invocations.get(3), new Invocation("getSpeed", 5, 7));
            assertEquals(invocations.get(4), new Invocation("beep", "Yo"));
            assertEquals(invocations.get(5), new Invocation("checkFuel"));
            assertEquals(invocations.get(6), new Invocation("getName"));
            assertEquals(invocations.get(7), new Invocation("crash1"));
            assertEquals(invocations.get(8), new Invocation("crash2"));

            assertEquals(5*7, speed);
            assertEquals(true, fuel);
            assertEquals(name, n);
            assertEquals("crash 1", e1.getMessage());
            assertEquals("crash 2", e2.getMessage());

            invocations.clear();
        }

    }

    public static interface Drivable {
        void steer(double angle);
        void start();
        void stop();
        int getSpeed(int x, int y);
        void beep(String s);
        boolean checkFuel();
        String getName();
        void crash1();
        String crash2();
    }

    private  static class Invocation {
        String method;
        Object[] args;

        private Invocation(String method, Object... args) {
            this.method = method;
            this.args = args;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Invocation that = (Invocation) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(args, that.args)) return false;
            if (method != null ? !method.equals(that.method) : that.method != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = method != null ? method.hashCode() : 0;
            result = 31 * result + (args != null ? Arrays.hashCode(args) : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Invocation{" +
                    "method='" + method + '\'' +
                    ", args=" + (args == null ? null : Arrays.asList(args)) +
                    '}';
        }
    }
}
