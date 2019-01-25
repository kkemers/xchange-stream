package info.bitrich.xchangestream.huobi.private_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HuobiPongMessage {

    private final Long pong;

    public HuobiPongMessage(Long pong) {
        this.pong = pong;
    }

    @JsonProperty("ts")
    public Long getPong() {
        return pong;
    }

    @JsonProperty("op")
    public String getOperation() {
        return "pong";
    }
}
