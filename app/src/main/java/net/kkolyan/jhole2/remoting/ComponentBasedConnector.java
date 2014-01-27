package net.kkolyan.jhole2.remoting;

import net.kkolyan.jhole2.core.Connection;
import net.kkolyan.jhole2.core.Connector;
import net.kkolyan.jhole2.utils.Address;
import net.kkolyan.jhole2.utils.Disposable;
import net.kkolyan.jhole2.utils.Disposables;

import java.io.IOException;

/**
 * @author NPlekhanov
 */
public class ComponentBasedConnector implements Connector, Disposable {
    private ComponentManager componentManager;

    public ComponentBasedConnector(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    @Override
    public Connection connect(Address address) throws IOException {
        return componentManager.createComponent(Connection.class, "connection:tcp:"+address.getHost()+":"+address.getPort());
    }

    @Override
    public void destroy() {
        Disposables.dispose(componentManager);
    }
}
