package org.vyhlidka.homeautomation.eq3;

import org.assertj.core.groups.Tuple;
import org.junit.Test;
import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by lucky on 26.12.16.
 */
public class LMessageParserTest {

    private LMessageParser parser = new LMessageParser();

    @Test(expected = NullPointerException.class)
    public void testNullMessage() throws Exception {
        parser.parse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBadFormat() throws Exception {
        parser.parse("Hello");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBlank() throws Exception {
        parser.parse(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDifferentMessageType() throws Exception {
        parser.parse("C:blahblahblah");
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