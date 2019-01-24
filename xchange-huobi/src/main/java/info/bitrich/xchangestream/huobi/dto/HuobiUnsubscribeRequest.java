package info.bitrich.xchangestream.huobi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class HuobiUnsubscribeRequest {

    private final String id;
    private final String topic;

    public HuobiUnsubscribeRequest(String topic) {
        this.id = UUID.randomUUID().toString();
        this.topic = topic;
    }

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("unsub")
    public String getTopic() {
        return topic;
    }
}
