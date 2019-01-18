package info.bitrich.xchangestream.bitfinex.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

@JsonDeserialize(using = BitfinexRawOrderbookUpdate.BitfinexRawOrderBookUpdateDeserializer.class)
public class BitfinexRawOrderbookUpdate {
    private final String channelId;
    private final BitfinexRawOrderbookLevel[] levels;

    public BitfinexRawOrderbookUpdate(String channelId, BitfinexRawOrderbookLevel[] levels) {
        this.channelId = channelId;
        this.levels = levels;
    }

    public String getChannelId() {
        return channelId;
    }

    public BitfinexRawOrderbookLevel[] getLevels() {
        return levels;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BitfinexRawOrderbookUpdate{");
        sb.append("channelId='").append(channelId).append('\'');
        sb.append(", levels=").append(Arrays.toString(levels));
        sb.append('}');
        return sb.toString();
    }

    protected static class BitfinexRawOrderBookUpdateDeserializer extends JsonDeserializer<BitfinexRawOrderbookUpdate> {
        @Override
        public BitfinexRawOrderbookUpdate deserialize(JsonParser jsonParser, DeserializationContext unused)
                throws IOException, JsonProcessingException {
            final ObjectCodec oc = jsonParser.getCodec();
            final JsonNode node = oc.readTree(jsonParser);

            final String channelID = getChannelId(node);

            final BitfinexRawOrderbookLevel[] levels = getOrderBookLevels(node);

            return new BitfinexRawOrderbookUpdate(channelID, levels);
        }

        private BitfinexRawOrderbookLevel[] getOrderBookLevels(JsonNode node) {
            final int LEVELS = 1;
            final int FIRST_NODE = 0;

            JsonNode levelsNode = node.get(LEVELS);

            if (!levelsNode.get(FIRST_NODE).isArray()) {
                BitfinexRawOrderbookLevel level = createLevelFromLevelNode(levelsNode);
                return new BitfinexRawOrderbookLevel[]{level};
            }

            final int levelsCount = levelsNode.size();

            final BitfinexRawOrderbookLevel[] levels = new BitfinexRawOrderbookLevel[levelsCount];

            for (int i = 0; i < levelsCount; i++) {
                levels[i] = createLevelFromLevelNode(levelsNode.get(i));
            }

            return levels;
        }

        private BitfinexRawOrderbookLevel createLevelFromLevelNode(JsonNode levelsNode) {
            final int ORDER_ID = 0;
            final int PRICE = 1;
            final int AMOUNT = 2;

            String orderId = levelsNode.get(ORDER_ID).asText();
            BigDecimal price = levelsNode.get(PRICE).decimalValue();
            BigDecimal amount = levelsNode.get(AMOUNT).decimalValue();

            return new BitfinexRawOrderbookLevel(orderId, price, amount);
        }

        private String getChannelId(JsonNode jsonNode) {
            final int CHANNEL_ID = 0;

            return jsonNode.get(CHANNEL_ID).asText();
        }
    }
}
