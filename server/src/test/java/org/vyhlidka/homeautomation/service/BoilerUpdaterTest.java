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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by lucky on 27.12.16.
 */
@RunWith(MockitoJUnitRunner.class)
public class BoilerUpdaterTest {

    public static final String BOILER_ID = "Boiler1";
    @Mock
    private CubeClient cubeClient;

    @Mock
    private BoilerRepository boilerRepo;

    @Mock
    private BoilerChangeRepository boilerChangeRepo;

    private BoilerUpdater updater;

    @Before
    public void setUp() throws Exception {
        this.updater = new BoilerUpdater(BOILER_ID, this.cubeClient, this.boilerChangeRepo, this.boilerRepo);
    }

    @Test
    public void testContinueHeatingWhenNotTooHot() throws Exception {
        when(this.cubeClient.getDeviceList()).thenReturn(new LMaxMessage(
                "L:blabla",
                Arrays.asList(genThermostat(1024, 40, 201))));

        when(this.boilerRepo.getBoiler(BOILER_ID)).thenReturn(Boiler.build(BOILER_ID, Boiler.BoilerState.SWITCHED_ON));

        this.updater.updateBoiler();

        verify(this.boilerRepo).getBoiler(BOILER_ID);
        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo);
    }

    @Test
    public void testContinueHeatingWhenNotTooCold() throws Exception {
        when(this.cubeClient.getDeviceList()).thenReturn(new LMaxMessage(
                "L:blabla",
                Arrays.asList(genThermostat(1024, 40, 199))));

        when(this.boilerRepo.getBoiler(BOILER_ID)).thenReturn(Boiler.build(BOILER_ID, Boiler.BoilerState.SWITCHED_ON));

        this.updater.updateBoiler();

        verify(this.boilerRepo).getBoiler(BOILER_ID);
        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo);
    }

    @Test
    public void testContinueHeatingWhenTooCold() throws Exception {
        when(this.cubeClient.getDeviceList()).thenReturn(new LMaxMessage(
                "L:blabla",
                Arrays.asList(genThermostat(1024, 40, 195))));

        when(this.boilerRepo.getBoiler(BOILER_ID)).thenReturn(Boiler.build(BOILER_ID, Boiler.BoilerState.SWITCHED_ON));

        this.updater.updateBoiler();

        verify(this.boilerRepo).getBoiler(BOILER_ID);
        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo);
    }

    @Test
    public void testStopHeatingWhenTooHot() throws Exception {
        when(this.cubeClient.getDeviceList()).thenReturn(new LMaxMessage(
                "L:blabla",
                Arrays.asList(genThermostat(1024, 40, 202))));

        when(this.boilerRepo.getBoiler(BOILER_ID)).thenReturn(Boiler.build(BOILER_ID, Boiler.BoilerState.SWITCHED_ON));

        this.updater.updateBoiler();

        verify(this.boilerRepo).getBoiler(BOILER_ID);
        verify(this.boilerRepo).setBoiler(Boiler.build(BOILER_ID, Boiler.BoilerState.SWITCHED_OFF));
        verify(this.boilerChangeRepo).addChange(any());
        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo);
    }

    @Test
    public void testStartHeatingWhenTooCold() throws Exception {
        when(this.cubeClient.getDeviceList()).thenReturn(new LMaxMessage(
                "L:blabla",
                Arrays.asList(genThermostat(1024, 40, 198))));

        when(this.boilerRepo.getBoiler(BOILER_ID)).thenReturn(Boiler.build(BOILER_ID, Boiler.BoilerState.SWITCHED_OFF));

        this.updater.updateBoiler();

        verify(this.boilerRepo).getBoiler(BOILER_ID);
        verify(this.boilerRepo).setBoiler(Boiler.build(BOILER_ID, Boiler.BoilerState.SWITCHED_ON));
        verify(this.boilerChangeRepo).addChange(any());
        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo);
    }

    @Test
    public void testContinueOffWhenNotTooCold() throws Exception {
        when(this.cubeClient.getDeviceList()).thenReturn(new LMaxMessage(
                "L:blabla",
                Arrays.asList(genThermostat(1024, 40, 199))));

        when(this.boilerRepo.getBoiler(BOILER_ID)).thenReturn(Boiler.build(BOILER_ID, Boiler.BoilerState.SWITCHED_OFF));

        this.updater.updateBoiler();

        verify(this.boilerRepo).getBoiler(BOILER_ID);
        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo);
    }

    @Test
    public void testContinueOffWhenNotTooHot() throws Exception {
        when(this.cubeClient.getDeviceList()).thenReturn(new LMaxMessage(
                "L:blabla",
                Arrays.asList(genThermostat(1024, 40, 201))));

        when(this.boilerRepo.getBoiler(BOILER_ID)).thenReturn(Boiler.build(BOILER_ID, Boiler.BoilerState.SWITCHED_OFF));

        this.updater.updateBoiler();

        verify(this.boilerRepo).getBoiler(BOILER_ID);
        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo);
    }

    @Test
    public void testContinueOffWhenTooHot() throws Exception {
        when(this.cubeClient.getDeviceList()).thenReturn(new LMaxMessage(
                "L:blabla",
                Arrays.asList(genThermostat(1024, 40, 210))));

        when(this.boilerRepo.getBoiler(BOILER_ID)).thenReturn(Boiler.build(BOILER_ID, Boiler.BoilerState.SWITCHED_OFF));

        this.updater.updateBoiler();

        verify(this.boilerRepo).getBoiler(BOILER_ID);
        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo);
    }

    private static LMaxMessage.MaxDevice genThermostat(int adr, int temperature, int actualTemperature) {
        return generateDevice(LMaxMessage.MaxDeviceType.THERMOSTAT, adr, temperature, actualTemperature);
    }

    private static LMaxMessage.MaxDevice generateDevice(LMaxMessage.MaxDeviceType type, int adr, int temperature, int actualTemperature) {
        return new LMaxMessage.MaxDevice(type, adr, (byte) 0, 0, (byte) 4, temperature, 0, (byte) 0, actualTemperature);
    }
}