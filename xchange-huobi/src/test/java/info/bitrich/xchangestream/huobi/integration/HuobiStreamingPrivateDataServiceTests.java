package info.bitrich.xchangestream.huobi.integration;

import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.huobi.HuobiStreamingExchange;
import info.bitrich.xchangestream.huobi.private_api.HuobiStreamingPrivateDataService;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.observers.TestObserver;
import org.junit.*;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class HuobiStreamingPrivateDataServiceTests {

    private HuobiStreamingExchange streamingExchange;
    private HuobiStreamingPrivateDataService streamingDataService;

    @Before
    public void setup() throws IOException {
        HuobiProperties properties = new HuobiProperties();
        Assume.assumeTrue("Ignore tests because credentials are missing", properties.isValid());

        streamingExchange = StreamingExchangeFactory.INSTANCE.createExchange(HuobiStreamingExchange.class,
                properties.getApiKey(), properties.getSecretKey());
        streamingExchange.remoteInit();

        streamingExchange.connect(HuobiStreamingExchange.StreamType.PRIVATE).blockingAwait();
        streamingDataService =
                (HuobiStreamingPrivateDataService) streamingExchange.getStreamingPrivateDataService();

        cancelAll();
    }

    @After
    public void cleanup() throws IOException {
        if (streamingExchange.isAlive()) {
            cancelAll();
            streamingExchange.disconnect().blockingAwait();
        }
    }

    private void cancelAll() throws IOException {
        streamingExchange.getTradeService().getOpenOrders().getOpenOrders().forEach(order -> {
            try {
                streamingExchange.getTradeService().cancelOrder(order.getId());
            } catch (IOException e) {
                Exceptions.propagate(e);
            }
        });
    }

    @Test
    public void getOrders_PlaceLimitOrder_ExpectSubmit() throws IOException {

        TestObserver<Order> observer = streamingDataService.getOrders().take(1).test();

        LimitOrder order = new LimitOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USDT)
                .limitPrice(new BigDecimal("3000.12"))
                .originalAmount(new BigDecimal("0.0002"))
                .build();

        String id = streamingExchange.getTradeService().placeLimitOrder(order);

        observer.awaitTerminalEvent(30, TimeUnit.SECONDS);
        observer.assertNoErrors();
        observer.assertValueCount(1);

        observer.assertValue(newOrder -> {
            LimitOrder limitOrder = (LimitOrder) newOrder;
            Assert.assertEquals(limitOrder.getId(), id);
            Assert.assertEquals(limitOrder.getStatus(), Order.OrderStatus.NEW);
            Assert.assertEquals(limitOrder.getCurrencyPair(), CurrencyPair.BTC_USDT);
            Assert.assertEquals(limitOrder.getRemainingAmount(), new BigDecimal("0.0002"));
            Assert.assertEquals(limitOrder.getAveragePrice(), new BigDecimal("3000.12"));
            return true;
        });
    }

    @Test
    public void getOrders_PlaceLimitOrder_ExpectFill() throws IOException {

        TestObserver<Order> observer = streamingDataService.getOrders().take(1).test();

        LimitOrder order = new LimitOrder.Builder(Order.OrderType.ASK, CurrencyPair.BTC_USDT)
                .limitPrice(new BigDecimal("3086.56"))
                .originalAmount(new BigDecimal("0.0002"))
                .build();

        String id = streamingExchange.getTradeService().placeLimitOrder(order);

        observer.awaitTerminalEvent(30, TimeUnit.SECONDS);
        observer.assertNoErrors();
        observer.assertValueCount(1);

        observer.assertValue(newOrder -> {
            LimitOrder limitOrder = (LimitOrder) newOrder;
            Assert.assertEquals(limitOrder.getId(), id);
            Assert.assertEquals(limitOrder.getStatus(), Order.OrderStatus.NEW);
            Assert.assertEquals(limitOrder.getCurrencyPair(), CurrencyPair.BTC_USDT);
            Assert.assertEquals(limitOrder.getRemainingAmount(), new BigDecimal("0.0002"));
            Assert.assertEquals(limitOrder.getAveragePrice(), new BigDecimal("3086.56"));
            return true;
        });
    }
}
