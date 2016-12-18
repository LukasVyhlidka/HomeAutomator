package org.vyhlidka.homeautomation.repo;

import org.vyhlidka.homeautomation.domain.Boiler;

import java.util.Set;

/**
 * Created by lucky on 18.12.16.
 */
public interface BoilerRepository {

    Boiler getBoiler(String id);

    void setBoiler(Boiler boiler);

    Set<String> getBoilerKeys();

}
