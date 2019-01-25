package info.bitrich.xchangestream.huobi.private_api;

import info.bitrich.xchangestream.huobi.private_api.dto.HuobiNotifyMessage;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;

import java.math.BigDecimal;
import java.util.Date;

public class HuobiAdapters {

    private enum Type {
        MARKET,
        LIMIT
    }

    public static Order adaptOrder(HuobiNotifyMessage message) {

        HuobiNotifyMessage.Data data = message.getData();

        Type type = adaptType(data.getOrderType());

        // TODO: Describe amounts

        if (type == Type.LIMIT) {
            LimitOrder.Builder builder = (LimitOrder.Builder)
                    new LimitOrder.Builder(adaptOrderType(data.getOrderType()), adaptCurrencyPair(data.getSymbol()))
                            .id(data.getOrderId().toString())
                            .timestamp(new Date(data.getCreatedAt()))
                            .originalAmount(data.getOrderAmount())
                            .remainingAmount(data.getUnfilledAmount())
                            .limitPrice(data.getOrderPrice())
                            .orderStatus(adaptOrderStatus(data.getOrderState()));
            LimitOrder limitOrder = builder.build();
            limitOrder.setFee(data.getFilledFees());
            return limitOrder;
        }

        if (type == Type.MARKET) {
            MarketOrder.Builder builder = (MarketOrder.Builder)
                    new MarketOrder.Builder(adaptOrderType(data.getOrderType()), adaptCurrencyPair(data.getSymbol()))
                            .id(data.getOrderId().toString())
                            .timestamp(new Date(data.getCreatedAt()))
                            .originalAmount(data.getFilledAmount())
                            .cumulativeAmount(data.getFilledAmount())
                            .remainingAmount(BigDecimal.ZERO)
                            .orderStatus(adaptOrderStatus(data.getOrderState()))
                            .averagePrice(data.getPrice());
            MarketOrder marketOrder = builder.build();
            marketOrder.setFee(data.getFilledFees());
            return marketOrder;
        }

        throw new IllegalArgumentException("Unexpected data");
    }

    private static Order.OrderStatus adaptOrderStatus(String state) {
        switch (state) {
            case "submitted":
                return Order.OrderStatus.NEW;
            case "partial-filled":
                return Order.OrderStatus.PARTIALLY_FILLED;
            case "partial-canceled":
                return Order.OrderStatus.PARTIALLY_CANCELED;
            case "filled":
                return Order.OrderStatus.FILLED;
            case "canceled":
                return Order.OrderStatus.CANCELED;
            default:
                throw new IllegalArgumentException(String.format("Unknown order state: %s", state));
        }
    }

    private static CurrencyPair adaptCurrencyPair(String symbol) {
        String base = symbol.substring(0, 3).toUpperCase();
        String counter = symbol.substring(3).toUpperCase();
        return new CurrencyPair(base, counter);
    }

    private static Order.OrderType adaptOrderType(String orderType) {
        switch (orderType) {
            case "buy-market":
            case "buy-limit":
            case "buy-ioc":
            case "buy-limit-maker":
                return Order.OrderType.BID;
            case "sell-market":
            case "sell-limit":
            case "sell-ioc":
            case "sell-limit-maker":
                return Order.OrderType.ASK;
            default:
                throw new IllegalArgumentException(String.format("Unknown order type: %s", orderType));

        }
    }

    private static Type adaptType(String orderType) {
        switch (orderType) {
            case "buy-market":
            case "sell-market":
                return Type.MARKET;
            case "buy-limit":
            case "buy-limit-maker":
            case "sell-limit":
            case "sell-limit-maker":
            case "buy-ioc":
            case "sell-ioc":
                return Type.LIMIT;
            default:
                throw new IllegalArgumentException(String.format("Unknown order type: %s", orderType));
        }
    }
}
