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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by lucky on 26.12.16.
 */
@Component
public class CubeClientImpl implements CubeClient {

    private static final Logger logger = LoggerFactory.getLogger(CubeClientImpl.class);

    private CubeSocket cubeSocket;

    private List<MaxMessage> lastMMsgCache = new ArrayList<>();

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

        logger.debug("Calling Cube.");
        try {
            CubeSocket socket = this.getConnectedSocket();

            /*PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.append("q:\r\n");
            out.flush();*/

            socket.append("l:\r\n");
            socket.append("f:\r\n");

            String line;
            while ((line = socket.readLine()) != null) {
                logger.debug("\t{}", line);
                final MaxMessage maxMessage = processor.processMessage(line);
                messages.add(maxMessage);
                if ("F".equals(maxMessage.msgType)) {
                    //F should be last because it was last command we sent.
                    break;
                }
            }

            final List<MaxMessage> mMessages = messages.stream().filter(msg -> "M".equals(msg.getMessageType())).collect(Collectors.toList());
            if (mMessages.isEmpty()) {
                // no M message (it is loaded only for new connections to cube), add it from cache
                logger.debug("M message is not present, adding from cache");
                messages.addAll(this.lastMMsgCache);
            } else {
                // M message is present, add it to the cache
                logger.debug("M message is present, adding to cache");
                this.lastMMsgCache = mMessages;
            }

            logger.debug("All lines read.");

        } catch (IOException e) {
            logger.error("Cube call error", e);
            this.releaseSocket();
            throw new IllegalStateException("Cube call error", e);
        }
        logger.debug("Cube called.");

        /*this.processCubeLines(line -> {
            final MaxMessage msg = this.processor.processMessage(line);
            messages.add(msg);
            return msg.isLast();
        });*/

        return messages;
    }

    private void processCubeLines(Function<String, Boolean> lineConsumer) {

        logger.debug("Calling Cube.");
        try {
            CubeSocket socket = this.getConnectedSocket();

            /*PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.append("q:\r\n");
            out.flush();*/

            socket.append("l:\r\n");
            socket.append("f:\r\n");

            String line;
            while ((line = socket.readLine()) != null) {
                logger.debug("\t{}", line);
                boolean last = lineConsumer.apply(line);
                if (last) {
                    break;
                }
            }

            logger.debug("All lines read.");

        } catch (IOException e) {
            logger.error("Cube call error", e);
            this.releaseSocket();
            throw new IllegalStateException("Cube call error", e);
        }
        logger.debug("Cube called.");
    }

    private CubeSocket getConnectedSocket() {
        if (this.cubeSocket != null && !this.cubeSocket.isClosed()) {
            return this.cubeSocket;
        }

        this.cubeSocket = new CubeSocket(this.host, this.port);

        // TODO: Blah wait for cube data...
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return this.cubeSocket;
    }

    private void releaseSocket() {
        if (this.cubeSocket != null) {
            try {
                this.cubeSocket.close();
            } catch (IOException e) {
                logger.error("Cube client release error", e);
            }
        }
        this.cubeSocket = null;

        logger.info("Cube client released");
    }
}
