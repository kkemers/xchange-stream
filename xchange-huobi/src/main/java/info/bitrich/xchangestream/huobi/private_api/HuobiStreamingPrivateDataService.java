package info.bitrich.xchangestream.huobi.private_api;

import info.bitrich.xchangestream.core.StreamingPrivateDataService;
import io.reactivex.Observable;
import org.knowm.xchange.dto.Order;

public class HuobiStreamingPrivateDataService
        extends HuobiStreamingPrivateDataServiceRaw
        implements StreamingPrivateDataService {

    public HuobiStreamingPrivateDataService(HuobiPrivateStreamingService service) {
        super(service);
    }

    @Override
    public Observable<Order> getOrders() {
        return getHuobiOrders().map(HuobiAdapters::adaptOrder);
    }
}
