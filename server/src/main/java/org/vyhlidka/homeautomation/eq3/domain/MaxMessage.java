package org.vyhlidka.homeautomation.eq3.domain;

import org.apache.commons.lang3.Validate;

/**
 * Created by lucky on 26.12.16.
 */
public class MaxMessage {

    public final String message;

    public MaxMessage(final String message) {
        Validate.notNull(message, "message can not be null;");

        this.message = message;
    }
}
