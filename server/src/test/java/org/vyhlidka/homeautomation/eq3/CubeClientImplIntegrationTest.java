package org.vyhlidka.homeautomation.eq3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;
import org.vyhlidka.homeautomation.eq3.domain.MaxMessage;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by lucky on 26.12.16.
 */
@ExtendWith(SpringExtension.class)

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CubeClientImplIntegrationTest {

    @Autowired
    private CubeClient client;

    @Test
    public void testGetLMessage() throws Exception {
        LMaxMessage message = this.client.getDeviceList();
        assertThat(message).isNotNull();
    }

    @Test
    public void testGetInitialMessages() throws Exception {
        final List<MaxMessage> msgs = this.client.getInitialMessages();

    }
}