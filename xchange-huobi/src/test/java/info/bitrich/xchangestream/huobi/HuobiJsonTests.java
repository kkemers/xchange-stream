package info.bitrich.xchangestream.huobi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.huobi.private_api.HuobiPrivateStreamingService;
import info.bitrich.xchangestream.huobi.public_api.HuobiPublicStreamingService;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

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
    public void testGetOrderExecution_orderPlace() throws Exception {

        JsonNode jsonNode = objectMapper.readTree(ClassLoader.getSystemClassLoader()
                .getResourceAsStream("market-depth.json"));

        when(publicStreamingService.subscribeChannel("market.btcusdt.depth.step1")).thenReturn(Observable.just(jsonNode));

        TestObserver<OrderBook> observer = exchange.getStreamingMarketDataService()
                .getOrderBook(CurrencyPair.BTC_USDT, 1).test();

        observer.assertNoErrors();
        observer.assertValueCount(1);
    }
}

