package info.bitrich.xchangestream.core;

import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.utils.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Factory to provide the following to {@link StreamingExchange}:
 * </p>
 * <ul>
 * <li>Manages the creation of specific Exchange implementations using runtime dependencies</li>
 * </ul>
 */
public enum StreamingExchangeFactory {

    INSTANCE;

    // flags
    private final Logger LOG = LoggerFactory.getLogger(ExchangeFactory.class);

    /**
     * Constructor
     */
    private StreamingExchangeFactory() {

    }

    /**
     * Create an StreamingExchange object without default ExchangeSpecification
     * <p>
     * The factory is parameterised with the name of the exchange implementation class. This must be a class extending
     * {@link info.bitrich.xchangestream.core.StreamingExchange}.
     * </p>
     *
     * @param exchangeClassName the fully-qualified class name of the exchange
     * @return a new exchange instance configured with the default {@link org.knowm.xchange.ExchangeSpecification}
     */
    public <T extends StreamingExchange> T createExchangeWithoutSpecification(String exchangeClassName) {

        Assert.notNull(exchangeClassName, "exchangeClassName cannot be null");
        Assert.isTrue(!exchangeClassName.isEmpty(), "exchangeClassName cannot be empty");

        try {
            Class<?> exchangeClass = Class.forName(exchangeClassName);

            if (!StreamingExchange.class.isAssignableFrom(exchangeClass)) {
                throw new ExchangeException(
                        String.format("Class '%s' does not implement 'StreamingExchange'", exchangeClass.getName()));
            }

            return createExchangeWithoutSpecification((Class<T>) exchangeClass);
        } catch (ClassNotFoundException e) {
            throw new ExchangeException("Problem creating Exchange (class not found)", e);
        }
    }

    /**
     * Create an StreamingExchange object without default ExchangeSpecification
     * <p>
     * The factory is parameterised with the exchange implementation class. This must be a class extending
     * {@link info.bitrich.xchangestream.core.StreamingExchange}.
     * </p>
     *
     * @param exchangeClass class of the exchange
     * @return a new exchange instance configured with the default {@link org.knowm.xchange.ExchangeSpecification}
     */
    public <T extends StreamingExchange> T createExchangeWithoutSpecification(Class<T> exchangeClass) {

        Assert.notNull(exchangeClass, "exchangeClass cannot be null");

        LOG.debug("Creating default exchange from class '{}'", exchangeClass);

        try {
            return exchangeClass.newInstance();
        } catch (InstantiationException e) {
            throw new ExchangeException("Problem creating StreamingExchange (instantiation)", e);
        } catch (IllegalAccessException e) {
            throw new ExchangeException("Problem creating StreamingExchange (illegal access)", e);
        }
    }

    /**
     * Create an StreamingExchange object with default ExchangeSpecification
     * <p>
     * The factory is parameterised with the name of the exchange implementation class. This must be a class extending
     * {@link info.bitrich.xchangestream.core.StreamingExchange}.
     * </p>
     *
     * @param exchangeClassName the fully-qualified class name of the exchange
     * @return a new exchange instance configured with the default {@link org.knowm.xchange.ExchangeSpecification}
     */
    public <T extends StreamingExchange> T createExchange(String exchangeClassName) {

        T exchange = createExchangeWithoutSpecification(exchangeClassName);
        exchange.applySpecification(exchange.getDefaultExchangeSpecification());
        return exchange;
    }

    /**
     * Create an StreamingExchange object with default ExchangeSpecification
     * <p>
     * The factory is parameterised with the exchange implementation class. This must be a class extending
     * {@link info.bitrich.xchangestream.core.StreamingExchange}.
     * </p>
     *
     * @param exchangeClass class of the exchange
     * @return a new exchange instance configured with the default {@link org.knowm.xchange.ExchangeSpecification}
     */
    public <T extends StreamingExchange> T createExchange(Class<T> exchangeClass) {
        return createExchange(exchangeClass.getName());
    }

    /**
     * Create an StreamingExchange object with default ExchangeSpecification with authentication info and API
     * keys provided through parameters
     *
     * <p>The factory is parameterized with the name of the exchange implementation class. This must
     * be a class extending {@link info.bitrich.xchangestream.core.StreamingExchange}.
     *
     * @param exchangeClassName the fully-qualified class name of the exchange
     * @param apiKey            the public API key
     * @param secretKey         the secret API key
     * @return a new exchange instance configured with the default {@link org.knowm.xchange.ExchangeSpecification}
     */
    public <T extends StreamingExchange> T createExchange(String exchangeClassName, String apiKey, String secretKey) {

        Assert.notNull(exchangeClassName, "exchangeClassName cannot be null");

        LOG.debug("Creating default exchange from class name");

        T exchange = createExchangeWithoutSpecification(exchangeClassName);

        ExchangeSpecification defaultExchangeSpecification = exchange.getDefaultExchangeSpecification();
        if (apiKey != null) {
            defaultExchangeSpecification.setApiKey(apiKey);
        }
        if (secretKey != null) {
            defaultExchangeSpecification.setSecretKey(secretKey);
        }
        exchange.applySpecification(defaultExchangeSpecification);

        return exchange;
    }

    /**
     * Create an StreamingExchange object with default ExchangeSpecification with authentication info and API
     * keys provided through parameters
     *
     * <p>The factory is parameterized with the exchange implementation class. This must
     * be a class extending {@link info.bitrich.xchangestream.core.StreamingExchange}.
     *
     * @param exchangeClass class of the exchange
     * @param apiKey        the public API key
     * @param secretKey     the secret API key
     * @return a new exchange instance configured with the default {@link org.knowm.xchange.ExchangeSpecification}
     */
    public <T extends StreamingExchange> T createExchange(Class<T> exchangeClass, String apiKey, String secretKey) {

        return createExchange(exchangeClass.getName(), apiKey, secretKey);
    }

    /**
     * Create an StreamingExchange object default ExchangeSpecification
     *
     * @param exchangeSpecification the exchange specification
     * @return a new exchange instance configured with the provided {@link org.knowm.xchange.ExchangeSpecification}
     */
    public <T extends StreamingExchange> T createExchange(ExchangeSpecification exchangeSpecification) {

        Assert.notNull(exchangeSpecification, "exchangeSpecfication cannot be null");

        T exchange = createExchangeWithoutSpecification(exchangeSpecification.getExchangeClassName());
        exchange.applySpecification(exchangeSpecification);
        return exchange;
    }

}
