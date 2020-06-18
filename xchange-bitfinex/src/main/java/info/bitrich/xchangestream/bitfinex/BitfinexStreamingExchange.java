package info.bitrich.xchangestream.bitfinex;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.core.StreamingPrivateDataService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitfinex.BitfinexExchange;

/**
 * Created by Lukas Zaoralek on 7.11.17.
 */
public class BitfinexStreamingExchange extends BitfinexExchange implements StreamingExchange {
    private static final String API_URI = "wss://api.bitfinex.com/ws/2";

    private final BitfinexStreamingService streamingService;
    private BitfinexStreamingMarketDataService streamingMarketDataService;
    private BitfinexStreamingPrivateDataService streamingPrivateDataService;

    public BitfinexStreamingExchange() {
        this.streamingService = new BitfinexStreamingService(this, API_URI);
    }

    BitfinexStreamingExchange(BitfinexStreamingService streamingService) {
        this.streamingService = streamingService;
        streamingMarketDataService = new BitfinexStreamingMarketDataService(streamingService);
        streamingPrivateDataService = new BitfinexStreamingPrivateDataService(streamingService);
    }

    @Override
    protected void initServices() {
        super.initServices();
        streamingMarketDataService = new BitfinexStreamingMarketDataService(streamingService);
        streamingPrivateDataService = new BitfinexStreamingPrivateDataService(streamingService);
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
    public boolean isAlive() {
        return streamingService.isSocketOpen();
    }

    public Observable<Boolean> ready() {
        return streamingService.connected();
    }

    @Override
    public ExchangeSpecification getDefaultExchangeSpecification() {
        ExchangeSpecification spec = super.getDefaultExchangeSpecification();
        spec.setShouldLoadRemoteMetaData(false);

        return spec;
    }

    @Override
    public StreamingMarketDataService getStreamingMarketDataService() {
        return streamingMarketDataService;
    }

    @Override
    public StreamingPrivateDataService getStreamingPrivateDataService() {
        return streamingPrivateDataService;
    }

    @Override
    public void useCompressedMessages(boolean compressedMessages) {
        streamingService.useCompressedMessages(compressedMessages);
    }
}
