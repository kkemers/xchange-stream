package info.bitrich.xchangestream.huobi.integration;

import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.huobi.HuobiStreamingExchange;
import info.bitrich.xchangestream.huobi.public_api.HuobiStreamingMarketDataService;
import io.reactivex.observers.TestObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.either;
import static org.hamcrest.CoreMatchers.is;

public class HuobiStreamingMarketDataServiceTests {

    private HuobiStreamingExchange streamingExchange;
    private HuobiStreamingMarketDataService streamingMarketDataService;

    @Before
    public void setup() {
        streamingExchange = StreamingExchangeFactory.INSTANCE.createExchange(HuobiStreamingExchange.class);
        streamingExchange.connect(HuobiStreamingExchange.StreamType.PUBLIC).blockingAwait();
        streamingMarketDataService =
                (HuobiStreamingMarketDataService) streamingExchange.getStreamingMarketDataService();
    }

    @After
    public void cleanup() {
        if (streamingExchange.isAlive()) {
            streamingExchange.disconnect().blockingAwait();
        }
    }

    @Test
    public void getOrderBook() {
        TestObserver<OrderBook> observer =
                streamingMarketDataService.getOrderBook(CurrencyPair.BTC_USDT).take(10).test();

        observer.awaitTerminalEvent(50, TimeUnit.SECONDS);
        observer.assertNoErrors();
        observer.assertValueCount(10);

        OrderBook orderBook = observer.values().get(0);
        LimitOrder ask0 = orderBook.getAsks().get(0);
        Assert.assertEquals(ask0.getCurrencyPair(), CurrencyPair.BTC_USDT);
        Assert.assertTrue(ask0.getLimitPrice().compareTo(BigDecimal.ZERO) > 0);
        Assert.assertTrue(orderBook.getBids().size() > 0);
        Assert.assertTrue(orderBook.getAsks().size() > 0);
    }

    @Test
    public void getTrades() {
        TestObserver<Trade> observer =
                streamingMarketDataService.getTrades(CurrencyPair.BTC_USDT).take(10).test();

        observer.awaitTerminalEvent(50, TimeUnit.SECONDS);
        observer.assertNoErrors();
        observer.assertValueCount(10);

        Trade trade = observer.values().get(0);

        Assert.assertTrue(!trade.getId().isEmpty());
        Assert.assertEquals(trade.getCurrencyPair(), CurrencyPair.BTC_USDT);
        Assert.assertTrue(trade.getPrice().compareTo(BigDecimal.ZERO) > 0);
        Assert.assertTrue(trade.getOriginalAmount().compareTo(BigDecimal.ZERO) > 0);
        Assert.assertThat(trade.getType(), either(is(Order.OrderType.BID)).or(is(Order.OrderType.ASK)));
    }
}
