package org.vyhlidka.homeautomation.repo;

import org.vyhlidka.homeautomation.domain.BoilerChange;

import java.util.List;

/**
 * Created by lucky on 26.12.16.
 */
public interface BoilerChangeRepository {

    List<BoilerChange> getChanges();

    void clear();

    void addChange(BoilerChange change);

}
