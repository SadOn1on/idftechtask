package by.zharski.idftechtask.dto;

import by.zharski.idftechtask.entity.ExpenseCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.bind.DefaultValue;

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
        ZonedDateTime datetime,
        @JsonProperty("limit_sum")
        BigDecimal limitSum,
        @JsonProperty("limit_datetime")
        ZonedDateTime limitDateTime,
        @JsonProperty("limit_currency_shortname")
        @DefaultValue("USD")
        String limitCurrencyShortname
) {
}
