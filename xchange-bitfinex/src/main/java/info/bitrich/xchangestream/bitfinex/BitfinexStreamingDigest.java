package info.bitrich.xchangestream.bitfinex;

import org.knowm.xchange.service.BaseParamsDigest;
import si.mazi.rescu.RestInvocation;

import javax.crypto.Mac;
import java.math.BigInteger;

public class BitfinexStreamingDigest extends BaseParamsDigest {

    protected BitfinexStreamingDigest(String secretKey) throws IllegalArgumentException {
        super(secretKey, HMAC_SHA_384);
    }

    @Override
    public String digestParams(RestInvocation restInvocation) {
        return null;
    }

    public String createSignature(String payload) {
        Mac mac = getMac();
        mac.update(payload.getBytes());
        return String.format("%096x", new BigInteger(1, mac.doFinal()));
    }
}
