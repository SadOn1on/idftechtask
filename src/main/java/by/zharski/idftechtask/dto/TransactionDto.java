package by.zharski.idftechtask.dto;

import by.zharski.idftechtask.entity.ExpenseCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TransactionDto(
        @JsonProperty("account_from")
        Long accountFrom,
        @JsonProperty("account_to")
        Long accountTo,
        @JsonProperty("currency_shortname")
        String currencyShortname,
        @JsonProperty("sum")
        BigDecimal sum,
        @JsonProperty("expense_category")
        ExpenseCategory expenseCategory,
        @JsonProperty("datetime")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXXX")
        ZonedDateTime datetime,
        @JsonProperty("limit_sum")
        BigDecimal limitSum,
        @JsonProperty("limit_datetime")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXXX")
        ZonedDateTime limitDateTime,
        @JsonProperty("limit_currency_shortname")
        String limitCurrencyShortname
) {
}
