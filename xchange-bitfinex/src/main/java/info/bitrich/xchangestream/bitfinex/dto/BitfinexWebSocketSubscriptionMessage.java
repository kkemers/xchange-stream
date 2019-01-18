package info.bitrich.xchangestream.bitfinex.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Lukas Zaoralek on 7.11.17.
 */
public class BitfinexWebSocketSubscriptionMessage {

    private static final String EVENT = "event";
    private static final String CHANNEL = "channel";
    private static final String PAIR = "pair";
    private static final String PREC = "prec";
    private static final String LEN = "len";
    private static final String FREQ = "freq";

    @JsonProperty(EVENT)
    private String event;

    @JsonProperty(CHANNEL)
    private String channel;

    @JsonProperty(PAIR)
    private String pair;

    @JsonProperty(PREC)
    private String prec;

    @JsonProperty(FREQ)
    private String freq;

    @JsonProperty(LEN)
    private Integer len;

    public BitfinexWebSocketSubscriptionMessage(String channel, Object... args) {

        this.event = "subscribe";
        this.channel = channel;

        if (args.length > 0) {
            this.pair = (String) args[0];
        }
        if (args.length > 1) {
            this.prec = (String) args[1];
        }
        if (args.length > 2) {
            this.freq = (String) args[2];
        }
        if (args.length > 3) {
            this.len = (Integer) args[3];
        }
        if (args.length > 4) {
            throw new IllegalArgumentException(String.format("Unexpected arguments count: %d", args.length));
        }
    }

    public String getEvent() {
        return event;
    }

    public String getChannel() {
        return channel;
    }

    public String getPair() {
        return pair;
    }

    public String getPrec() {
        return prec;
    }

    public String getFreq() {
        return freq;
    }

    public Integer getLen() {
        return len;
    }
}
