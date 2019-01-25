package info.bitrich.xchangestream.huobi.private_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HuobiPingRequest {

    private final Long ping;

    public HuobiPingRequest() {
        this.ping = System.currentTimeMillis();
    }

    @JsonProperty("ts")
    public Long getPing() {
        return ping;
    }

    @JsonProperty("op")
    public String getOperation() {
        return "ping";
    }
}
