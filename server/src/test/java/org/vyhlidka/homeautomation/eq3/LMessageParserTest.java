package org.vyhlidka.homeautomation.eq3;

import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

;

/**
 * Created by lucky on 26.12.16.
 */
public class LMessageParserTest {

    private LMessageParser parser = new LMessageParser();

    @Test
    public void testNullMessage() throws Exception {
        assertThatThrownBy(() -> parser.parse(null)).isInstanceOf(NullPointerException.class);

    }

    @Test
    public void testBadFormat() throws Exception {
        assertThatThrownBy(() -> parser.parse("Hello")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testBlank() throws Exception {
        assertThatThrownBy(() -> parser.parse(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testDifferentMessageType() throws Exception {
        assertThatThrownBy(() -> parser.parse("C:blahblahblah")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testExample1() throws Exception {
        final LMaxMessage msg = parser.parse("L:Cw/a7QkSGBgoAMwACw/DcwkSGBgoAM8ACw/DgAkSGBgoAM4A");

        assertThat(msg).isNotNull();
        assertThat(msg.devices)
                .extracting("type", "rfAddress")
                .containsOnly(
                        Tuple.tuple(LMaxMessage.MaxDeviceType.VALVE, 0x0FDAED),
                        Tuple.tuple(LMaxMessage.MaxDeviceType.VALVE, 0x0fc373),
                        Tuple.tuple(LMaxMessage.MaxDeviceType.VALVE, 0x0fc380));
    }

    @Test
    public void testHomeExample() throws Exception {
        final LMaxMessage msg = parser.parse("L:DBaBdQkSGAQmAAAA7gsVFl7xEhgAJgAAAAsVFXrxEhgAJgAAAAwWetakEhgELgAAAOcLFRZW7BIYAS4A5wALFRYj7BIYAC4AAAA=");

        assertThat(msg).isNotNull();
        System.out.println(msg);
    }

    @Test
    public void testRealLMessageWithout00AtEnd() throws Exception {
        final LMaxMessage msg = parser.parse("L:DBaBdQkSGAQuAAAA1wsVFlYJEhhkLQAAAAsVFLAJEhgHKgDTAAwWgQYJEhgEKgAAANMLFRZeCRIYZC4AAAALFRYjCRIYZC0AAAAMFnrWCRIYBC0AAADM");
        System.out.println(msg);

        assertThat(msg.devices)
                .extracting("type")
                .containsOnly(
                        LMaxMessage.MaxDeviceType.THERMOSTAT,
                        LMaxMessage.MaxDeviceType.VALVE,
                        LMaxMessage.MaxDeviceType.VALVE,
                        LMaxMessage.MaxDeviceType.THERMOSTAT,
                        LMaxMessage.MaxDeviceType.VALVE,
                        LMaxMessage.MaxDeviceType.VALVE,
                        LMaxMessage.MaxDeviceType.THERMOSTAT
                );
    }

    @Test
    public void testHomeExample2() throws Exception {
        final LMaxMessage msg = parser.parse("L:DBaBdQkSGASvAAAAFgsVFl7xEhgALwAAAAsVFXrxEhgALwAAAAwWetakEhgELgAAAN8LFRZW7BIYZC4AAAALFRYj7BIYZC4AAAA=");

        assertThat(msg).isNotNull();

        assertThat(msg.devices)
                .extracting("type", "rfAddress", "valvePosition", "temperature", "actualTemperature")
                .containsOnly(
                        Tuple.tuple(
                                LMaxMessage.MaxDeviceType.THERMOSTAT,
                                1474933,
                                (byte)4,
                                47,
                                278),
                        Tuple.tuple(
                                LMaxMessage.MaxDeviceType.VALVE,
                                1381982,
                                (byte)0,
                                47,
                                0),
                        Tuple.tuple(
                                LMaxMessage.MaxDeviceType.VALVE,
                                1381754,
                                (byte)0,
                                47,
                                0),
                        Tuple.tuple(
                                LMaxMessage.MaxDeviceType.THERMOSTAT,
                                1473238,
                                (byte)4,
                                46,
                                223),
                        Tuple.tuple(
                                LMaxMessage.MaxDeviceType.VALVE,
                                1381974,
                                (byte)100,
                                46,
                                0),
                        Tuple.tuple(
                                LMaxMessage.MaxDeviceType.VALVE,
                                1381923,
                                (byte)100,
                                46,
                                0)
                );
    }
}