package info.bitrich.xchangestream.huobi.public_api;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.huobi.public_api.dto.HuobiMarketDepthMessage;
import info.bitrich.xchangestream.huobi.public_api.dto.HuobiTradeDetailsMessage;
import io.reactivex.Observable;
import org.knowm.xchange.currency.CurrencyPair;

@SuppressWarnings("WeakerAccess")
public class HuobiStreamingMarketDataServiceRaw {

    private final ObjectMapper mapper = new ObjectMapper();
    private final HuobiPublicStreamingService service;

    public HuobiStreamingMarketDataServiceRaw(HuobiPublicStreamingService service) {
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

   public Observable<HuobiTradeDetailsMessage.HuobiTradeDetails> getHuobiTrade(CurrencyPair currencyPair) {

        String topic = String.format("market.%s.trade.detail", HuobiAdapters.adaptCurrencyPair(currencyPair));
        return service.subscribeChannel(topic)
                .map(node -> mapper.readValue(node.toString(), HuobiTradeDetailsMessage.class))
                .flatMapIterable(message -> message.getData().getData());
    }
}
