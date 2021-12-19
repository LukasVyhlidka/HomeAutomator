package org.vyhlidka.homeautomation.eq3;

import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Component;
import org.vyhlidka.homeautomation.eq3.domain.MaxMessage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MessageProcessorImpl implements MessageProcessor {

    private static final Pattern msgPattern = Pattern.compile("^(\\w):.+$");

    private final Map<String, MessageParser> parsers = new HashMap<>();

    public MessageProcessorImpl(List<MessageParser> parsers) {
        Validate.notNull(parsers, "parsers can not be null;");

        for (MessageParser parser : parsers) {
            String type = parser.getMessageType();
            if (this.parsers.containsKey(type)) {
                throw new IllegalStateException("Parser for message type " + type + " already exists.");
            }

            this.parsers.put(type, parser);
        }
    }

    @Override
    public MaxMessage processMessage(String message) {
        Validate.notNull(message, "message can not be null;");

        Matcher mat = msgPattern.matcher(message);
        if (!mat.matches()) {
            throw new IllegalArgumentException("Message [" + message + "] is not valid eQ-3 MAX Cube message.");
        }

        String msgType = mat.group(1);
        if (this.parsers.containsKey(msgType)) {
            MessageParser parser = this.parsers.get(msgType);
            return parser.parse(message);
        }

        // Default message type
        return new MaxMessage(msgType, message);
    }

}
