package info.bitrich.xchangestream.bitfinex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.bitfinex.dto.BitfinexWebSocketAuthRequest;
import info.bitrich.xchangestream.bitfinex.dto.BitfinexWebSocketSubscriptionMessage;
import info.bitrich.xchangestream.bitfinex.dto.BitfinexWebSocketUnSubscriptionMessage;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.exceptions.ExchangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lukas Zaoralek on 7.11.17.
 */
public class BitfinexStreamingService extends JsonNettyStreamingService {
    private static final Logger LOG = LoggerFactory.getLogger(BitfinexStreamingService.class);

    private static final String INFO = "info";
    private static final String AUTH = "auth";
    private static final String ERROR = "error";
    private static final String CHANNEL_ID = "chanId";
    private static final String SUBSCRIBED = "subscribed";
    private static final String UNSUBSCRIBED = "unsubscribed";

    private static final int SUBSCRIPTION_FAILED = 10300;
    private static final String PRIVATE_CHANNEL_ID = "0";

    private final Map<String, String> subscribedChannels = new HashMap<>();
    private final StreamingExchange streamingExchange;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public BitfinexStreamingService(StreamingExchange streamingExchange, String apiUrl) {
        super(apiUrl, Integer.MAX_VALUE);
        this.streamingExchange = streamingExchange;
    }

    @Override
    public void messageHandler(String message) {
        LOG.trace("Received message: {}", message);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;

        // Parse incoming message to JSON
        try {
            jsonNode = objectMapper.readTree(message);
        } catch (IOException e) {
            LOG.error("Error parsing incoming message to JSON: {}", message);
            return;
        }

        handleMessage(jsonNode);
    }

    @Override
    public String getSubscribeMessage(String channelName, Object... args) throws IOException {
        BitfinexWebSocketSubscriptionMessage subscribeMessage = null;
        if (args.length == 1) {
            subscribeMessage =
                    new BitfinexWebSocketSubscriptionMessage(channelName, (String) args[0]);
        } else if (args.length == 3) {
            subscribeMessage =
                    new BitfinexWebSocketSubscriptionMessage(channelName, (String) args[0], (String) args[1],
                            (String) args[2]);
        } else {
            return null;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(subscribeMessage);
    }

    @Override
    public String getUnsubscribeMessage(String channelName, Object... args) throws IOException {
        String channelId = null;
        for (Map.Entry<String, String> entry : subscribedChannels.entrySet()) {
            if (entry.getValue().equals(channelName)) {
                channelId = entry.getKey();
                break;
            }
        }

        if (channelId == null) {
            return null;
        }

        BitfinexWebSocketUnSubscriptionMessage subscribeMessage = new BitfinexWebSocketUnSubscriptionMessage(channelId);
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(subscribeMessage);
    }

    @Override
    public String getSubscriptionUniqueId(String channelName, Object... args) {
        if (args.length > 0) {
            return channelName + "-" + args[0].toString();
        } else {
            return channelName;
        }
    }

    @Override
    protected WebSocketClientExtensionHandler getWebSocketClientExtensionHandler() {
        return null;
    }

    @Override
    protected void handleMessage(JsonNode message) {
        if (message.isArray()) {
            String type = message.get(1).asText();
            if (type.equals("hb")) {
                return;
            }
        }

        JsonNode event = message.get("event");
        if (event == null) {
            super.handleMessage(message);
            return;
        }

        switch (event.textValue()) {
            case INFO:
                JsonNode version = message.get("version");
                if (version != null) {
                    LOG.debug("Bitfinex websocket API version: {}.", version.intValue());
                }
                auth();
                break;
            case AUTH: {
                String status = message.get("status").asText();
                if (status.equals("OK")) {
                    LOG.info("Authorization success");
                } else {
                    int autErrorCode = message.get("code").asInt();
                    LOG.error("Authorization error [{}]: {}", autErrorCode, authCodeToMessage(autErrorCode));
                }
                break;
            }
            case SUBSCRIBED: {
                String channel = message.get("channel").asText();
                String pair = message.get("pair").asText();
                String channelId = message.get(CHANNEL_ID).asText();
                try {
                    String subscriptionUniqueId = getSubscriptionUniqueId(channel, pair);
                    subscribedChannels.put(channelId, subscriptionUniqueId);
                    LOG.debug("Register channel {}: {}", subscriptionUniqueId, channelId);
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
                break;
            }
            case UNSUBSCRIBED: {
                String channelId = message.get(CHANNEL_ID).asText();
                subscribedChannels.remove(channelId);
                break;
            }
            case ERROR:
                if (message.get("code").asInt() == SUBSCRIPTION_FAILED) {
                    LOG.error("Error with message: " + message.get("msg"));
                    return;
                }
                super.handleError(message, new ExchangeException("Error code: " + message.get("code").asText()));
                break;
        }
    }

    private String authCodeToMessage(int code) {
        switch (code) {
            case 10000:
                return "Unknown error";
            case 10001:
                return "Generic error";
            case 10008:
                return "Concurrency error";
            case 10020:
                return "Request parameters error";
            case 10050:
                return "Configuration setup failed";
            case 10100:
                return "Failed authentication";
            case 10111:
                return "Error in authentication request payload";
            case 10112:
                return "Error in authentication request signature";
            case 10113:
                return "Error in authentication request encryption";
            case 10114:
                return "Error in authentication request nonce";
            case 10200:
                return "Error in un-authentication request";
            case 10300:
                return "Failed channel subscription";
            case 10301:
                return "Failed channel subscription: already subscribed";
            case 10400:
                return "Failed channel un-subscription: channel not found";
            case 11000:
                return "Not ready, try again later";
            case 20051:
                return "Websocket server stopping... please reconnect later";
            case 20060:
                return "Websocket server resyncing... please reconnect later";
            case 20061:
                return "Websocket server resync complete. please reconnect";
            default:
                return "(unknown)";
        }
    }

    @Override
    protected String getChannelNameFromMessage(JsonNode message) throws IOException {
        String chanId;
        if (message.has(CHANNEL_ID)) {
            chanId = message.get(CHANNEL_ID).asText();
        } else {
            chanId = message.get(0).asText();
        }

        if (chanId == null) {
            throw new IOException("Can't find CHANNEL_ID value");
        }

        if (chanId.equals(PRIVATE_CHANNEL_ID)) {
            return message.get(1).asText();
        }

        return subscribedChannels.get(chanId);
    }

    private void auth() {
        ExchangeSpecification specification = streamingExchange.getExchangeSpecification();
        String apiKey = specification.getApiKey();
        String secretKey = specification.getSecretKey();

        if (apiKey == null || secretKey == null) {
            LOG.debug("Credentials are not defined. Skip authorisation");
            return;
        }

        Long nonce = streamingExchange.getNonceFactory().createValue();
        String authPayload = String.format("AUTH%d", nonce);

        BitfinexStreamingDigest digest = new BitfinexStreamingDigest(secretKey);
        String signature = digest.createSignature(authPayload);

        BitfinexWebSocketAuthRequest message =
                new BitfinexWebSocketAuthRequest(apiKey, signature, authPayload, String.valueOf(nonce),
                        "trading");
        sendMessage(message);
    }

    private void sendMessage(Object message) {
        try {
            sendMessage(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            LOG.error("Error creating json message: {}", e.getMessage());
        }
    }
}
