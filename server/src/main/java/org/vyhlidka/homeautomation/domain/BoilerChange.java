package org.vyhlidka.homeautomation.domain;

import org.apache.commons.lang3.Validate;

import java.time.LocalDateTime;

/**
 * Created by lucky on 26.12.16.
 */
public class BoilerChange {

    public final String boilerId;

    public final LocalDateTime dateTime;

    public final Boiler.BoilerState state;

    public BoilerChange(final String boilerId, final LocalDateTime dateTime, final Boiler.BoilerState state) {
        Validate.notNull(boilerId, "boilerId can not be null;");
        Validate.notNull(dateTime, "dateTime can not be null;");
        Validate.notNull(state, "state can not be null;");

        this.boilerId = boilerId;
        this.dateTime = dateTime;
        this.state = state;
    }

    public BoilerChange(final String boilerId, final Boiler.BoilerState state) {
        this(boilerId, LocalDateTime.now(), state);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BoilerChange that = (BoilerChange) o;

        if (boilerId != null ? !boilerId.equals(that.boilerId) : that.boilerId != null) return false;
        if (dateTime != null ? !dateTime.equals(that.dateTime) : that.dateTime != null) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = boilerId != null ? boilerId.hashCode() : 0;
        result = 31 * result + (dateTime != null ? dateTime.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        return result;
    }
}
