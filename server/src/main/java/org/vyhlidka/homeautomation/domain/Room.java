package org.vyhlidka.homeautomation.domain;

import java.util.Objects;

public class Room {

    public final int id;

    public final String name;

    public final Boiler.BoilerState state;

    public final Double actualTemp;

    public final Double targetTemp;

    public Room(final int id, final String name, final Boiler.BoilerState state, final Double actTemp, final Double targetTemp) {
        Objects.requireNonNull(name, "name can not be null.");
        Objects.requireNonNull(state, "state can not be null.");

        this.id = id;
        this.name = name;
        this.state = state;
        this.actualTemp = actTemp;
        this.targetTemp = targetTemp;
    }

}
