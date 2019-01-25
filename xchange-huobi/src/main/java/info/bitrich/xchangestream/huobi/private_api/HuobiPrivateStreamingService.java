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
import io.reactivex.subjects.CompletableSubject;
import org.knowm.xchange.ExchangeSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class HuobiPrivateStreamingService extends JsonNettyStreamingService {

    static final String ORDERS_TOPIC = "orders.*";

    private static final Logger LOG = LoggerFactory.getLogger(HuobiPrivateStreamingService.class);

    private final ObjectMapper mapper = new ObjectMapper();
    private final HuobiAuthorization authorization = new HuobiAuthorization();

    private final StreamingExchange exchange;

    private final CompletableSubject authSubject = CompletableSubject.create();

    public HuobiPrivateStreamingService(StreamingExchange exchange, String apiUrl) {
        super(apiUrl, Integer.MAX_VALUE);
        this.exchange = exchange;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public Completable connect() {
        return super.connect().doOnComplete(this::auth).andThen(authSubject);
    }

    @Override
    public Completable reconnect() {
        return super.reconnect().doOnComplete(this::auth).andThen(authSubject);
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

        LOG.debug("=>: {}", message);

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
                    handleSubscription(message);
                    return;
                case "notify":
                    super.handleMessage(message);
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
            LOG.info("Authorisation is successful. Logged with id {}", userId);
            authSubject.onComplete();
            return;
        }

        LOG.error("Authorisation error: {}", message);

        String errMessage = message.get("err-msg").asText();
        authSubject.onError(new Exception(String.format("Authorisation error: %s", errMessage)));
    }

    private void handleSubscription(JsonNode message) {

        long errCode = message.get("err-code").asLong();
        String channel = message.get("topic").asText();

        if (errCode == 0) {
            LOG.info("Subscription to '{}' is successful", channel);
            return;
        }

        LOG.error("Subscription error: {}", message);

        String errMessage = message.get("err-msg").asText();
        handleChannelError(channel, new Exception(String.format("Subscription error: %s", errMessage)));
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

    private boolean handlePongIfExists(JsonNode message) {

        JsonNode pong = message.get("pong");
        if (pong != null) {
            long pongTime = pong.asLong();
            LOG.debug("Ping responded at {}ms", System.currentTimeMillis() - pongTime);
            return true;
        }

        return false;
    }

    private void auth() throws NoSuchAlgorithmException, InvalidKeyException, JsonProcessingException {
        ExchangeSpecification specification = exchange.getExchangeSpecification();
        if (specification == null) {
            LOG.debug("Exchange has no specification. Skip authorisation");
            authSubject.onComplete();
            return;
        }

        String apiKey = specification.getApiKey();
        String secretKey = specification.getSecretKey();

        if (apiKey == null || secretKey == null) {
            LOG.debug("Credentials are not defined. Skip authorisation");
            authSubject.onComplete();
            return;
        }

        Map<String, String> request = authorization.calcSignature(apiKey, secretKey);
        String requestString = mapper.writeValueAsString(request);

        sendMessage(requestString);
    }

}
