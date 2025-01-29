package by.zharski.idftechtask.service;

import by.zharski.idftechtask.client.ExchangeRateClient;
import by.zharski.idftechtask.entity.ExchangeRate;
import by.zharski.idftechtask.entity.ExchangeRateKey;
import by.zharski.idftechtask.mapper.MapstructMapper;
import by.zharski.idftechtask.repository.ExchangeRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Service for handling exchange rate retrieval and storage.
 * This service interacts with a repository and an external exchange rate API to fetch exchange rates.
 */
@Service
@Slf4j
public class ExchangeRateService {

    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateClient exchangeRateClient;
    private final MapstructMapper mapper;

    public ExchangeRateService(
            ExchangeRateRepository exchangeRateRepository,
            ExchangeRateClient exchangeRateClient,
            MapstructMapper mapper
    ) {
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateClient = exchangeRateClient;
        this.mapper = mapper;
    }

    /**
     * Retrieves the exchange rate for the given currency pair on a specific date.
     * If the exchange rate is not found in the database, it is fetched from an external API,
     * stored in the database, and then returned.
     *
     * @param baseCurrency the base currency code (e.g., "USD")
     * @param targetCurrency the target currency code (e.g., "EUR")
     * @param date the date for which the exchange rate is requested
     * @return the exchange rate for the given currency pair and date
     */
    public ExchangeRate getExchangeRate(String baseCurrency, String targetCurrency, LocalDate date) {
        return exchangeRateRepository.findById(new ExchangeRateKey(baseCurrency, targetCurrency, date))
                .orElseGet(() -> {
                    log.info("Exchange rate for pair {}/{} doesn't exist in db, requesting from TwelveAPI", baseCurrency, targetCurrency);

                    ExchangeRate firstExchangeRate = mapper.toExchangeRate(
                            exchangeRateClient.getExchangeRate(baseCurrency, targetCurrency, date).block()
                    );
                    ExchangeRate secondExchangeRate = mapper.toExchangeRate(
                            exchangeRateClient.getExchangeRate(targetCurrency, baseCurrency, date).block()
                    );

                    exchangeRateRepository.save(firstExchangeRate);
                    exchangeRateRepository.save(secondExchangeRate);

                    return firstExchangeRate;
                });
    }
}
