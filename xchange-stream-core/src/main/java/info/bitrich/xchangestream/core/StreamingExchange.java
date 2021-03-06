package info.bitrich.xchangestream.core;

import io.reactivex.Completable;
import io.reactivex.Observable;
import org.knowm.xchange.Exchange;

public interface StreamingExchange extends Exchange {
    /**
     * Connects to the WebSocket API of the exchange.
     *
     * @param args Product subscription is used only in certain exchanges where you need to specify subscriptions during the connect phase.
     * @return {@link Completable} that completes upon successful connection.
     */
    Completable connect(ProductSubscription... args);

    /**
     * Disconnect from the WebSocket API.
     *
     * @return {@link Completable} that completes upon successful disconnect.
     */
    Completable disconnect();

    /**
     * Checks whether connection to the exchange is alive.
     *
     * @return true if connection is open, otherwise false.
     */
    boolean isAlive();

    /**
     * Returns connection readiness status. It's either connection status for public API, or connection and successful
     * authorization for private API
     *
     * @return true if ready, otherwise false
     */
    Observable<Boolean> ready();

    /**
     * Returns service that can be used to access market data.
     */
    StreamingMarketDataService getStreamingMarketDataService();

    /**
     * Returns service that can be used to access account private data.
     */
    StreamingPrivateDataService getStreamingPrivateDataService();    

    /**
     * Set whether or not to enable compression handler.
     *
     * @param compressedMessages Defaults to false
     */
    void useCompressedMessages(boolean compressedMessages);
}
