package org.vyhlidka.homeautomation.domain;

import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;

/**
 * Created by lucky on 26.12.16.
 */
public class BoilerChange {

    public final LocalDateTime dateTime;

    public final Boiler.BoilerState state;

    public BoilerChange(final LocalDateTime dateTime, final Boiler.BoilerState state) {
        Validate.notNull(dateTime, "dateTime can not be null;");
        Validate.notNull(state, "state can not be null;");

        this.dateTime = dateTime;
        this.state = state;
    }

    public BoilerChange(final Boiler.BoilerState state) {
        this(LocalDateTime.now(), state);
    }
}
