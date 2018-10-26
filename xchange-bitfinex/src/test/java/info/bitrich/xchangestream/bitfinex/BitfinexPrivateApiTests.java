package info.bitrich.xchangestream.bitfinex;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BitfinexPrivateApiTests {

    private final BitfinexStreamingService streamingService = mock(BitfinexStreamingService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private BitfinexStreamingExchange exchange;

    @Before
    public void setUp() {
        exchange = new BitfinexStreamingExchange(streamingService);
    }

    @Test
    public void testGetOrderExecution_orderSnapshot() throws Exception {

        JsonNode jsonSnapshot = objectMapper.readTree(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("orders-snapshot.json"));
        when(streamingService.subscribeChannel("os")).thenReturn(Observable.just(jsonSnapshot));
        when(streamingService.subscribeChannel("on")).thenReturn(Observable.empty());
        when(streamingService.subscribeChannel("ou")).thenReturn(Observable.empty());
        when(streamingService.subscribeChannel("oc")).thenReturn(Observable.empty());

        TestObserver<Order> test = exchange.getStreamingPrivateDataService().getOrders().test();

        test.assertNoErrors();
        test.awaitCount(2);

        LimitOrder expected1 = new LimitOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USD)
                .id("11111111111")
                .currencyPair(CurrencyPair.BTC_USD)
                .orderType(Order.OrderType.BID)
                .orderStatus(Order.OrderStatus.NEW)
                .timestamp(new Date(1539692596308L))
                .originalAmount(new BigDecimal("0.02"))
                .remainingAmount(new BigDecimal("0.01"))
                .limitPrice(new BigDecimal("5000.0"))
                .build();
        LimitOrder expected2 = new LimitOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USD)
                .id("11111111112")
                .currencyPair(CurrencyPair.BTC_USD)
                .orderType(Order.OrderType.BID)
                .orderStatus(Order.OrderStatus.NEW)
                .timestamp(new Date(1539692596309L))
                .originalAmount(new BigDecimal("0.02"))
                .remainingAmount(new BigDecimal("0.02"))
                .limitPrice(new BigDecimal("5000.0"))
                .build();

        test.assertValues(expected1, expected2);
    }

    @Test
    public void testGetOrderExecution_orderPlaceUpdate() throws Exception {

        JsonNode jsonUpdate = objectMapper.readTree(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("orders-update.json"));
        when(streamingService.subscribeChannel("os")).thenReturn(Observable.empty());
        when(streamingService.subscribeChannel("on")).thenReturn(Observable.just(jsonUpdate));
        when(streamingService.subscribeChannel("ou")).thenReturn(Observable.empty());
        when(streamingService.subscribeChannel("oc")).thenReturn(Observable.empty());

        TestObserver<Order> test = exchange.getStreamingPrivateDataService().getOrders().test();

        test.assertNoErrors();
        test.awaitCount(1);

        LimitOrder expected = new LimitOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USD)
                .id("22222222222")
                .currencyPair(CurrencyPair.BTC_USD)
                .orderType(Order.OrderType.ASK)
                .orderStatus(Order.OrderStatus.NEW)
                .timestamp(new Date(1540205421889L))
                .originalAmount(new BigDecimal("0.02"))
                .remainingAmount(new BigDecimal("0.02"))
                .limitPrice(new BigDecimal("5000.0"))
                .build();

        test.assertValues(expected);
    }
}
