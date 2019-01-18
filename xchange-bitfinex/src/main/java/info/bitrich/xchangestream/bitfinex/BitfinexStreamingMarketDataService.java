package info.bitrich.xchangestream.bitfinex;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.bitfinex.dto.*;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.Observable;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.knowm.xchange.bitfinex.v1.BitfinexAdapters.*;

/**
 * Created by Lukas Zaoralek on 7.11.17.
 */
public class BitfinexStreamingMarketDataService extends BitfinexStreamingMarketDataServiceRaw
        implements StreamingMarketDataService {

    private final BitfinexStreamingService service;

    private Map<CurrencyPair, BitfinexOrderbook> orderbooks = new ConcurrentHashMap<>();

    public BitfinexStreamingMarketDataService(BitfinexStreamingService service) {
        super(service);
        this.service = service;
    }

    @Override
    public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
        return getOrderBookUpdatesRaw(currencyPair, args)
                .map(orderBookUpdate -> {
                    BitfinexOrderbook orderBook =
                            updateOrCreateOrderBook(orderbooks, currencyPair, orderBookUpdate.getLevels());

                    return adaptOrderBook(orderBook.toBitfinexDepth(), currencyPair);
                });
    }

    @Override
    public Observable<OrderBookUpdate> getOrderBookUpdates(CurrencyPair currencyPair, Object... args) {
        return getOrderBookUpdatesRaw(currencyPair, args)
                .flatMap(update -> Observable.fromIterable(
                        BitfinexStreamingAdapters.adaptOrderBookUpdates(update, currencyPair)));
    }

    @Override
    public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
        String channelName = "ticker";

        String pair = currencyPair.base.toString() + currencyPair.counter.toString();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Observable<BitfinexWebSocketTickerTransaction> subscribedChannel = service.subscribeChannel(channelName,
                new Object[]{pair})
                .map(s -> mapper.readValue(s.toString(), BitfinexWebSocketTickerTransaction.class));

        return subscribedChannel
                .map(s -> adaptTicker(s.toBitfinexTicker(), currencyPair));
    }

    @Override
    public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
        String channelName = "trades";
        final String tradeType = args.length > 0 ? args[0].toString() : "te";

        String pair = currencyPair.base.toString() + currencyPair.counter.toString();
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Observable<BitfinexWebSocketTradesTransaction> subscribedChannel = service.subscribeChannel(channelName,
                new Object[]{pair})
                .filter(s -> s.get(1).asText().equals(tradeType))
                .map(s -> {
                    if (s.get(1).asText().equals("te") || s.get(1).asText().equals("tu")) {
                        return mapper.readValue(s.toString(), BitfinexWebsocketUpdateTrade.class);
                    } else return mapper.readValue(s.toString(), BitfinexWebSocketSnapshotTrades.class);
                });

        return subscribedChannel
                .flatMapIterable(s -> {
                    Trades adaptedTrades = adaptTrades(s.toBitfinexTrades(), currencyPair);
                    return adaptedTrades.getTrades();
                });
    }

    private Observable<BitfinexOrderbookUpdate> getOrderBookUpdatesRaw(CurrencyPair currencyPair, Object... args) {

        String pair = currencyPair.base.toString() + currencyPair.counter.toString();
        Integer length = args.length > 0 ? (Integer) args[0] : 100;
        String frequency = args.length > 1 ? args[1].toString() : "F0";
        String precision = args.length > 2 ? args[2].toString() : "P0";

        return getBitfinexOrderBookUpdates(pair, precision, frequency, length);
    }

    private BitfinexOrderbook updateOrCreateOrderBook(Map<CurrencyPair, BitfinexOrderbook> orderbooks,
                                                      CurrencyPair currencyPair,
                                                      BitfinexOrderbookLevel[] updatedLevels) {
        BitfinexOrderbook orderBook;

        if (orderbooks.containsKey(currencyPair)) {
            orderBook = orderbooks.get(currencyPair);
            updateOrderBook(orderBook, updatedLevels);
        } else {
            orderBook = new BitfinexOrderbook(updatedLevels);
            orderbooks.put(currencyPair, orderBook);
        }

        return orderBook;
    }

    private void updateOrderBook(BitfinexOrderbook orderBook, BitfinexOrderbookLevel[] orderBookUpdatesLevels) {
        for (BitfinexOrderbookLevel orderBookLevel : orderBookUpdatesLevels) {
            orderBook.updateLevel(orderBookLevel);
        }
    }
}
