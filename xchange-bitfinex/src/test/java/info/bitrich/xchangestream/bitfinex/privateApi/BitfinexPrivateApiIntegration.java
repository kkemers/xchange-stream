package info.bitrich.xchangestream.bitfinex.privateApi;

import info.bitrich.xchangestream.bitfinex.BitfinexStreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import io.reactivex.observers.TestObserver;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.trade.params.CancelAllOrders;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public class BitfinexPrivateApiIntegration {

    private BitfinexProperties properties = new BitfinexProperties();
    private StreamingExchange exchange;

    public BitfinexPrivateApiIntegration() throws IOException {
    }

    @Before
    public void setup() throws IOException {
        properties = new BitfinexProperties();
        Assume.assumeTrue("Ignore tests because credentials are missing", properties.isValid());

        exchange =
                StreamingExchangeFactory.INSTANCE.createExchange(
                        BitfinexStreamingExchange.class.getName(), properties.getApiKey(), properties.getSecretKey());
    }

    @After
    public void teardown() throws IOException {
        if (exchange != null) {
            exchange.getTradeService().cancelOrder(new CancelAllOrders() {});
            exchange.disconnect().blockingAwait();
        }
    }

    @Test
    public void PlaceLimitOrders() throws IOException {
        LimitOrder limitOrder1 =
                new LimitOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USD)
                        .originalAmount(new BigDecimal("0.02"))
                        .limitPrice(new BigDecimal("5000.0"))
                        .build();
        LimitOrder limitOrder2 =
                new LimitOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USD)
                        .originalAmount(new BigDecimal("0.02"))
                        .limitPrice(new BigDecimal("5001.0"))
                        .build();

        TestObserver<Order> observer = exchange.getStreamingPrivateDataService().getOrders().take(2).test();

        exchange.connect().blockingAwait();
        exchange.getTradeService().cancelOrder(new CancelAllOrders() {});
        exchange.getTradeService().placeLimitOrder(limitOrder1);
        exchange.getTradeService().placeLimitOrder(limitOrder2);

        observer.awaitTerminalEvent(10, TimeUnit.SECONDS);
        observer.assertNoErrors();
        observer.assertValueCount(2);
        observer.assertValueAt(0, order -> {
            LimitOrder limitOrder = (LimitOrder) order;
            return limitOrder.getOriginalAmount().compareTo(new BigDecimal("0.02")) == 0 &&
                    limitOrder.getLimitPrice().compareTo(new BigDecimal("5000.0")) == 0;
        });
        observer.assertValueAt(1, order -> {
            LimitOrder limitOrder = (LimitOrder) order;
            return limitOrder.getOriginalAmount().compareTo(new BigDecimal("0.02")) == 0 &&
                    limitOrder.getLimitPrice().compareTo(new BigDecimal("5001.0")) == 0;
        });
    }
}
