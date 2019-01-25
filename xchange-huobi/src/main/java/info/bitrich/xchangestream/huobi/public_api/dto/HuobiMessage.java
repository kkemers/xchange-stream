package info.bitrich.xchangestream.huobi.public_api.dto;

abstract public class HuobiMessage {

    private final String channel;
    private final Long ts;

    public HuobiMessage(String channel, Long ts) {
        this.channel = channel;
        this.ts = ts;
    }

    public String getChannel() {
        return channel;
    }

    public Long getTs() {
        return ts;
    }
}
