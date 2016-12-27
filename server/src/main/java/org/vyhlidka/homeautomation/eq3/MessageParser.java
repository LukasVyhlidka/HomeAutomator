package org.vyhlidka.homeautomation.eq3;

import org.vyhlidka.homeautomation.eq3.domain.MaxMessage;

/**
 * A Processor for eQ-3 MAX Cube message parsing.
 */
public interface MessageParser<T extends MaxMessage> {

    String getMessageType();

    T parse(String message);

}
