package info.bitrich.xchangestream.huobi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HuobiPongMessage {

    private final Long pong;

    public HuobiPongMessage(Long pong) {
        this.pong = pong;
    }

    @JsonProperty("pong")
    public Long getPong() {
        return pong;
    }
}
