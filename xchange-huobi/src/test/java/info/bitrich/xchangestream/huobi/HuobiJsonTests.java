package info.bitrich.xchangestream.huobi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.huobi.private_api.HuobiPrivateStreamingService;
import info.bitrich.xchangestream.huobi.public_api.HuobiPublicStreamingService;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.MarketOrder;

import java.math.BigDecimal;
import java.util.Date;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HuobiJsonTests {

    private final HuobiPublicStreamingService publicStreamingService = mock(HuobiPublicStreamingService.class);
    private final HuobiPrivateStreamingService privateStreamingService = mock(HuobiPrivateStreamingService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private HuobiStreamingExchange exchange;

    @Before
    public void setUp() {
        exchange = new HuobiStreamingExchange(publicStreamingService, privateStreamingService);
    }

    @Test
    public void testMarketDepth() throws Exception {

        JsonNode jsonNode = objectMapper.readTree(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("market-depth.json"));

        when(publicStreamingService.subscribeChannel("market.btcusdt.depth.step1")).thenReturn(Observable.just(jsonNode));

        TestObserver<OrderBook> observer = exchange.getStreamingMarketDataService()
                .getOrderBook(CurrencyPair.BTC_USDT, 1).test();

        observer.assertNoErrors();
        observer.assertValueCount(1);
    }

    @Test
    public void testGetOrder_MarketBuy() throws Exception {

        JsonNode jsonNode = objectMapper.readTree(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("order-market-buy.json"));

        when(privateStreamingService.subscribeChannel("orders.*")).thenReturn(Observable.just(jsonNode));

        TestObserver<Order> observer = exchange.getStreamingPrivateDataService().getOrders().test();

        observer.assertNoErrors();
        observer.assertValueCount(1);

        MarketOrder order = (MarketOrder) observer.values().get(0);
        Assert.assertEquals(order.getId(), "22969278037");
        Assert.assertEquals(order.getType(), Order.OrderType.BID);
        Assert.assertEquals(order.getCurrencyPair(), new CurrencyPair(Currency.BTC, Currency.USDT));
        Assert.assertEquals(order.getOriginalAmount(), new BigDecimal("0.000281383831846703"));
        Assert.assertTrue(order.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0);
        Assert.assertEquals(order.getAveragePrice(), new BigDecimal("3553.864461355386443569"));
        Assert.assertEquals(order.getFee(), new BigDecimal("0.000000562767663693"));
        Assert.assertEquals(order.getTimestamp(), new Date(1548422011691L));
        Assert.assertEquals(order.getStatus(), Order.OrderStatus.FILLED);
    }

    @Test
    public void testGetOrder_MarketSell() throws Exception {

        JsonNode jsonNode = objectMapper.readTree(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("order-market-sell.json"));

        when(privateStreamingService.subscribeChannel("orders.*")).thenReturn(Observable.just(jsonNode));

        TestObserver<Order> observer = exchange.getStreamingPrivateDataService().getOrders().test();

        observer.assertNoErrors();
        observer.assertValueCount(1);

        MarketOrder order = (MarketOrder) observer.values().get(0);
        Assert.assertEquals(order.getId(), "22968928526");
        Assert.assertEquals(order.getType(), Order.OrderType.ASK);
        Assert.assertEquals(order.getCurrencyPair(), new CurrencyPair(Currency.BTC, Currency.USDT));
        Assert.assertEquals(order.getOriginalAmount(), new BigDecimal("0.0002"));
        Assert.assertTrue(order.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0);
        Assert.assertEquals(order.getAveragePrice(), new BigDecimal("3551.53"));
        Assert.assertEquals(order.getFee(), new BigDecimal("0.001420612"));
        Assert.assertEquals(order.getTimestamp(), new Date(1548421634708L));
        Assert.assertEquals(order.getStatus(), Order.OrderStatus.FILLED);
    }

    @Test
    public void testGetOrder_LimitBuy() throws Exception {

        JsonNode jsonNode = objectMapper.readTree(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("order-limit-buy.json"));

        when(privateStreamingService.subscribeChannel("orders.*")).thenReturn(Observable.just(jsonNode));

        TestObserver<Order> observer = exchange.getStreamingPrivateDataService().getOrders().test();

        observer.assertNoErrors();
        observer.assertValueCount(1);

        LimitOrder order = (LimitOrder) observer.values().get(0);
        Assert.assertEquals(order.getId(), "22970020355");
        Assert.assertEquals(order.getType(), Order.OrderType.BID);
        Assert.assertEquals(order.getCurrencyPair(), new CurrencyPair(Currency.BTC, Currency.USDT));
        Assert.assertEquals(order.getOriginalAmount(), new BigDecimal("0.0002"));
        Assert.assertEquals(order.getRemainingAmount(), new BigDecimal("0.0002"));
        Assert.assertEquals(order.getAveragePrice(), new BigDecimal("3247.66"));
        Assert.assertEquals(order.getLimitPrice(), new BigDecimal("3247.66"));
        Assert.assertEquals(order.getFee(), BigDecimal.ZERO);
        Assert.assertEquals(order.getTimestamp(), new Date(1548422818512L));
        Assert.assertEquals(order.getStatus(), Order.OrderStatus.NEW);
    }

    @Test
    public void testGetOrder_LimitSell() throws Exception {

        JsonNode jsonNode = objectMapper.readTree(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("order-limit-sell.json"));

        when(privateStreamingService.subscribeChannel("orders.*")).thenReturn(Observable.just(jsonNode));

        TestObserver<Order> observer = exchange.getStreamingPrivateDataService().getOrders().test();

        observer.assertNoErrors();
        observer.assertValueCount(1);

        LimitOrder order = (LimitOrder) observer.values().get(0);
        Assert.assertEquals(order.getId(), "22970079595");
        Assert.assertEquals(order.getType(), Order.OrderType.ASK);
        Assert.assertEquals(order.getCurrencyPair(), new CurrencyPair(Currency.BTC, Currency.USDT));
        Assert.assertEquals(order.getOriginalAmount(), new BigDecimal("0.0002"));
        Assert.assertEquals(order.getRemainingAmount(), new BigDecimal("0.0002"));
        Assert.assertEquals(order.getAveragePrice(), new BigDecimal("3747.28"));
        Assert.assertEquals(order.getLimitPrice(), new BigDecimal("3747.28"));
        Assert.assertEquals(order.getFee(), BigDecimal.ZERO);
        Assert.assertEquals(order.getTimestamp(), new Date(1548422881876L));
        Assert.assertEquals(order.getStatus(), Order.OrderStatus.NEW);
    }

    @Test
    public void testGetOrder_CancelLimit() throws Exception {

        JsonNode jsonNode = objectMapper.readTree(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("order-limit-cancel.json"));

        when(privateStreamingService.subscribeChannel("orders.*")).thenReturn(Observable.just(jsonNode));

        TestObserver<Order> observer = exchange.getStreamingPrivateDataService().getOrders().test();

        observer.assertNoErrors();
        observer.assertValueCount(1);

        LimitOrder order = (LimitOrder) observer.values().get(0);
        Assert.assertEquals(order.getId(), "22970141207");
        Assert.assertEquals(order.getType(), Order.OrderType.ASK);
        Assert.assertEquals(order.getCurrencyPair(), new CurrencyPair(Currency.BTC, Currency.USDT));
        Assert.assertEquals(order.getOriginalAmount(), new BigDecimal("0.0002"));
        Assert.assertEquals(order.getRemainingAmount(), new BigDecimal("0.0002"));
        Assert.assertEquals(order.getAveragePrice(), new BigDecimal("3747.28"));
        Assert.assertEquals(order.getLimitPrice(), new BigDecimal("3747.28"));
        Assert.assertEquals(order.getFee(), BigDecimal.ZERO);
        Assert.assertEquals(order.getTimestamp(), new Date(1548422968218L));
        Assert.assertEquals(order.getStatus(), Order.OrderStatus.CANCELED);
    }


    @Test
    public void testGetOrder_FillLimit() throws Exception {

        JsonNode jsonNode = objectMapper.readTree(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("order-limit-fill.json"));

        when(privateStreamingService.subscribeChannel("orders.*")).thenReturn(Observable.just(jsonNode));

        TestObserver<Order> observer = exchange.getStreamingPrivateDataService().getOrders().test();

        observer.assertNoErrors();
        observer.assertValueCount(1);

        LimitOrder order = (LimitOrder) observer.values().get(0);
        Assert.assertEquals(order.getId(), "23182069040");
        Assert.assertEquals(order.getType(), Order.OrderType.BID);
        Assert.assertEquals(order.getCurrencyPair(), new CurrencyPair(Currency.BTC, Currency.USDT));
        Assert.assertEquals(order.getOriginalAmount(), new BigDecimal("0.0002"));
        Assert.assertTrue(order.getRemainingAmount().compareTo(BigDecimal.ZERO) == 0);
        Assert.assertEquals(order.getAveragePrice(), new BigDecimal("3775.74"));
        Assert.assertEquals(order.getLimitPrice(), new BigDecimal("3775.74"));
        Assert.assertEquals(order.getFee(), new BigDecimal("0.0000004"));
        Assert.assertEquals(order.getTimestamp(), new Date(1548666190545L));
        Assert.assertEquals(order.getStatus(), Order.OrderStatus.FILLED);
    }
}
