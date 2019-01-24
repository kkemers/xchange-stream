package info.bitrich.xchangestream.huobi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.huobi.dto.HuobiPongMessage;
import info.bitrich.xchangestream.huobi.dto.HuobiSubscribeRequest;
import info.bitrich.xchangestream.huobi.dto.HuobiUnsubscribeRequest;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import org.knowm.xchange.exceptions.ExchangeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;

public class HuobiStreamingService extends JsonNettyStreamingService {

    private static final Logger LOG = LoggerFactory.getLogger(HuobiStreamingService.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final Map<String, String> subscriptionRequests = new ConcurrentHashMap<>();

    public HuobiStreamingService(String apiUrl) {
        super(apiUrl, Integer.MAX_VALUE);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    protected String getChannelNameFromMessage(JsonNode message) throws IOException {
        return message.get("ch").asText();
    }

    @Override
    public String getSubscribeMessage(String channelName, Object... args) throws IOException {
        HuobiSubscribeRequest message = new HuobiSubscribeRequest(channelName);
        subscriptionRequests.put(message.getId(), channelName);
        return mapper.writeValueAsString(message);
    }

    @Override
    public String getUnsubscribeMessage(String channelName, Object... args) throws IOException {
        HuobiUnsubscribeRequest message = new HuobiUnsubscribeRequest(channelName);
        return mapper.writeValueAsString(message);
    }

    @Override
    protected void messageHandler(ByteBuf message) {

        StringBuilder output = new StringBuilder();

        ByteBufInputStream stream = new ByteBufInputStream(message);
        try (InputStream gzipInputStream = new GZIPInputStream(stream)) {
            InputStreamReader streamReader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8);
            try (BufferedReader bufferedReader = new BufferedReader(streamReader)) {

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    output.append(line);
                }

                super.messageHandler(output.toString());
            }
        } catch (IOException e) {
            LOG.error("Encoded message unpack error: %s", e.getMessage(), e);
        }
    }

    @Override
    protected void handleMessage(JsonNode message) {

        LOG.debug("=>: {}", message);

        if (handleErrorIfExists(message)) {
            return;
        }

        if (handlePingIfExists(message)) {
            return;
        }

        JsonNode subbedNode = message.get("subbed");
        if (subbedNode != null) {
            String channel = subbedNode.asText();
            String id = message.get("id").asText();
            subscriptionRequests.remove(id);
            LOG.info("Subscription to '{}' is successful", channel);
            return;
        }

        JsonNode unsubbedNode = message.get("unsubbed");
        if (unsubbedNode != null) {
            String channel = unsubbedNode.asText();
            String id = message.get("id").asText();
            LOG.info("Unsubscribe from '{}' is successful", channel);
            return;
        }

        super.handleMessage(message);
    }

    private boolean handleErrorIfExists(JsonNode message) {

        JsonNode status = message.get("status");
        if (status != null && !status.asText().equals("ok")) {

            String id = message.get("id").asText();
            String errCode = message.get("err-code").asText();
            String errMessage = message.get("err-msg").asText();

            String channel = subscriptionRequests.remove(id);
            if (channel == null) {
                LOG.error("Got error from exchange for unknown request {}: {}", id, errMessage);
                return true;
            }

            handleChannelError(channel, new ExchangeException(String.format("%s: %s", errCode, errMessage)));
            return true;
        }

        return false;
    }

    private boolean handlePingIfExists(JsonNode message) {

        JsonNode ping = message.get("ping");
        if (ping != null) {
            try {
                sendMessage(mapper.writeValueAsString(new HuobiPongMessage(ping.asLong())));
            } catch (JsonProcessingException e) {
                LOG.error("Fail to serialize pong message: {}", e.getMessage(), e);
            }
            return true;
        }

        return false;
    }
}
