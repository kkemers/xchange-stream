package info.bitrich.xchangestream.bitfinex;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.bitfinex.dto.BitfinexOrderbookUpdate;
import info.bitrich.xchangestream.bitfinex.dto.BitfinexRawOrderbookUpdate;
import io.reactivex.Observable;

public class BitfinexStreamingMarketDataServiceRaw {
    private final BitfinexStreamingService service;

    public BitfinexStreamingMarketDataServiceRaw(BitfinexStreamingService service) {
        this.service = service;
    }

    public Observable<BitfinexOrderbookUpdate> getBitfinexOrderBookUpdates(String currencyPair, String precision,
                                                                           String frequency, Integer length) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String channelName = "book";
        return service.subscribeChannel(channelName, currencyPair, precision, frequency, length)
                .map(jsonNode -> mapper.readValue(jsonNode.toString(), BitfinexOrderbookUpdate.class));
    }

    public Observable<BitfinexRawOrderbookUpdate> getBitfinexRawOrderBookUpdates(String currencyPair, Integer length) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        String channelName = "book";
        return service.subscribeChannel(channelName, currencyPair, "R0", null, length)
                .map(jsonNode -> mapper.readValue(jsonNode.toString(), BitfinexRawOrderbookUpdate.class));
    }
}
