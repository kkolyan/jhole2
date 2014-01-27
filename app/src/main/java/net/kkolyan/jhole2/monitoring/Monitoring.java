package net.kkolyan.jhole2.monitoring;

import net.kkolyan.jhole2.core.Connection;
import net.kkolyan.jhole2.utils.LoggingConnection;
import net.kkolyan.jhole2.utils.LoggingInputStream;
import net.kkolyan.jhole2.utils.LoggingOutputStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:nplekhanov86@gmail.com">nplekhanov</a>
 */
@Deprecated
public class Monitoring {
    private List<HistoryEntry> entries = new ArrayList<HistoryEntry>();

    private boolean historyEnabled;

    public Monitoring(boolean historyEnabled) {
        this.historyEnabled = historyEnabled;
    }

    public synchronized HistoryEntry addEntry(String name) {
        HistoryEntry entry = new HistoryEntryImpl(name);
        entries.add(entry);
        return entry;
    }

    public synchronized List<HistoryEntry> getEntries() {
        ArrayList<HistoryEntry> list = new ArrayList<HistoryEntry>(entries);
        Collections.sort(list, new Comparator<HistoryEntry>() {
            @Override
            public int compare(HistoryEntry o1, HistoryEntry o2) {
                int diff = o1.getName().compareTo(o2.getName());
                if (diff == 0) {
                    diff = (int) (o1.getTimestamp() - o2.getTimestamp());
                }
                return diff;
            }
        });
        return list;
    }

    public InputStream wrap(String entry, String section, InputStream stream) {
        if (!historyEnabled) {
            return stream;
        }
        return new LoggingInputStream(stream, addEntry(entry).createSection(section).getOutputStream());
    }

    public OutputStream wrap(String entry, String section, OutputStream stream) {
        if (!historyEnabled) {
            return stream;
        }
        return new LoggingOutputStream(stream, addEntry(entry).createSection(section).getOutputStream());
    }

    public Connection wrap(String entry, Connection connection) {
        if (!historyEnabled) {
            return connection;
        }
        return new LoggingConnection(connection,
                addEntry(entry).createSection("out").getOutputStream(),
                addEntry(entry).createSection("in").getOutputStream()
        );
    }

    public boolean isHistoryEnabled() {
        return historyEnabled;
    }
}
