package by.zharski.idftechtask.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrencyValuesDto(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate datetime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close
) {
}
