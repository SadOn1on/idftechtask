package by.zharski.idftechtask.client;

import by.zharski.idftechtask.dto.ExchangeRateResponseDto;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.time.LocalDate;

/**
 * Service for fetching exchange rate data from the TwelveData API.
 */
@Service
@Slf4j
public class ExchangeRateClient {

    private final WebClient webClient;

    @Value("${twelvedata.api.key}")
    private String apiKey;

    public ExchangeRateClient(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * Retrieves the exchange rate between the given base and target currencies for the specified date.
     *
     * @param baseCurrency   the base currency code (e.g., "USD")
     * @param targetCurrency the target currency code (e.g., "EUR")
     * @param date           the date for which the exchange rate is requested
     * @return a {@link Mono} containing the exchange rate response DTO
     * @throws IllegalArgumentException if any parameter is null or empty
     */
    public Mono<ExchangeRateResponseDto> getExchangeRate(
            String baseCurrency,
            String targetCurrency,
            LocalDate date
    ) throws IllegalArgumentException {
        if (baseCurrency == null || baseCurrency.isEmpty() || targetCurrency == null || targetCurrency.isEmpty()) {
            throw new IllegalArgumentException("Base currency and target currency must not be null or empty");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        log.info("Call to twelvedata: {}, date: {}", baseCurrency + "/" + targetCurrency, date);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("time_series")
                        .queryParam("symbol", baseCurrency + "/" + targetCurrency)
                        .queryParam("interval", "1day")
                        // 3-day window to get closing price of previous day in case data is not there for the day
                        .queryParam("start_date", date.minusDays(3))
                        // adding one day because end_date is not included in response
                        .queryParam("end_date", date.plusDays(1))
                        .queryParam("apikey", apiKey)
                        .build()
                )
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(
                                new ApiCallException("twelvedata api call failed with status: " + response.statusCode())
                        )
                )
                .bodyToMono(ExchangeRateResponseDto.class)
                .doOnSuccess(response -> log.info("Call to twelvedata successful: {}", response))
                .doOnError(error -> log.error("twelvedata api call failed: {}", error.getMessage()))
                .retry(2);
    }
}
