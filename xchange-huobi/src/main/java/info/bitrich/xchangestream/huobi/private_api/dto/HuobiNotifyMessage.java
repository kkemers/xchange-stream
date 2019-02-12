package info.bitrich.xchangestream.huobi.private_api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class HuobiNotifyMessage {

    private final String op;
    private final Long ts;
    private final String topic;

    private final Data data;

    public HuobiNotifyMessage(@JsonProperty("op") String op,
                              @JsonProperty("ts") Long ts,
                              @JsonProperty("topic") String topic,
                              @JsonProperty("data") Data data) {
        this.op = op;
        this.ts = ts;
        this.topic = topic;
        this.data = data;
    }

    public String getOp() {
        return op;
    }

    public Long getTs() {
        return ts;
    }

    public String getTopic() {
        return topic;
    }

    public Data getData() {
        return data;
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer("{");
        buffer.append("op='").append(op).append('\'');
        buffer.append(", ts=").append(ts);
        buffer.append(", topic='").append(topic).append('\'');
        buffer.append(", data=").append(data);
        buffer.append('}');
        return buffer.toString();
    }

    public static class Data {
        private final Long seqId;
        private final Long orderId;
        private final String symbol;
        private final Long accountId;
        private final BigDecimal orderPrice;
        private final BigDecimal orderAmount;
        private final Long createdAt;
        private final String orderType;
        private final String orderSource;
        private final String orderState;
        private final String role;
        private final BigDecimal price;
        private final BigDecimal filledAmount;
        private final BigDecimal unfilledAmount;
        private final BigDecimal filledCashAmount;
        private final BigDecimal filledFees;

        public Data(@JsonProperty("seq-id") Long seqId,
                    @JsonProperty("order-id") Long orderId,
                    @JsonProperty("symbol") String symbol,
                    @JsonProperty("account-id") Long accountId,
                    @JsonProperty("order-price") BigDecimal orderPrice,
                    @JsonProperty("order-amount") BigDecimal orderAmount,
                    @JsonProperty("created-at") Long createdAt,
                    @JsonProperty("order-type") String orderType,
                    @JsonProperty("order-source") String orderSource,
                    @JsonProperty("order-state") String orderState,
                    @JsonProperty("role") String role,
                    @JsonProperty("price") BigDecimal price,
                    @JsonProperty("filled-amount") BigDecimal filledAmount,
                    @JsonProperty("unfilled-amount") BigDecimal unfilledAmount,
                    @JsonProperty("filled-cash-amount") BigDecimal filledCashAmount,
                    @JsonProperty("filled-fees") BigDecimal filledFees) {
            this.seqId = seqId;
            this.orderId = orderId;
            this.symbol = symbol;
            this.accountId = accountId;
            this.orderPrice = orderPrice;
            this.orderAmount = orderAmount;
            this.createdAt = createdAt;
            this.orderType = orderType;
            this.orderSource = orderSource;
            this.orderState = orderState;
            this.role = role;
            this.price = price;
            this.filledAmount = filledAmount;
            this.unfilledAmount = unfilledAmount;
            this.filledCashAmount = filledCashAmount;
            this.filledFees = filledFees;
        }

        public Long getSeqId() {
            return seqId;
        }

        public Long getOrderId() {
            return orderId;
        }

        public String getSymbol() {
            return symbol;
        }

        public Long getAccountId() {
            return accountId;
        }

        public BigDecimal getOrderPrice() {
            return orderPrice;
        }

        public BigDecimal getOrderAmount() {
            return orderAmount;
        }

        public Long getCreatedAt() {
            return createdAt;
        }

        public String getOrderType() {
            return orderType;
        }

        public String getOrderSource() {
            return orderSource;
        }

        public String getOrderState() {
            return orderState;
        }

        public String getRole() {
            return role;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public BigDecimal getFilledAmount() {
            return filledAmount;
        }

        public BigDecimal getUnfilledAmount() {
            return unfilledAmount;
        }

        public BigDecimal getFilledCashAmount() {
            return filledCashAmount;
        }

        public BigDecimal getFilledFees() {
            return filledFees;
        }

        @Override
        public String toString() {
            final StringBuffer buffer = new StringBuffer("{");
            buffer.append("seqId=").append(seqId);
            buffer.append(", orderId=").append(orderId);
            buffer.append(", symbol='").append(symbol).append('\'');
            buffer.append(", accountId=").append(accountId);
            buffer.append(", orderPrice=").append(orderPrice);
            buffer.append(", orderAmount=").append(orderAmount);
            buffer.append(", createdAt=").append(createdAt);
            buffer.append(", orderType='").append(orderType).append('\'');
            buffer.append(", orderSource='").append(orderSource).append('\'');
            buffer.append(", orderState='").append(orderState).append('\'');
            buffer.append(", role='").append(role).append('\'');
            buffer.append(", price=").append(price);
            buffer.append(", filledAmount=").append(filledAmount);
            buffer.append(", unfilledAmount=").append(unfilledAmount);
            buffer.append(", filledCashAmount=").append(filledCashAmount);
            buffer.append(", filledFees=").append(filledFees);
            buffer.append('}');
            return buffer.toString();
        }
    }

}
