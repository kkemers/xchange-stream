package info.bitrich.xchangestream.huobi.integration;

import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.huobi.HuobiStreamingExchange;
import info.bitrich.xchangestream.huobi.private_api.HuobiStreamingPrivateDataService;
import io.reactivex.observers.TestObserver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.knowm.xchange.dto.Order;

import java.util.concurrent.TimeUnit;

public class HuobiStreamingPrivateDataServiceTests {

    private HuobiStreamingExchange streamingExchange;
    private HuobiStreamingPrivateDataService streamingDataService;

    @Before
    public void setup() {
        streamingExchange = StreamingExchangeFactory.INSTANCE.createExchange(HuobiStreamingExchange.class,
                "19c7a13a-349d4c5e-edf4abde-00566", "2b18dda8-806e7ff3-e832fe30-a264c");

        streamingExchange.connect(HuobiStreamingExchange.StreamType.PRIVATE).blockingAwait();
        streamingDataService =
                (HuobiStreamingPrivateDataService) streamingExchange.getStreamingPrivateDataService();
    }

    @After
    public void cleanup() {
        if (streamingExchange.isAlive()) {
            streamingExchange.disconnect().blockingAwait();
        }
    }

    @Test
    public void getOrders() {
        TestObserver<Order> observer =
                streamingDataService.getOrders().take(1).test();

        observer.awaitTerminalEvent(50, TimeUnit.SECONDS);
        observer.assertNoErrors();
        observer.assertValueCount(1);
    }
}
