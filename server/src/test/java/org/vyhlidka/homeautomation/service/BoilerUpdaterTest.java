package org.vyhlidka.homeautomation.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vyhlidka.homeautomation.domain.Boiler;
import org.vyhlidka.homeautomation.domain.Room;
import org.vyhlidka.homeautomation.eq3.CubeClient;
import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;
import org.vyhlidka.homeautomation.eq3.domain.MMaxMessage;
import org.vyhlidka.homeautomation.repo.BoilerChangeRepository;
import org.vyhlidka.homeautomation.repo.BoilerRepository;
import org.vyhlidka.homeautomation.repo.RoomRepository;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Created by lucky on 27.12.16.
 */
@ExtendWith(MockitoExtension.class)
public class BoilerUpdaterTest {

    public static final String BOILER_ID = "Boiler1";
    @Mock
    private CubeClient cubeClient;

    @Mock
    private BoilerRepository boilerRepo;

    @Mock
    private BoilerChangeRepository boilerChangeRepo;

    @Mock
    private RoomRepository roomRepository;

    private BoilerUpdater updater;

    @BeforeEach
    public void setUp() throws Exception {
        this.updater = new BoilerUpdater(BOILER_ID, this.cubeClient, this.boilerChangeRepo, this.boilerRepo, this.roomRepository);
    }

