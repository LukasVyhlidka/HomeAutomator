package org.vyhlidka.homeautomation.eq3;

import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;
import org.vyhlidka.homeautomation.eq3.domain.MaxMessage;

import java.util.List;

/**
 * Client that communicates with the eQ-3
 */
public interface CubeClient {

    /**
     * Obtains the device list from the Cube
     * @return
     */
    LMaxMessage getDeviceList();

    /**
     * Obtains the initial messages that the cube sends.
     * @return
     */
    List<MaxMessage> getInitialMessages();

}
