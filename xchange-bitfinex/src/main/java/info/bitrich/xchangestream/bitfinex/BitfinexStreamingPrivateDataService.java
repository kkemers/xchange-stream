package info.bitrich.xchangestream.bitfinex;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.bitfinex.dto.BitfinexWebSocketOrder;
import info.bitrich.xchangestream.core.StreamingPrivateDataService;
import io.reactivex.Observable;
import org.knowm.xchange.dto.Order;

public class BitfinexStreamingPrivateDataService implements StreamingPrivateDataService {

    private final ObjectMapper mapper = new ObjectMapper();

    private final BitfinexStreamingService streamingService;

    public BitfinexStreamingPrivateDataService(BitfinexStreamingService streamingService) {
        this.streamingService = streamingService;
    }

    @Override
    public Observable<Order> getOrders() {

        Observable<BitfinexWebSocketOrder> orderSnapshot = streamingService.subscribeChannel("os")
                .map(json -> mapper.convertValue(json.get(2), BitfinexWebSocketOrder[].class))
                .flatMap(Observable::fromArray);
        Observable<BitfinexWebSocketOrder> orderPlaced = streamingService.subscribeChannel("on")
                .map(json -> mapper.convertValue(json.get(2), BitfinexWebSocketOrder.class));
        Observable<BitfinexWebSocketOrder> orderUpdated = streamingService.subscribeChannel("ou")
                .map(json -> mapper.convertValue(json.get(2), BitfinexWebSocketOrder.class));
        Observable<BitfinexWebSocketOrder> orderCanceled = streamingService.subscribeChannel("oc")
                .map(json -> mapper.convertValue(json.get(2), BitfinexWebSocketOrder.class));

        return Observable.merge(orderSnapshot, orderPlaced, orderUpdated, orderCanceled)
                .map(BitfinexStreamingAdapters::adaptOrder);
    }
}
