package org.vyhlidka.homeautomation.repo;

import org.springframework.stereotype.Repository;
import org.vyhlidka.homeautomation.domain.BoilerChange;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by lucky on 26.12.16.
 */
@Repository
public class BoilerChangeInMemoryRepository implements BoilerChangeRepository {

    private List<BoilerChange> changes = new ArrayList<>();

    @Override
    public List<BoilerChange> getChanges() {
        return Collections.unmodifiableList(this.changes);
    }

    @Override
    public void clear() {
        this.changes = new ArrayList<>();
    }

    @Override
    public void addChange(final BoilerChange change) {
        this.changes.add(change);
    }
}
