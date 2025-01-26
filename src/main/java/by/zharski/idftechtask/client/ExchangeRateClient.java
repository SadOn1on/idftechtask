package by.zharski.idftechtask.client;

import by.zharski.idftechtask.dto.ExchangeRateResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ExchangeRateClient {

    private final WebClient webClient;

    @Value("${twelvedata.api.key}")
    private String apiKey;

    public ExchangeRateClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<ExchangeRateResponseDto> getExchangeRate(
            String baseCurrency,
            String targetCurrency,
            LocalDate dateTime
    ) throws IllegalArgumentException {
        if (baseCurrency == null || baseCurrency.isEmpty() || targetCurrency == null || targetCurrency.isEmpty()) {
            throw new IllegalArgumentException("Base currency and target currency must not be null or empty");
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("Date must not be null");
        }
        log.info("Call to twelvedata: {}, date: {}", baseCurrency + "/" + targetCurrency, dateTime);
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("time_series")
                        .queryParam("symbol", baseCurrency + "/" + targetCurrency)
                        .queryParam("interval", "1day")
                        .queryParam("date", dateTime)
                        .queryParam("apikey", apiKey)
                        .build()
                )
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        response -> Mono.error(
                                new RuntimeException("twelvedata api call failed with status: " + response.statusCode())
                        )
                )
                .bodyToMono(ExchangeRateResponseDto.class)
                .doOnSuccess(response -> log.info("Call to twelvedata successful: {}", response))
                .doOnError(error -> log.error("twelvedata api call failed: {}", error.getMessage()))
                .retry(2);

    }
}
