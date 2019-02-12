package info.bitrich.xchangestream.huobi.public_api;

import info.bitrich.xchangestream.huobi.public_api.dto.HuobiMarketDepthMessage;
import info.bitrich.xchangestream.huobi.public_api.dto.HuobiTradeDetailsMessage;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class HuobiAdapters {

    public static String adaptCurrencyPair(CurrencyPair currencyPair) {
        return currencyPair.base.toString().toLowerCase() + currencyPair.counter.toString().toLowerCase();
    }

    public static OrderBook adaptOrderBook(CurrencyPair currencyPair, HuobiMarketDepthMessage orderBook) {

        List<LimitOrder> asks = adaptOrderBookLevels(currencyPair, Order.OrderType.ASK, orderBook.getData().getAsks());
        List<LimitOrder> bids = adaptOrderBookLevels(currencyPair, Order.OrderType.BID, orderBook.getData().getBids());
        return new OrderBook(new Date(orderBook.getData().getTs()), asks, bids);
    }

    public static Trade adaptTrade(CurrencyPair currencyPair, HuobiTradeDetailsMessage.HuobiTradeDetails trade) {
        return new Trade.Builder()
                .id(trade.getId().toString())
                .timestamp(new Date(trade.getTs()))
                .currencyPair(currencyPair)
                .type(adaptOrderType(trade.getDirection()))
                .originalAmount(trade.getAmount())
                .price(trade.getPrice())
                .build();
    }

    private static Order.OrderType adaptOrderType(String direction) {
        switch (direction) {
            case "buy":
                return Order.OrderType.BID;
            case "sell":
                return Order.OrderType.ASK;
            default:
                throw new IllegalArgumentException(String.format("Unexpected direction: %s", direction));
        }
    }

    private static List<LimitOrder> adaptOrderBookLevels(CurrencyPair currencyPair, Order.OrderType orderType,
                                                         List<HuobiMarketDepthMessage.HuobiMarketDepthLevel> levels) {
        return levels.stream()
                .map(level -> adaptOrderBookLevel(currencyPair, orderType, level))
                .collect(Collectors.toList());
    }

    private static LimitOrder adaptOrderBookLevel(CurrencyPair currencyPair, Order.OrderType orderType,
                                                  HuobiMarketDepthMessage.HuobiMarketDepthLevel level) {
        return new LimitOrder.Builder(orderType, currencyPair)
                .limitPrice(level.getPrice())
                .originalAmount(level.getAmount())
                .build();
    }

    public static Ticker adaptOrderBookToTicker(CurrencyPair currencyPair, HuobiMarketDepthMessage orderBook) {

        List<HuobiMarketDepthMessage.HuobiMarketDepthLevel> bids = orderBook.getData().getBids();
        List<HuobiMarketDepthMessage.HuobiMarketDepthLevel> asks = orderBook.getData().getAsks();

        BigDecimal bidPrice = null;
        BigDecimal bidAmount = null;
        if (!bids.isEmpty()) {
            HuobiMarketDepthMessage.HuobiMarketDepthLevel bid = bids.get(0);
            bidPrice = bid.getPrice();
            bidAmount = bid.getAmount();
        }

        BigDecimal askPrice = null;
        BigDecimal askAmount = null;
        if (!asks.isEmpty()) {
            HuobiMarketDepthMessage.HuobiMarketDepthLevel ask = asks.get(0);
            askPrice = ask.getPrice();
            askAmount = ask.getAmount();
        }

        return new Ticker.Builder()
                .currencyPair(currencyPair)
                .timestamp(new Date(orderBook.getData().getTs()))
                .bid(bidPrice)
                .ask(askPrice)
                .bidSize(bidAmount)
                .askSize(askAmount)
                .build();
    }
}
