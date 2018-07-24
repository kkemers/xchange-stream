package info.bitrich.xchangestream.binance.netty;

import io.netty.channel.ChannelHandler;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandshaker;
import io.netty.handler.codec.http.websocketx.extensions.compression.DeflateFrameClientExtensionHandshaker;
import io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateClientExtensionHandshaker;

@ChannelHandler.Sharable
public final class BinanceWebSocketClientExtensionHandler extends WebSocketClientExtensionHandler {
    public static final BinanceWebSocketClientExtensionHandler INSTANCE = new BinanceWebSocketClientExtensionHandler();

    private BinanceWebSocketClientExtensionHandler() {
        super(new WebSocketClientExtensionHandshaker[]{
                new PerMessageDeflateClientExtensionHandshaker(
                        6, ZlibCodecFactory.isSupportingWindowSizeAndMemLevel(), 15, true, true),
                new DeflateFrameClientExtensionHandshaker(false),
                new DeflateFrameClientExtensionHandshaker(true)
        });
    }
}
