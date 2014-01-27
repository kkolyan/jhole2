package net.kkolyan.jhole2.monitoring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
public class HistoryEntryImpl implements HistoryEntry {
    private final String name;
    private final long timestamp;
    private Map<String,HistorySection> sections = new LinkedHashMap<String, HistorySection>();

    public HistoryEntryImpl(String name) {
        this.name = name;
        timestamp = System.currentTimeMillis();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public synchronized HistorySection createSection(String name) {
        HistorySectionImpl section = new HistorySectionImpl(name);
        if (sections.containsKey(name)) {
            throw new IllegalStateException("section name already in use: "+name);
        }
        sections.put(name, section);
        return section;
    }

    @Override
    public synchronized Collection<HistorySection> getSections() {
        return new ArrayList<HistorySection>(sections.values());
    }

    @Override
    public synchronized HistorySection getSection(String name) {
        return sections.get(name);
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}
