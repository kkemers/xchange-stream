package info.bitrich.xchangestream.huobi.public_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HuobiPingRequest {

    private final Long ping;

    public HuobiPingRequest() {
        this.ping = System.currentTimeMillis();
    }

    @JsonProperty("ping")
    public Long getPing() {
        return ping;
    }
}
