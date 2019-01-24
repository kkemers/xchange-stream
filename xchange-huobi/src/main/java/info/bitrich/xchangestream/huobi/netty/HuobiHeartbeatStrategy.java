package info.bitrich.xchangestream.huobi.netty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.huobi.dto.HuobiPingRequest;
import info.bitrich.xchangestream.service.netty.strategy.HeartbeatStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class HuobiHeartbeatStrategy implements HeartbeatStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(HuobiHeartbeatStrategy.class);

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Duration getPeriod() {
        return Duration.ofSeconds(20);
    }

    @Override
    public String getHeartbeatString() {
        try {
            return mapper.writeValueAsString(new HuobiPingRequest());
        } catch (JsonProcessingException e) {
            LOG.error("Cannot serialize ping: {}", e.getMessage(), e);
        }

        return null;
    }
}
