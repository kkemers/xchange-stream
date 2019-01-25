package info.bitrich.xchangestream.huobi;

import info.bitrich.xchangestream.core.ProductSubscription;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.core.StreamingPrivateDataService;
import info.bitrich.xchangestream.huobi.private_api.HuobiPrivateStreamingService;
import info.bitrich.xchangestream.huobi.private_api.HuobiStreamingPrivateDataService;
import info.bitrich.xchangestream.huobi.public_api.HuobiPublicStreamingService;
import info.bitrich.xchangestream.huobi.public_api.HuobiStreamingMarketDataService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.huobi.HuobiExchange;

public class HuobiStreamingExchange extends HuobiExchange implements StreamingExchange {

    private static final String PUBLIC_API_URI = "wss://api.huobi.pro/ws";
    private static final String PRIVATE_API_URI = "wss://api.huobi.pro/ws/v1";

    private final HuobiPublicStreamingService publicStreamingService;
    private final HuobiPrivateStreamingService privateStreamingService;
    private final HuobiStreamingMarketDataService streamingMarketDataService;
    private final HuobiStreamingPrivateDataService streamingPrivateDataService;

    private StreamType streamType = StreamType.BOTH;

    /**
     * Enum to set what type stream are we using
     *
     * It's needed because Huobi uses two connections, one for market data, one for client private data, and use this
     * enum as parameter at connect allows client to create only the needed one.
     */
    public enum StreamType {
        PUBLIC,
        PRIVATE,
        BOTH
    }

    public HuobiStreamingExchange() {
        this.publicStreamingService = new HuobiPublicStreamingService(this, PUBLIC_API_URI);
        this.privateStreamingService = new HuobiPrivateStreamingService(this, PRIVATE_API_URI);

        streamingMarketDataService = new HuobiStreamingMarketDataService(publicStreamingService);
        streamingPrivateDataService = new HuobiStreamingPrivateDataService(privateStreamingService);
    }

    protected HuobiStreamingExchange(HuobiPublicStreamingService publicStreamingService,
                                     HuobiPrivateStreamingService privateStreamingService) {
        this.publicStreamingService = publicStreamingService;
        this.privateStreamingService = privateStreamingService;

        streamingMarketDataService = new HuobiStreamingMarketDataService(publicStreamingService);
        streamingPrivateDataService = new HuobiStreamingPrivateDataService(privateStreamingService);
    }

    @Override
    public Completable connect(ProductSubscription... args) {
        return connect(StreamType.BOTH);
    }

    public Completable connect(StreamType streamType) {
        this.streamType = streamType;

        switch (streamType) {
            case PUBLIC:
                return publicStreamingService.connect();
            case PRIVATE:
                return privateStreamingService.connect();
            case BOTH:
                return publicStreamingService.connect().andThen(privateStreamingService.connect());
            default:
                throw new IllegalArgumentException(String.format("Unknown stream type: %s", streamType));
        }
    }

    @Override
    public Completable disconnect() {
        return publicStreamingService.disconnect().andThen(privateStreamingService.disconnect());
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
    public boolean isAlive() {
        return publicStreamingService.isSocketOpen();
    }

    @Override
    public Observable<Boolean> ready() {
        return publicStreamingService.connected();
    }

    @Override
    public void useCompressedMessages(boolean compressedMessages) {
        publicStreamingService.useCompressedMessages(compressedMessages);
    }


}
