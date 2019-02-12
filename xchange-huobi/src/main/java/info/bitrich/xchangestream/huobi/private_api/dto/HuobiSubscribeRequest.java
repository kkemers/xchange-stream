package info.bitrich.xchangestream.huobi.private_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class HuobiSubscribeRequest {

    private final String id;
    private final String topic;

    public HuobiSubscribeRequest(String topic) {
        this.id = UUID.randomUUID().toString();
        this.topic = topic;
    }

    @JsonProperty("cid")
    public String getId() {
        return id;
    }

    @JsonProperty("op")
    public String getOperation() {
        return "sub";
    }

    public String getTopic() {
        return topic;
    }
}
