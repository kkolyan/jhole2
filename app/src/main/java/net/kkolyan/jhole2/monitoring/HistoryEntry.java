package net.kkolyan.jhole2.monitoring;

import java.util.Collection;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public interface HistoryEntry {
    String getName();
    HistorySection createSection(String name);
    Collection<HistorySection> getSections();
    HistorySection getSection(String name);

    long getTimestamp();
}
