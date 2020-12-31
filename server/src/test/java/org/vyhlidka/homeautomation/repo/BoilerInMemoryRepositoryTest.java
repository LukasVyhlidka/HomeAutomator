package org.vyhlidka.homeautomation.repo;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vyhlidka.homeautomation.domain.Boiler;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Created by lucky on 18.12.16.
 */
public class BoilerInMemoryRepositoryTest {

    private BoilerRepository repository;

    @BeforeEach
    public void setUp() throws Exception {
        this.repository = new BoilerInMemoryRepository();
    }

    @Test
    public void testGetNonExisting() throws Exception {
        assertThatThrownBy(() -> this.repository.getBoiler("XYZ")).isInstanceOf(ElementNotFoundExcepion.class);
    }

    @Test
    public void testGetNullId() throws Exception {
        assertThatThrownBy(() -> this.repository.getBoiler(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testAddNullId() throws Exception {
        Boiler b = new Boiler();
        b.setState(Boiler.BoilerState.SWITCHED_ON);

        assertThatThrownBy(() -> this.repository.setBoiler(b)).isInstanceOf(RepositoryException.class);
    }

    @Test
    public void testNullState() throws Exception {
        Boiler b = new Boiler();
        b.setId("TestBoiler");

        assertThatThrownBy(() -> this.repository.setBoiler(b)).isInstanceOf(RepositoryException.class);
    }

    @Test
    public void testAddGetDiferent() throws Exception {
        Boiler b = new Boiler();
        b.setId("TestBoiler");
        b.setState(Boiler.BoilerState.SWITCHED_OFF);

        this.repository.setBoiler(b);

        try {
            this.repository.getBoiler("Different");
            Assertions.fail("Should have thrown exception.");
        } catch (ElementNotFoundExcepion e) {
            //OK
        }
    }

    @Test
    public void testAddGet() throws Exception {
        Boiler b = new Boiler();
        b.setId("TestBoiler");
        b.setState(Boiler.BoilerState.SWITCHED_OFF);

        this.repository.setBoiler(b);

        Boiler obtained = this.repository.getBoiler("TestBoiler");
        Assertions.assertThat(obtained.getId()).isEqualTo("TestBoiler");
        Assertions.assertThat(obtained.getState()).isEqualTo(Boiler.BoilerState.SWITCHED_OFF);
    }

    @Test
    public void testAddGetSeveral() throws Exception {
        Boiler b1 = new Boiler();
        b1.setId("TestBoiler");
        b1.setState(Boiler.BoilerState.SWITCHED_OFF);

        Boiler b2 = new Boiler();
        b2.setId("TestBoiler2");
        b2.setState(Boiler.BoilerState.SWITCHED_ON);

        Boiler b3 = new Boiler();
        b3.setId("TestBoiler3");
        b3.setState(Boiler.BoilerState.SWITCHED_ON);

        this.repository.setBoiler(b1);
        this.repository.setBoiler(b3);
        this.repository.setBoiler(b2);

        Boiler obtB1 = this.repository.getBoiler("TestBoiler");
        Boiler obtB2 = this.repository.getBoiler("TestBoiler2");
        Boiler obtB3 = this.repository.getBoiler("TestBoiler3");

        Assertions.assertThat(obtB1).isEqualToComparingFieldByField(b1);
        Assertions.assertThat(obtB2).isEqualToComparingFieldByField(b2);
        Assertions.assertThat(obtB3).isEqualToComparingFieldByField(b3);

    }

    @Test
    public void testEmptyBoilers() throws Exception {
        Assertions.assertThat(this.repository.getBoilerKeys()).isEmpty();
    }

    @Test
    public void testGetBoilerKeys() throws Exception {
        Boiler b1 = new Boiler();
        b1.setId("TestBoiler");
        b1.setState(Boiler.BoilerState.SWITCHED_OFF);

        Boiler b2 = new Boiler();
        b2.setId("TestBoiler2");
        b2.setState(Boiler.BoilerState.SWITCHED_ON);

        this.repository.setBoiler(b1);
        this.repository.setBoiler(b2);

        Assertions.assertThat(this.repository.getBoilerKeys())
                .containsOnly("TestBoiler", "TestBoiler2");

    }
}
