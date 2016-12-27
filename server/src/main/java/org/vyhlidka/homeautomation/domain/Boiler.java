package org.vyhlidka.homeautomation.domain;

import org.apache.commons.lang3.Validate;

/**
 * Represents a Boiler
 */
public class Boiler {

    public static Boiler build(String id, BoilerState state) {
        Boiler b = new Boiler();
        b.setId(id);
        b.setState(state);
        return b;
    }

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

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final Boiler boiler = (Boiler) o;

        if (id != null ? !id.equals(boiler.id) : boiler.id != null) return false;
        return state == boiler.state;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }

    public enum BoilerState {
        SWITCHED_ON, SWITCHED_OFF;
    }
}
