package info.bitrich.xchangestream.huobi.private_api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.huobi.private_api.dto.HuobiPongMessage;
import info.bitrich.xchangestream.huobi.private_api.dto.HuobiSubscribeRequest;
import info.bitrich.xchangestream.huobi.private_api.dto.HuobiUnsubscribeRequest;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import org.knowm.xchange.ExchangeSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class HuobiPrivateStreamingService extends JsonNettyStreamingService {

    static final String ORDERS_TOPIC = "orders.*";

    private static final Logger LOG = LoggerFactory.getLogger(HuobiPrivateStreamingService.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final HuobiAuthorization authorization = new HuobiAuthorization();

    private final StreamingExchange exchange;

    private CompletableEmitter authEmitter;
    private String apiKey;
    private String secretKey;

    public HuobiPrivateStreamingService(StreamingExchange exchange, String apiUrl) {
        super(apiUrl, Integer.MAX_VALUE);
        this.exchange = exchange;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Completable connect() {
        try {
            prepareCredentials();
        } catch (Exception e) {
            return Completable.error(e);
        }

        return super.connect();
    }

    @Override
    public Completable authorize() {
        return Completable.create(emitter -> {
            Map<String, String> request = authorization.calcSignature(apiKey, secretKey);
            String requestString = mapper.writeValueAsString(request);
            sendMessage(requestString);

            authEmitter = emitter;
        });
    }

    @Override
    protected String getChannelNameFromMessage(JsonNode message) {
        return message.get("topic").asText();
    }

    @Override
    public String getSubscribeMessage(String channelName, Object... args) throws IOException {
        HuobiSubscribeRequest message = new HuobiSubscribeRequest(channelName);
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

        JsonNode op = message.get("op");

        try {
            switch (op.asText()) {
                case "ping":
                    handlePing(message);
                    return;
                case "error":
                    handleError(message);
                    return;
                case "auth":
                    handleAuth(message);
                    return;
                case "sub":
                    handleErrorIfExists(message, "Subscribe");
                    return;
                case "unsub":
                    handleErrorIfExists(message, "Unsubscribe");
                    return;
                case "notify":
                    super.handleMessage(message);
                    return;
                case "close":
                    handleClose();
                    return;
                default:
                    LOG.error("Got unexpected message: {}", message);
            }
        } catch (JsonProcessingException e) {
            LOG.error("Fail to serialize response: {}", e.getMessage(), e);
        }
    }

    private void handlePing(JsonNode message) throws JsonProcessingException {
        long ts = message.get("ts").asLong();

        String pong = mapper.writeValueAsString(new HuobiPongMessage(ts));
        sendMessage(pong);
    }

    private void handleError(JsonNode message) {
        LOG.error("Got error from exchange: {}", message);
    }

    private void handleAuth(JsonNode message) {

        long errCode = message.get("err-code").asLong();
        if (errCode == 0) {
            long userId = message.get("data").get("user-id").asLong();
            LOG.info("Authorization is successful. Logged with id {}", userId);
            authEmitter.onComplete();
            return;
        }

        LOG.error("Authorisation error: {}", message);

        String errMessage = message.get("err-msg").asText();
        authEmitter.onError(new Exception(String.format("Authorisation error: %s", errMessage)));
    }

    private void handleErrorIfExists(JsonNode message, String scope) {

        long errCode = message.get("err-code").asLong();
        String channel = message.get("topic").asText();

        if (errCode == 0) {
            LOG.info("Subscription to '{}' is successful", channel);
            return;
        }

        LOG.error("Subscription error: {}", message);

        String errMessage = message.get("err-msg").asText();
        handleChannelError(channel, new Exception(String.format("%s error: %s", scope, errMessage)));
    }

    private void handleClose() {
        LOG.warn("Exchange closes the connection");
    }

    private void prepareCredentials() throws Exception {
        ExchangeSpecification specification = exchange.getExchangeSpecification();
        if (specification == null) {
            throw new Exception("Exchange has no specification");
        }

        apiKey = specification.getApiKey();
        secretKey = specification.getSecretKey();

        if (apiKey == null || secretKey == null) {
            throw new Exception("Credentials are not defined");
        }
    }
}
