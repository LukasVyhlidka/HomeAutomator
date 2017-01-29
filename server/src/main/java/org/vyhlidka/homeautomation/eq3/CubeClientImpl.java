package org.vyhlidka.homeautomation.eq3;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.vyhlidka.homeautomation.eq3.domain.LMaxMessage;
import org.vyhlidka.homeautomation.eq3.domain.MaxMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Created by lucky on 26.12.16.
 */
@Component
public class CubeClientImpl implements CubeClient {

    private static final Logger logger = LoggerFactory.getLogger(CubeClientImpl.class);

    private final String host;
    private final int port;
    private final MessageProcessor processor;

    @Autowired
    public CubeClientImpl(@Value("${eQ3.cube.host}") final String host, @Value("${eQ3.cube.port}") final int port, final MessageProcessor processor) {
        Validate.notNull(host, "host can not be null;");
        Validate.notNull(processor, "processor can not be null;");
        Validate.inclusiveBetween(1, 65535, port, "Port has to be in range <1; 65535>");

        this.host = host;
        this.port = port;
        this.processor = processor;
    }

    @Override
    public LMaxMessage getDeviceList() {
        List<MaxMessage> messages = this.getInitialMessages();
        final Optional<MaxMessage> lMessage = messages.stream().filter(m -> m instanceof LMaxMessage).findFirst();
        if (!lMessage.isPresent()) {
            logger.error("Cube response does not contain L Message.");
            throw new IllegalStateException("Cube response does not contain L Message.");
        }

        return (LMaxMessage) lMessage.get();
    }

    @Override
    public List<MaxMessage> getInitialMessages() {
        List<MaxMessage> messages = new ArrayList<>();

        this.processCubeLines(line -> messages.add(this.processor.processMessage(line)));

        return messages;
    }

    private void processCubeLines(Consumer<String> lineConsumer) {
        logger.debug("Calling Cube.");
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(5 * 1000);
            socket.connect(new InetSocketAddress(this.host, this.port), 5 * 1000);

            // TODO: Blah...
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.append("q:\r\n");
            out.flush();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    logger.debug("\t{}", line);
                    lineConsumer.accept(line);
                }

                logger.debug("All lines read.");
            }
        } catch (IOException e) {
            logger.error("Cube call error", e);
            throw new IllegalStateException("Cube call error", e);
        }
        logger.debug("Cube called.");
    }
}
