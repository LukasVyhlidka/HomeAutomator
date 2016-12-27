package org.vyhlidka.homeautomation.eq3;

import org.vyhlidka.homeautomation.eq3.domain.MaxMessage;

/**
 * Processor for eQ-3 MAX Cube messages. It transform a String message into the corresponding object representation.
 */
public interface MessageProcessor {

    /**
     * Processes the message.
     * @param message
     * @return
     */
    MaxMessage processMessage(String message);

}
