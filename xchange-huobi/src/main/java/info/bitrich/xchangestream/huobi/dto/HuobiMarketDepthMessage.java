package info.bitrich.xchangestream.huobi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public class HuobiMarketDepthMessage extends HuobiMessage {

    private final HuobiMarketDepthData data;

    public HuobiMarketDepthMessage(@JsonProperty("ch") String channel,
                                   @JsonProperty("ts") Long ts,
                                   @JsonProperty("tick") HuobiMarketDepthData data) {
        super(channel, ts);
        this.data = data;
    }

    public HuobiMarketDepthData getData() {
        return data;
    }

    public static class HuobiMarketDepthData {

        private final Long ts;
        private final Long version;

        private final List<HuobiMarketDepthLevel> bids;
        private final List<HuobiMarketDepthLevel> asks;

        public HuobiMarketDepthData(
                @JsonProperty("ts") Long ts,
                @JsonProperty("version") Long version,
                @JsonProperty("bids") List<HuobiMarketDepthLevel> bids,
                @JsonProperty("asks") List<HuobiMarketDepthLevel> asks) {
            this.ts = ts;
            this.version = version;
            this.bids = bids;
            this.asks = asks;
        }

        public Long getTs() {
            return ts;
        }

        public Long getVersion() {
            return version;
        }

        public List<HuobiMarketDepthLevel> getBids() {
            return bids;
        }

        public List<HuobiMarketDepthLevel> getAsks() {
            return asks;
        }

        @Override
        public String toString() {
            final StringBuffer buffer = new StringBuffer("{");
            buffer.append("bids=").append(bids);
            buffer.append(", asks=").append(asks);
            buffer.append('}');
            return buffer.toString();
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class HuobiMarketDepthLevel {
        private final BigDecimal price;
        private final BigDecimal amount;

        public HuobiMarketDepthLevel(@JsonProperty("price") BigDecimal price,
                                     @JsonProperty("amount") BigDecimal amount) {
            this.price = price;
            this.amount = amount;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            final StringBuffer buffer = new StringBuffer("{");
            buffer.append("price=").append(price);
            buffer.append(", amount=").append(amount);
            buffer.append('}');
            return buffer.toString();
        }
    }

}
