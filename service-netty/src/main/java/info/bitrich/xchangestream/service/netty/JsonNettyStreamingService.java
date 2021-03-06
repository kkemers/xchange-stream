package info.bitrich.xchangestream.service.netty;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import info.bitrich.xchangestream.service.netty.strategy.HeartbeatStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;

public abstract class JsonNettyStreamingService extends NettyStreamingService<JsonNode> {
    private static final Logger LOG = LoggerFactory.getLogger(JsonNettyStreamingService.class);

    public JsonNettyStreamingService(String apiUrl) {
        super(apiUrl);
    }

    public JsonNettyStreamingService(String apiUrl, int maxFramePayloadLength) {
        super(apiUrl, maxFramePayloadLength);
    }

    public JsonNettyStreamingService(String apiUrl, int maxFramePayloadLength,
                                     Duration connectionTimeout, Duration retryDuration,
                                     HeartbeatStrategy heartbeatStrategy) {
        super(apiUrl, maxFramePayloadLength, connectionTimeout, retryDuration, heartbeatStrategy);
    }

    @Override
    public void messageHandler(String message) {
        LOG.trace("=>: {}", message);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;

        // Parse incoming message to JSON
        try {
            jsonNode = objectMapper.readTree(message);
        } catch (IOException e) {
            LOG.error("Error parsing incoming message to JSON: {}", message);
            return;
        }

        // In case of array - handle every message separately.
        if (jsonNode.getNodeType().equals(JsonNodeType.ARRAY)) {
            for (JsonNode node : jsonNode) {
                handleMessage(node);
            }
        } else {
            handleMessage(jsonNode);
        }
    }
}
