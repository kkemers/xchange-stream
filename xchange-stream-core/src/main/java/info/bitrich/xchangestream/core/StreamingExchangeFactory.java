package info.bitrich.xchangestream.core;

import org.knowm.xchange.Exchange;
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
    public StreamingExchange createExchangeWithoutSpecification(String exchangeClassName) {

        Assert.notNull(exchangeClassName, "exchangeClassName cannot be null");

        LOG.debug("Creating default exchange from class name");

        // Attempt to create an instance of the exchange provider
        try {

            // Attempt to locate the exchange provider on the classpath
            Class exchangeProviderClass = Class.forName(exchangeClassName);

            // Test that the class implements Exchange
            if (Exchange.class.isAssignableFrom(exchangeProviderClass)) {
                // Instantiate through the default constructor and use the default exchange specification
                StreamingExchange exchange = (StreamingExchange) exchangeProviderClass.newInstance();
                return exchange;
            } else {
                throw new ExchangeException("Class '" + exchangeClassName + "' does not implement Exchange");
            }
        } catch (ClassNotFoundException e) {
            throw new ExchangeException("Problem creating Exchange (class not found)", e);
        } catch (InstantiationException e) {
            throw new ExchangeException("Problem creating Exchange (instantiation)", e);
        } catch (IllegalAccessException e) {
            throw new ExchangeException("Problem creating Exchange (illegal access)", e);
        }

        // Cannot be here due to exceptions

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
    public StreamingExchange createExchange(String exchangeClassName) {

        Assert.notNull(exchangeClassName, "exchangeClassName cannot be null");

        LOG.debug("Creating default exchange from class name");

        StreamingExchange exchange = createExchangeWithoutSpecification(exchangeClassName);
        exchange.applySpecification(exchange.getDefaultExchangeSpecification());
        return exchange;

    }

    /**
     * Create an StreamingExchange object with default ExchangeSpecification with authentication info and API
     * keys provided through parameters
     *
     * <p>The factory is parameterized with the name of the exchange implementation class. This must
     * be a class extending {@link info.bitrich.xchangestream.core.StreamingExchange}.
     *
     * @param exchangeClassName the fully-qualified class name of the exchange
     * @param apiKey the public API key
     * @param secretKey the secret API key
     * @return a new exchange instance configured with the default {@link org.knowm.xchange.ExchangeSpecification}
     */
    public StreamingExchange createExchange(String exchangeClassName, String apiKey, String secretKey) {

        Assert.notNull(exchangeClassName, "exchangeClassName cannot be null");

        LOG.debug("Creating default exchange from class name");

        StreamingExchange exchange = createExchangeWithoutSpecification(exchangeClassName);

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
     * Create an StreamingExchange object default ExchangeSpecification
     *
     * @param exchangeSpecification the exchange specification
     * @return a new exchange instance configured with the provided {@link org.knowm.xchange.ExchangeSpecification}
     */
    public StreamingExchange createExchange(ExchangeSpecification exchangeSpecification) {

        Assert.notNull(exchangeSpecification, "exchangeSpecfication cannot be null");

        LOG.debug("Creating exchange from specification");

        String exchangeClassName = exchangeSpecification.getExchangeClassName();

        // Attempt to create an instance of the exchange provider
        try {

            // Attempt to locate the exchange provider on the classpath
            Class exchangeProviderClass = Class.forName(exchangeClassName);

            // Test that the class implements Exchange
            if (Exchange.class.isAssignableFrom(exchangeProviderClass)) {
                // Instantiate through the default constructor
                StreamingExchange exchange = (StreamingExchange) exchangeProviderClass.newInstance();
                exchange.applySpecification(exchangeSpecification);
                return exchange;
            } else {
                throw new ExchangeException("Class '" + exchangeClassName + "' does not implement Exchange");
            }
        } catch (ClassNotFoundException e) {
            throw new ExchangeException("Problem starting exchange provider (class not found)", e);
        } catch (InstantiationException e) {
            throw new ExchangeException("Problem starting exchange provider (instantiation)", e);
        } catch (IllegalAccessException e) {
            throw new ExchangeException("Problem starting exchange provider (illegal access)", e);
        }

        // Cannot be here due to exceptions

    }

}
