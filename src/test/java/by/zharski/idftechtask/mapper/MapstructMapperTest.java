package by.zharski.idftechtask.mapper;

import by.zharski.idftechtask.dto.*;
import by.zharski.idftechtask.entity.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Currency;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        classes = {
                MapstructMapperImpl.class,
        }
)
class MapstructMapperTest {

    @Autowired
    private MapstructMapper mapper;

    private static Instant timestamp;

    @BeforeAll
    static void setup() {
        timestamp = Instant.now();
    }

    @Test
    void testToExchangeRate() {
        ExchangeRateResponseDto exchangeRateResponseDto = new ExchangeRateResponseDto(
                new ExchangeRateMetaDto(
                        "USD/EUR",
                        "1day",
                        "US Dollar",
                        "Euro",
                        "Physical currency"
                ),
                List.of(new CurrencyValuesDto(
                        LocalDate.ofInstant(timestamp, ZoneOffset.UTC),
                        BigDecimal.ONE,
                        BigDecimal.TWO,
                        BigDecimal.ZERO,
                        BigDecimal.valueOf(1.5)
                )),
                "ok"
        );
        ExchangeRate exchangeRate = new ExchangeRate(
                new ExchangeRateKey(
                        "USD",
                        "EUR",
                        LocalDate.ofInstant(timestamp, ZoneOffset.UTC)
                ),
                BigDecimal.valueOf(1.5)
        );
        assertEquals(exchangeRate, mapper.toExchangeRate(exchangeRateResponseDto));
    }

    @Test
    void testToTransaction() {
        TransactionDto transactionDto = new TransactionDto(
                11L,
                12L,
                "USD",
                BigDecimal.valueOf(12.2),
                ExpenseCategory.PRODUCT,
                timestamp.atZone(ZoneOffset.UTC).toLocalDateTime()
        );
        Transaction transaction = new Transaction(
                null,
                11L,
                12L,
                Currency.getInstance("USD"),
                BigDecimal.valueOf(12.2),
                ExpenseCategory.PRODUCT,
                timestamp.atZone(ZoneOffset.UTC).toLocalDateTime(),
                false
        );
        assertEquals(transaction, mapper.toTransaction(transactionDto));
    }

    @Test
    void testToTransactionDto() {
        TransactionDto transactionDto = new TransactionDto(
                11L,
                12L,
                "USD",
                BigDecimal.valueOf(12.2),
                ExpenseCategory.PRODUCT,
                timestamp.atZone(ZoneOffset.UTC).toLocalDateTime()
        );
        Transaction transaction = new Transaction(
                null,
                11L,
                12L,
                Currency.getInstance("USD"),
                BigDecimal.valueOf(12.2),
                ExpenseCategory.PRODUCT,
                timestamp.atZone(ZoneOffset.UTC).toLocalDateTime(),
                false
        );
        assertEquals(transactionDto, mapper.toTransactionDto(transaction));
    }

    @Test
    void testToExpenseLimit() {
        ExpenseLimitDto expenseLimitDto = new ExpenseLimitDto(
                11L,
                BigDecimal.valueOf(12.1),
                ExpenseCategory.PRODUCT
        );
        ExpenseLimit expenseLimit = mapper.toExpenseLimit(expenseLimitDto);

        assertNull(expenseLimit.getId());
        assertEquals(11L, expenseLimit.getAccountId());
        assertEquals(BigDecimal.valueOf(12.1), expenseLimit.getSum());
        assertEquals(ExpenseCategory.PRODUCT, expenseLimit.getExpenseCategory());
    }

    @Test
    void testToExpenseLimitDto() {
        ExpenseLimit expenseLimit = new ExpenseLimit(
                1L,
                11L,
                LocalDateTime.now(),
                BigDecimal.valueOf(12.1),
                ExpenseCategory.PRODUCT
        );
        ExpenseLimitDto expenseLimitDto = mapper.toExpenseLimitDto(expenseLimit);

        assertEquals(11L, expenseLimitDto.accountId());
        assertEquals(BigDecimal.valueOf(12.1), expenseLimit.getSum());
        assertEquals(ExpenseCategory.PRODUCT, expenseLimit.getExpenseCategory());
    }
}