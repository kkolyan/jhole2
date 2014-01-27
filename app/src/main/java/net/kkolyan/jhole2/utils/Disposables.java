package net.kkolyan.jhole2.utils;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class Disposables {
    public static void dispose(Object... objects) {
        for (Object o: objects) {
            if (o instanceof Disposable) {
                ((Disposable) o).destroy();
            }
        }
    }
}
