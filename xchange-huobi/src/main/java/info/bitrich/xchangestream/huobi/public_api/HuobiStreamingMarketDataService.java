package info.bitrich.xchangestream.huobi.public_api;

import info.bitrich.xchangestream.core.StreamingMarketDataService;
import io.reactivex.Observable;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.OrderBookUpdate;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;

public class HuobiStreamingMarketDataService
        extends HuobiStreamingMarketDataServiceRaw
        implements StreamingMarketDataService {

    public HuobiStreamingMarketDataService(HuobiPublicStreamingService service) {
        super(service);
    }

    /**
     * Order book stream
     *
     * @param currencyPair Trading instrument
     * @param args[0]      aka "Merged Depth". Optional. From 0 (doesn't merge) to 5 (full merge)
     * @see "https://github.com/huobiapi/API_Docs_en/wiki/WS_api_reference_en#subscribe-market-depth-data-"
     * <p>
     * When the user selects "Merged Depth", the market pending orders
     * within the certain quotation accuracy will be combined and displayed.
     * The merged depth only changes the display mode and does not change the actual order price
     * <p>
     */
    @Override
    public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
        return getHuobiOrderBook(currencyPair, args)
                .map(orderBook -> HuobiAdapters.adaptOrderBook(currencyPair, orderBook));
    }

    /**
     * Ticker subscription. Build based on order book.
     *
     * @param currencyPair Trading instrument
     * @param args         Unused
     */
    @Override
    public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
        return getHuobiOrderBook(currencyPair, args)
                .map(orderBook -> HuobiAdapters.adaptOrderBookToTicker(currencyPair, orderBook));
    }

    @Override
    public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
        return getHuobiTrade(currencyPair)
                .map(trade -> HuobiAdapters.adaptTrade(currencyPair, trade));
    }

    /**
     * @implSpec Not implemented in exchange. Huobi sends full book, without updates
     */
    @Override
    public Observable<OrderBookUpdate> getOrderBookUpdates(CurrencyPair currencyPair, Object... args) {
        throw new NotAvailableFromExchangeException();
    }
}
