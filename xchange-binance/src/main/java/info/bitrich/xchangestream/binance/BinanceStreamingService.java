package info.bitrich.xchangestream.binance;

import com.fasterxml.jackson.databind.JsonNode;
import info.bitrich.xchangestream.binance.netty.BinanceWebSocketClientExtensionHandler;
import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class BinanceStreamingService extends JsonNettyStreamingService {
    private static final Logger LOG = LoggerFactory.getLogger(BinanceStreamingService.class);

    private ProductSubscription productSubscription;

    public BinanceStreamingService(String baseUri, ProductSubscription productSubscription) {
        super(baseUri, Integer.MAX_VALUE);
        this.productSubscription = productSubscription;
    }

    @Override
    public void messageHandler(String message) {
        super.messageHandler(message);
    }

    @Override
    protected void handleMessage(JsonNode message) {
        super.handleMessage(message);
    }

    @Override
    protected String getChannelNameFromMessage(JsonNode message) throws IOException {
        return message.get("stream").asText();
    }

    @Override
    public String getSubscribeMessage(String channelName, Object... args) throws IOException {
        // No op. Disconnecting from the web socket will cancel subscriptions.
        return null;
    }

    @Override
    public String getUnsubscribeMessage(String channelName) throws IOException {
        // No op. Disconnecting from the web socket will cancel subscriptions.
        return null;
    }

    @Override
    public void sendMessage(String message) {
        // Subscriptions are made upon connection - no messages are sent.
    }

    /**
     * The available subscriptions for this streaming service.
     * @return The subscriptions for the currently open connection.
     */
    public ProductSubscription getProductSubscription() {
        return productSubscription;
    }

    /**
     * Using our own extension handler to fix handshake error:
     *  "io.netty.handler.codec.CodecException: invalid WebSocket Extension handshake for
     *  "permessage-deflate; server_no_context_takeover; client_no_context_takeover"
     */
    @Override
    protected WebSocketClientExtensionHandler getWebSocketClientExtensionHandler() {
        return BinanceWebSocketClientExtensionHandler.INSTANCE;
    }
}
