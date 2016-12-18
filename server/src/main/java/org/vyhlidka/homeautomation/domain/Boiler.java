package org.vyhlidka.homeautomation.domain;

import org.apache.commons.lang3.Validate;

/**
 * Represents a Boiler
 */
public class Boiler {

    public String id;

    public BoilerState state;

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        Validate.notNull(id, "id can not be null;");
        this.id = id;
    }

    public BoilerState getState() {
        return state;
    }

    public void setState(final BoilerState state) {
        this.state = state;
    }

    public enum BoilerState {
        SWITCHED_ON, SWITCHED_OFF;
    }
}
