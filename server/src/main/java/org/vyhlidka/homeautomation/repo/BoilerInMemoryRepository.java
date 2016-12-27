package org.vyhlidka.homeautomation.repo;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Repository;
import org.vyhlidka.homeautomation.domain.Boiler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lucky on 18.12.16.
 */
@Repository
class BoilerInMemoryRepository implements BoilerRepository {

    private Map<String, Boiler> boilers = new ConcurrentHashMap<>();

    @Override
    public Set<String> getBoilerKeys() {
        return this.boilers.keySet();
    }

    public Boiler getBoiler(final String id) {
        Validate.notNull(id, "id can not be null;");

        Boiler res = this.boilers.get(id);

        if (res == null) {
            throw new ElementNotFoundExcepion("Boiler "+ id + " was not found");
        }

        return res;
    }

    public void setBoiler(final Boiler boiler) {
        Validate.notNull(boiler, "boiler can not be null;");

        if (StringUtils.isBlank(boiler.getId())) {
            throw new RepositoryException("Boiler ID has to be set and can not be blank.");
        }

        if (boiler.getState() == null) {
            throw new RepositoryException("Boiler Switch state has to be set.");
        }

        this.boilers.put(boiler.getId(), boiler);
    }
}
