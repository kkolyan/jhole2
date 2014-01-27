package net.kkolyan.jhole2.remoting;

import java.nio.ByteBuffer;

/**
 * @author NPlekhanov
 */
public interface RawEndpoint {
    ByteBuffer executeRequest(ByteBuffer request);
}
