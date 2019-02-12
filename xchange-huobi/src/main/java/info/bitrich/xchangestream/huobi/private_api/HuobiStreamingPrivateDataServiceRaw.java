package info.bitrich.xchangestream.huobi.private_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.huobi.private_api.dto.HuobiNotifyMessage;
import io.reactivex.Observable;

public class HuobiStreamingPrivateDataServiceRaw {

    private final ObjectMapper mapper = new ObjectMapper();

    private final HuobiPrivateStreamingService service;

    public HuobiStreamingPrivateDataServiceRaw(HuobiPrivateStreamingService service) {
        this.service = service;
    }

    Observable<HuobiNotifyMessage> getHuobiOrders() {
        return service.subscribeChannel(HuobiPrivateStreamingService.ORDERS_TOPIC)
                .map(node -> mapper.readValue(node.toString(), HuobiNotifyMessage.class));
    }
}
