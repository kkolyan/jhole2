package net.kkolyan.jhole2.monitoring;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
@Deprecated
public class MonitoringHolder {
    private static Monitoring instance = new Monitoring(false);

    public static void enableMonitoring() {
        instance = new Monitoring(true);
    }

    public static Monitoring getInstance() {
        return instance;
    }
}
