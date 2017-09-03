package org.vyhlidka.homeautomation.domain;

import java.util.Objects;

public class Room {

    public final int id;

    public final String name;

    public final Boiler.BoilerState state;

    public Room(final int id, final String name, final Boiler.BoilerState state) {
        Objects.requireNonNull(name, "name can not be null.");
        Objects.requireNonNull(state, "state can not be null.");

        this.id = id;
        this.name = name;
        this.state = state;
    }

}
