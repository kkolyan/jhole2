package net.kkolyan.jhole2.core;

import net.kkolyan.jhole2.utils.Address;

import java.io.IOException;

public interface Connector {
    Connection connect(Address address) throws IOException;
}
