package org.vyhlidka.homeautomation.eq3;

import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;

/**
 * Client that communicates with the eQ-3
 */
public interface CubeClient {

    /**
     * Obtains the device list from the Cube
     * @return
     */
    LMaxMessage getDeviceList();

}
