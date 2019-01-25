package info.bitrich.xchangestream.huobi.private_api;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HuobiAuthorization {

    public Map<String, String> calcSignature(String apiKey, String secretKey)
            throws NoSuchAlgorithmException, InvalidKeyException {

        StringBuilder builder = new StringBuilder();
        builder.append("GET").append('\n')
                .append("api.huobi.pro").append('\n')
                .append("/ws/v1").append('\n');

        LinkedHashMap<String, String> data = new LinkedHashMap<>();
        data.put("AccessKeyId", apiKey);
        data.put("SignatureMethod", "HmacSHA256");
        data.put("SignatureVersion", "2");

        long now = Instant.now().getEpochSecond();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String timestamp = Instant.ofEpochSecond(now).atZone(ZoneId.of("GMT")).format(dateTimeFormatter);

        data.put("Timestamp", timestamp);

        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append(key).append('=').append(urlEncode(value)).append('&');
        }
        builder.deleteCharAt(builder.length() - 1);

        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secKey = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSha256.init(secKey);

        String payload = builder.toString();
        byte[] hash = hmacSha256.doFinal(payload.getBytes(StandardCharsets.UTF_8));

        String actualSign = Base64.getEncoder().encodeToString(hash);

        LinkedHashMap<String, String> request = new LinkedHashMap<>();
        request.put("op", "auth");
        request.put("cid", UUID.randomUUID().toString());
        request.putAll(data);
        request.put("Signature", actualSign);

        return request;
    }

    private static String urlEncode(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8").replaceAll("\\+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("UTF-8 encoding not supported!");
        }
    }
}
