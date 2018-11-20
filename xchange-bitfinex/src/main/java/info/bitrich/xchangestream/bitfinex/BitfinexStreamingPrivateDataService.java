package info.bitrich.xchangestream.bitfinex;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.bitfinex.dto.BitfinexWebSocketOrder;
import info.bitrich.xchangestream.core.StreamingPrivateDataService;
import io.reactivex.Observable;
import org.knowm.xchange.dto.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitfinexStreamingPrivateDataService implements StreamingPrivateDataService {

    private static final Logger log = LoggerFactory.getLogger(BitfinexStreamingPrivateDataService.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final BitfinexStreamingService streamingService;

    public BitfinexStreamingPrivateDataService(BitfinexStreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @Override
    public Observable<Order> getOrders() {

        Observable<BitfinexWebSocketOrder> orderSnapshot = streamingService.subscribeChannel("os")
                .map(json -> mapper.convertValue(json.get(2), BitfinexWebSocketOrder[].class))
                .doOnNext(order -> log.debug("Order update: {}", (Object[]) order))
                .flatMap(Observable::fromArray);
        Observable<BitfinexWebSocketOrder> on = streamingService.subscribeChannel("on")
                .map(json -> mapper.convertValue(json.get(2), BitfinexWebSocketOrder.class));
        Observable<BitfinexWebSocketOrder> ou = streamingService.subscribeChannel("ou")
                .map(json -> mapper.convertValue(json.get(2), BitfinexWebSocketOrder.class));
        Observable<BitfinexWebSocketOrder> oc = streamingService.subscribeChannel("oc")
                .map(json -> mapper.convertValue(json.get(2), BitfinexWebSocketOrder.class));

        return Observable.merge(orderSnapshot, on, ou, oc)
                .map(BitfinexStreamingAdapters::adaptOrder);
    }
}
