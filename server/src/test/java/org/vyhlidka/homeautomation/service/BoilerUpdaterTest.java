package org.vyhlidka.homeautomation.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.vyhlidka.homeautomation.domain.Boiler;
import org.vyhlidka.homeautomation.eq3.CubeClient;
import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;
import org.vyhlidka.homeautomation.repo.BoilerChangeRepository;
import org.vyhlidka.homeautomation.repo.BoilerRepository;

import java.util.Arrays;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by lucky on 27.12.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class BoilerUpdaterTest {

    @Mock
    private CubeClient cubeClient;

    @Mock
    private BoilerRepository boilerRepo;

    @Mock
    private BoilerChangeRepository boilerChangeRepo;

    private BoilerUpdater updater;

    @Before
    public void setUp() throws Exception {
        this.updater = new BoilerUpdater("Boiler1", this.cubeClient, this.boilerChangeRepo, this.boilerRepo);
    }

    @Test
    public void testUpdateStateWithOneThermostat() throws Exception {
        when(this.cubeClient.getDeviceList()).thenReturn(new LMaxMessage(
                "L:blabla",
                Arrays.asList(new LMaxMessage.MaxDevice(
                        LMaxMessage.MaxDeviceType.THERMOSTAT,
                        1024, (byte) 0, 0, (byte) 4, 40, 0, (byte) 0, 199))));

        when(this.boilerRepo.getBoiler("Boiler1")).thenReturn(Boiler.build("Boiler1", Boiler.BoilerState.SWITCHED_OFF));

        this.updater.updateBoiler();

        verify(this.boilerRepo, times(2)).getBoiler("Boiler1");
        verify(this.boilerRepo).setBoiler(Boiler.build("Boiler1", Boiler.BoilerState.SWITCHED_ON));
        verify(this.boilerChangeRepo).addChange(any());
    }
}