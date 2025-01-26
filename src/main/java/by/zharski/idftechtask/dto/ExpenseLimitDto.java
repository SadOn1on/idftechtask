package by.zharski.idftechtask.dto;

import by.zharski.idftechtask.entity.ExpenseCategory;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ExpenseLimitDto(
        @JsonProperty("account")
        Long accountId,
        @JsonProperty("sum")
        BigDecimal sum,
        @JsonProperty("expense_category")
        ExpenseCategory expenseCategory
) {
}