    @Test
    public void testContinueHeatingWhenNotTooHot() throws Exception {
        when(this.cubeClient.getInitialMessages()).thenReturn(
                Arrays.asList(
                        new LMaxMessage(
                                "L:blabla", Arrays.asList(
                                    genThermostat(1024, 40, 201),
                                    genValve(1024, 40, (byte) 32)))
                )
        );

        this.mockDefaultRoomRepo(Boiler.BoilerState.SWITCHED_ON);

        this.updater.updateRooms();

        this.verifySavedRoomState(Boiler.BoilerState.SWITCHED_ON);
        verify(this.roomRepository).getRooms();
        verify(this.roomRepository).getRoom(-1); // The default room (test does not create rooms)

        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo, this.roomRepository);
    }

    @Test
    public void testContinueHeatingWhenNotTooCold() throws Exception {
        when(this.cubeClient.getInitialMessages()).thenReturn(
                Arrays.asList(
                        new LMaxMessage(
                                "L:blabla", Arrays.asList(
                                genThermostat(1024, 40, 199),
                                genValve(1024, 40, (byte) 80)))
                )
        );

        this.mockDefaultRoomRepo(Boiler.BoilerState.SWITCHED_ON);

        this.updater.updateRooms();

        this.verifySavedRoomState(Boiler.BoilerState.SWITCHED_ON);
        verify(this.roomRepository).getRooms();
        verify(this.roomRepository).getRoom(-1); // The default room (test does not create rooms)

        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo, this.roomRepository);
    }

    private void verifySavedRoomState(Boiler.BoilerState state) {
        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(this.roomRepository).saveRoom(roomCaptor.capture());
        Room room = roomCaptor.getValue();
        assertThat(room.state).isEqualTo(state);
    }

    private void mockDefaultRoomRepo(Boiler.BoilerState state) {
        when(this.roomRepository.getRoom(-1)).thenReturn(new Room(-1, "Default", state));
    }

    @Test
    public void testContinueHeatingWhenTooCold() throws Exception {
        when(this.cubeClient.getInitialMessages()).thenReturn(
                Arrays.asList(
                        new LMaxMessage(
                                "L:blabla", Arrays.asList(
                                genThermostat(1024, 40, 195),
                                genValve(1024, 40, (byte) 100)))
                )
        );

        this.mockDefaultRoomRepo(Boiler.BoilerState.SWITCHED_ON);

        this.updater.updateRooms();

        this.verifySavedRoomState(Boiler.BoilerState.SWITCHED_ON);
        verify(this.roomRepository).getRooms();
        verify(this.roomRepository).getRoom(-1); // The default room (test does not create rooms)

        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo, this.roomRepository);
    }

    @Test
    public void testStopHeatingWhenTooHot() throws Exception {
        when(this.cubeClient.getInitialMessages()).thenReturn(
                Arrays.asList(
                        new LMaxMessage(
                                "L:blabla", Arrays.asList(
                                genThermostat(1024, 40, 202),
                                genValve(1024, 40, (byte) 80)))
                )
        );

        this.mockDefaultRoomRepo(Boiler.BoilerState.SWITCHED_ON);

        this.updater.updateRooms();

        this.verifySavedRoomState(Boiler.BoilerState.SWITCHED_OFF);
        verify(this.roomRepository).getRooms();
        verify(this.roomRepository).getRoom(-1); // The default room (test does not create rooms)

        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo, this.roomRepository);
    }

    @Test
    public void testStartHeatingWhenTooCold() throws Exception {
        when(this.cubeClient.getInitialMessages()).thenReturn(
                Arrays.asList(
                        new LMaxMessage(
                                "L:blabla", Arrays.asList(
                                genThermostat(1024, 40, 198),
                                genValve(1024, 40, (byte) 80)))
                )
        );

        this.mockDefaultRoomRepo(Boiler.BoilerState.SWITCHED_ON);

        this.updater.updateRooms();

        this.verifySavedRoomState(Boiler.BoilerState.SWITCHED_ON);
        verify(this.roomRepository).getRooms();
        verify(this.roomRepository).getRoom(-1); // The default room (test does not create rooms)

        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo, this.roomRepository);
    }

    @Test
    public void testContinueOffWhenNotTooCold() throws Exception {
        when(this.cubeClient.getInitialMessages()).thenReturn(
                Arrays.asList(
                        new LMaxMessage(
                                "L:blabla", Arrays.asList(
                                genThermostat(1024, 40, 199),
                                genValve(1024, 40, (byte) 80)))
                )
        );

        this.mockDefaultRoomRepo(Boiler.BoilerState.SWITCHED_OFF);

        this.updater.updateRooms();

        this.verifySavedRoomState(Boiler.BoilerState.SWITCHED_OFF);
        verify(this.roomRepository).getRooms();
        verify(this.roomRepository).getRoom(-1); // The default room (test does not create rooms)

        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo, this.roomRepository);
    }

    @Test
    public void testContinueOffWhenNotTooHot() throws Exception {
        when(this.cubeClient.getInitialMessages()).thenReturn(
                Arrays.asList(
                        new LMaxMessage(
                                "L:blabla", Arrays.asList(
                                genThermostat(1024, 40, 201),
                                genValve(1024, 40, (byte) 80)))
                )
        );

        this.mockDefaultRoomRepo(Boiler.BoilerState.SWITCHED_OFF);

        this.updater.updateRooms();

        this.verifySavedRoomState(Boiler.BoilerState.SWITCHED_OFF);
        verify(this.roomRepository).getRooms();
        verify(this.roomRepository).getRoom(-1); // The default room (test does not create rooms)

        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo, this.roomRepository);
    }

    @Test
    public void testContinueOffWhenTooHot() throws Exception {
        when(this.cubeClient.getInitialMessages()).thenReturn(
                Arrays.asList(
                        new LMaxMessage(
                                "L:blabla", Arrays.asList(
                                genThermostat(1024, 40, 202),
                                genValve(1024, 40, (byte) 80)))
                )
        );

        this.mockDefaultRoomRepo(Boiler.BoilerState.SWITCHED_OFF);

        this.updater.updateRooms();

        this.verifySavedRoomState(Boiler.BoilerState.SWITCHED_OFF);
        verify(this.roomRepository).getRooms();
        verify(this.roomRepository).getRoom(-1); // The default room (test does not create rooms)

        verifyNoMoreInteractions(this.boilerRepo, this.boilerChangeRepo, this.roomRepository);
    }

    private static LMaxMessage.MaxDevice genThermostat(int adr, int temperature, int actualTemperature) {
        return generateDevice(LMaxMessage.MaxDeviceType.THERMOSTAT, adr, (byte) 4, temperature, actualTemperature);
    }

    private static LMaxMessage.MaxDevice genValve(int adr, int temperature, byte valve) {
        return generateDevice(LMaxMessage.MaxDeviceType.VALVE, adr, valve, temperature, 0);
    }

    private static LMaxMessage.MaxDevice generateDevice(LMaxMessage.MaxDeviceType type, int adr, byte valve, int temperature, int actualTemperature) {
        return new LMaxMessage.MaxDevice(type, adr, (byte) 0, 0, valve, temperature, 0, (byte) 0, actualTemperature);
    }

    private static MMaxMessage.MaxRoomMeta genRoomMeta(int id, String name) {
        return new MMaxMessage.MaxRoomMeta(id, name, 42);
    }

    private static MMaxMessage.MaxDeviceMeta genDevMeta(int rfAddr, String name, int roomId) {
        return new MMaxMessage.MaxDeviceMeta(MMaxMessage.DeviceTypeMeta.HeatingThermostat, rfAddr, "XXX", name, roomId);
    }
}