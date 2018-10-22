package info.bitrich.xchangestream.bitfinex.dto;

import java.util.Arrays;
import java.util.List;

public class BitfinexWebSocketAuthRequest {

    private final String event = "auth";

    private final String apiKey;
    private final String authPayload;
    private final String authNonce;
    private final String authSig;
    private final List<String> filter;

    public BitfinexWebSocketAuthRequest(String apiKey, String authSig, String authPayload, String authNonce,
                                        String... filter) {
        this.apiKey = apiKey;
        this.authSig = authSig;
        this.authPayload = authPayload;
        this.authNonce = authNonce;
        this.filter = Arrays.asList(filter);
    }

    public String getEvent() {
        return event;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getAuthPayload() {
        return authPayload;
    }

    public String getAuthNonce() {
        return authNonce;
    }

    public String getAuthSig() {
        return authSig;
    }

    public List<String> getFilter() {
        return filter;
    }
}
