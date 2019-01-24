package info.bitrich.xchangestream.huobi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class HuobiTradeDetailsMessage extends HuobiMessage {

    private final HuobiTradeDetailsData data;

    public HuobiTradeDetailsMessage(@JsonProperty("ch") String channel,
                                    @JsonProperty("ts") Long ts,
                                    @JsonProperty("tick") HuobiTradeDetailsData data) {
        super(channel, ts);
        this.data = data;
    }

    public HuobiTradeDetailsData getData() {
        return data;
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer("{");
        buffer.append("data=").append(data);
        buffer.append('}');
        return buffer.toString();
    }

    public static class HuobiTradeDetailsData {
        private final Long id;
        private final Long ts;
        private final List<HuobiTradeDetails> data;

        public HuobiTradeDetailsData(@JsonProperty("id") Long id,
                                     @JsonProperty("ts") Long ts,
                                     @JsonProperty("data") List<HuobiTradeDetails> data) {
            this.id = id;
            this.ts = ts;
            this.data = data;
        }

        public Long getId() {
            return id;
        }

        public Long getTs() {
            return ts;
        }

        public List<HuobiTradeDetails> getData() {
            return data;
        }

        @Override
        public String toString() {
            final StringBuffer buffer = new StringBuffer("{");
            buffer.append("id=").append(id);
            buffer.append(", ts=").append(ts);
            buffer.append(", data=").append(data);
            buffer.append('}');
            return buffer.toString();
        }
    }

    public static class HuobiTradeDetails {
        private final BigInteger id;
        private final Long ts;
        private final BigDecimal amount;
        private final BigDecimal price;
        private final String direction;

        public HuobiTradeDetails(@JsonProperty("id") BigInteger id,
                                 @JsonProperty("ts") Long ts,
                                 @JsonProperty("amount") BigDecimal amount,
                                 @JsonProperty("price") BigDecimal price,
                                 @JsonProperty("direction") String direction) {
            this.id = id;
            this.ts = ts;
            this.amount = amount;
            this.price = price;
            this.direction = direction;
        }

        public BigInteger getId() {
            return id;
        }

        public Long getTs() {
            return ts;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public String getDirection() {
            return direction;
        }

        @Override
        public String toString() {
            final StringBuffer buffer = new StringBuffer("{");
            buffer.append("id=").append(id);
            buffer.append(", ts=").append(ts);
            buffer.append(", amount=").append(amount);
            buffer.append(", price=").append(price);
            buffer.append(", direction='").append(direction).append('\'');
            buffer.append('}');
            return buffer.toString();
        }
    }
}
