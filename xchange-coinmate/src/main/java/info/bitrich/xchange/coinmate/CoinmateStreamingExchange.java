package info.bitrich.xchange.coinmate;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.core.StreamingPrivateDataService;
import info.bitrich.xchangestream.service.pusher.PusherStreamingService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import org.knowm.xchange.coinmate.CoinmateExchange;
import org.knowm.xchange.exceptions.NotAvailableFromExchangeException;

public class CoinmateStreamingExchange extends CoinmateExchange implements StreamingExchange {
    private static final String API_KEY = "af76597b6b928970fbb0";
    private final PusherStreamingService streamingService;

    private CoinmateStreamingMarketDataService streamingMarketDataService;

    public CoinmateStreamingExchange() {
        streamingService = new PusherStreamingService(API_KEY);
    }

    @Override
    protected void initServices() {
        super.initServices();
        streamingMarketDataService = new CoinmateStreamingMarketDataService(streamingService);
    }

    @Override
    public Completable connect(ProductSubscription... args) {
        return streamingService.connect();
    }

    @Override
    public Completable disconnect() {
        return streamingService.disconnect();
    }

    @Override
    public StreamingMarketDataService getStreamingMarketDataService() {
        return streamingMarketDataService;
    }

    @Override
    public StreamingPrivateDataService getStreamingPrivateDataService() {
        throw new NotAvailableFromExchangeException();
    }

    @Override
    public boolean isAlive() {
        return streamingService.isSocketOpen();
    }

    @Override
    public Observable<Boolean> ready() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public void useCompressedMessages(boolean compressedMessages) {
        streamingService.useCompressedMessages(compressedMessages);
    }
}
