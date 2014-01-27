package net.kkolyan.jhole2;

import net.kkolyan.jhole2.core.Connection;
import net.kkolyan.jhole2.core.Connector;
import net.kkolyan.jhole2.utils.Address;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author nplekhanov
 */
public class DispatchingConnector implements Connector {
    private List<Mapping> mappings = new ArrayList<Mapping>();
    private Connector defaultConnector;

    public DispatchingConnector(Connector defaultConnector) {
        this.defaultConnector = defaultConnector;
    }

    public DispatchingConnector addRule(String hosts, Connector connector) {
        for (String host: hosts.split(";")) {
            if (host.trim().isEmpty()) {
                continue;
            }
            String regex = host.replace("*", "\\w*").replace("?", "\\w");
            mappings.add(new Mapping(Pattern.compile(regex), connector));
        }
        return this;
    }

    @Override
    public Connection connect(Address address) throws IOException {
        for (Mapping mapping: mappings) {
            if (mapping.hosts.matcher(address.getHost()).matches()) {
                return mapping.connector.connect(address);
            }
        }
        return defaultConnector.connect(address);
    }

    public static class Mapping {
        final Pattern hosts;
        final Connector connector;

        private Mapping(Pattern hosts, Connector connector) {
            this.hosts = hosts;
            this.connector = connector;
        }
    }
}
