package by.zharski.idftechtask.dto;

import java.util.List;

public record ExchangeRateResponseDto(
        ExchangeRateMetaDto meta,
        List<CurrencyValuesDto> values,
        String status
)  {
}