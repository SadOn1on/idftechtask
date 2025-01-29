package by.zharski.idftechtask.dto;

import by.zharski.idftechtask.entity.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
@Setter
public class TransactionWithExceededLimit {
    private Long id;
    private Long accountFrom;
    private Long accountTo;
    private String currencyShortname;
    private BigDecimal sum;
    private ExpenseCategory expenseCategory;
    private ZonedDateTime datetime;
    private Boolean limitExceeded;
    private BigDecimal limitSum;
    private ZonedDateTime limitDateTime;
}
