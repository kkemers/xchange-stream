package info.bitrich.xchangestream.huobi.integration;

import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.huobi.HuobiStreamingExchange;
import info.bitrich.xchangestream.huobi.HuobiStreamingMarketDataService;
import io.reactivex.observers.TestObserver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.util.concurrent.TimeUnit;

public class HuobiStreamingMarketDataServiceTests {

    private StreamingExchange streamingExchange;
    private HuobiStreamingMarketDataService streamingMarketDataService;

    @Before
    public void setup() {
        streamingExchange = new HuobiStreamingExchange();
        streamingExchange.useCompressedMessages(true);

        streamingExchange.connect().blockingAwait();
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
        Assert.assertTrue(orderBook.getBids().size() > 0);
        Assert.assertTrue(orderBook.getAsks().size() > 0);
    }
}
