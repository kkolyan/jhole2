package net.kkolyan.jhole2.utils;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class ThreadUtils {
    private static final ThreadLocal<String> originalName = new ThreadLocal<String>() {
        @Override
        protected String initialValue() {
            return Thread.currentThread().getName();
        }
    };
    public static void label(String text) {
        Thread.currentThread().setName(originalName.get()+" ("+text+")");
    }
}
