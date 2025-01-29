package by.zharski.idftechtask.mapper;

import by.zharski.idftechtask.dto.*;
import by.zharski.idftechtask.entity.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

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
                timestamp.atZone(ZoneOffset.UTC),
                null,
                null,
                null
        );
        Transaction mappedTransaction = mapper.toTransaction(transactionDto);

        assertNull(mappedTransaction.getId());
        assertEquals(11L, mappedTransaction.getAccountFrom());
        assertEquals(12L, mappedTransaction.getAccountTo());
        assertEquals("USD", mappedTransaction.getCurrencyShortname());
        assertEquals(BigDecimal.valueOf(12.2), mappedTransaction.getSum());
        assertEquals(ExpenseCategory.PRODUCT, mappedTransaction.getExpenseCategory());
        assertEquals(timestamp.atZone(ZoneOffset.UTC), mappedTransaction.getDatetime());
        assertFalse(mappedTransaction.getLimitExceeded());
    }

    @Test
    void testToTransactionDtoFromTransaction() {
        TransactionDto transactionDto = new TransactionDto(
                11L,
                12L,
                "USD",
                BigDecimal.valueOf(12.2),
                ExpenseCategory.PRODUCT,
                timestamp.atZone(ZoneOffset.UTC),
                null,
                null,
                null
        );
        Transaction transaction = new Transaction(
                null,
                11L,
                12L,
                "USD",
                BigDecimal.valueOf(12.2),
                ExpenseCategory.PRODUCT,
                timestamp.atZone(ZoneOffset.UTC),
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
                ZonedDateTime.now(),
                BigDecimal.valueOf(12.1),
                ExpenseCategory.PRODUCT
        );
        ExpenseLimitDto expenseLimitDto = mapper.toExpenseLimitDto(expenseLimit);

        assertEquals(11L, expenseLimitDto.accountId());
        assertEquals(BigDecimal.valueOf(12.1), expenseLimit.getSum());
        assertEquals(ExpenseCategory.PRODUCT, expenseLimit.getExpenseCategory());
    }

    @Test
    void testToTransactionDtoFromTransactionWithExceededLimit() {
        ZonedDateTime dateTime = ZonedDateTime.now();
        TransactionWithExceededLimit transactionWithExceededLimit = new TransactionWithExceededLimit(
                1L,
                1L,
                2L,
                "USD",
                BigDecimal.TEN,
                ExpenseCategory.PRODUCT,
                dateTime,
                true,
                BigDecimal.valueOf(100L),
                dateTime.withDayOfMonth(1)
        );
        TransactionDto expectedTransactionDto = new TransactionDto(
                1L,
                2L,
                "USD",
                BigDecimal.TEN,
                ExpenseCategory.PRODUCT,
                dateTime,
                BigDecimal.valueOf(100L),
                dateTime.withDayOfMonth(1),
                "USD"
        );

        TransactionDto transactionDto = mapper.toTransactionDto(transactionWithExceededLimit);

        assertEquals(expectedTransactionDto, transactionDto);
    }
}