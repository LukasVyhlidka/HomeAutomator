package org.vyhlidka.homeautomation.repo;

import org.junit.Before;
import org.junit.Test;
import org.vyhlidka.homeautomation.domain.Boiler;
import org.vyhlidka.homeautomation.domain.BoilerChange;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by lucky on 27.12.16.
 */
public class BoilerChangeInMemoryRepositoryTest {

    private BoilerChangeRepository repo;

    @Before
    public void setUp() throws Exception {
        this.repo = new BoilerChangeInMemoryRepository();
    }

    @Test
    public void testGetEmpty() throws Exception {
        assertThat(this.repo.getChanges()).isEmpty();
    }

    @Test
    public void testAddGet() throws Exception {
        final BoilerChange change1 = new BoilerChange("X", Boiler.BoilerState.SWITCHED_ON);
        final BoilerChange change2 = new BoilerChange("Y", Boiler.BoilerState.SWITCHED_OFF);
        final BoilerChange change3 = new BoilerChange("Z", Boiler.BoilerState.SWITCHED_ON);

        this.repo.addChange(change1);
        this.repo.addChange(change2);
        this.repo.addChange(change3);

        assertThat(this.repo.getChanges()).containsExactly(change1, change2, change3);
    }

    @Test
    public void testClear() throws Exception {
        final BoilerChange change1 = new BoilerChange("X", Boiler.BoilerState.SWITCHED_ON);
        final BoilerChange change2 = new BoilerChange("Y", Boiler.BoilerState.SWITCHED_OFF);
        final BoilerChange change3 = new BoilerChange("Z", Boiler.BoilerState.SWITCHED_ON);

        this.repo.addChange(change1);
        this.repo.addChange(change2);

        assertThat(this.repo.getChanges()).containsExactly(change1, change2);

        this.repo.clear();

        assertThat(this.repo.getChanges()).isEmpty();

        this.repo.addChange(change3);
        assertThat(this.repo.getChanges()).containsExactly(change3);
    }
}