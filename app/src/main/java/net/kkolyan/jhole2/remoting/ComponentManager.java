package net.kkolyan.jhole2.remoting;

/**
 * @author NPlekhanov
 */
public interface ComponentManager {
    <T> T createComponent(Class<T> aClass, String descriptor);
}
