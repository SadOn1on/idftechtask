package by.zharski.idftechtask.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrencyValuesDto(
        LocalDate datetime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close
) {
}
