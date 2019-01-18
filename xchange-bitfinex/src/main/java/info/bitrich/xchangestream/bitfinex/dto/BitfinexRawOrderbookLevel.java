package info.bitrich.xchangestream.bitfinex.dto;

import java.math.BigDecimal;

public class BitfinexRawOrderbookLevel {
    private final String orderId;
    private final BigDecimal price;
    private final BigDecimal amount;

    public BitfinexRawOrderbookLevel(String orderId, BigDecimal price, BigDecimal amount) {
        this.orderId = orderId;
        this.price = price;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getAmount() {
        return amount;
    }
}
