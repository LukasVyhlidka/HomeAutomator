package org.vyhlidka.homeautomation.eq3.domain;

import org.apache.commons.lang3.Validate;

/**
 * Created by lucky on 26.12.16.
 */
public class MaxMessage {

    public final String msgType;

    public final String message;

    public MaxMessage(final String msgType, final String message) {
        Validate.notNull(msgType, "msgType can not be null;");
        Validate.notNull(message, "message can not be null;");

        this.msgType = msgType;
        this.message = message;
    }

    public String getMessageType() {
        return this.msgType;
    }

}
