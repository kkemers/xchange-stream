package info.bitrich.xchangestream.huobi.private_api;

import info.bitrich.xchangestream.huobi.private_api.dto.HuobiNotifyMessage;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;

public class HuobiAdapters {
    public static Order adaptOrder(HuobiNotifyMessage huobiNotifyMessage) {

        return new LimitOrder.Builder(Order.OrderType.BID, CurrencyPair.BTC_USD).build();
    }
}
