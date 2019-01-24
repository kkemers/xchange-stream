package info.bitrich.xchangestream.huobi;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.huobi.dto.HuobiMarketDepthMessage;
import info.bitrich.xchangestream.huobi.utils.HuobiAdapters;
import io.reactivex.Observable;
import org.knowm.xchange.currency.CurrencyPair;

public class HuobiStreamingMarketDataServiceRaw {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HuobiStreamingService service;

    public HuobiStreamingMarketDataServiceRaw(HuobiStreamingService service) {
        this.service = service;
    }

    public Observable<HuobiMarketDepthMessage> getHuobiOrderBook(CurrencyPair currencyPair, Object... args) {

        int mergedDepth;
        if (args.length == 0) {
            mergedDepth = 0;
        } else if (args.length == 1) {
            mergedDepth = (Integer) args[0];
        } else {
            throw new IllegalArgumentException(String.format("Got more arguments then expected: %s", args));
        }

        if (mergedDepth < 0 || mergedDepth > 5) {
            throw new IllegalArgumentException(String.format("Merged depth %d is not in range [0 - 5]", args));
        }

        String topic = String.format("market.%s.depth.step%d",
                HuobiAdapters.adaptCurrencyPair(currencyPair), mergedDepth);
        return service.subscribeChannel(topic)
                .map(node -> mapper.readValue(node.toString(), HuobiMarketDepthMessage.class));
    }

}
