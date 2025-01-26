package by.zharski.idftechtask.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ExchangeRateMetaDto(
        String symbol,
        String interval,
        @JsonProperty("currency_base")
        String baseCurrency,
        @JsonProperty("currency_quote")
        String targetCurrency,
        String type
) {
}
