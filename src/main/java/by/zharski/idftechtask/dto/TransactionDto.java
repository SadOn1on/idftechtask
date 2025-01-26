package by.zharski.idftechtask.dto;

import by.zharski.idftechtask.entity.ExpenseCategory;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        LocalDateTime datetime
) {
}
