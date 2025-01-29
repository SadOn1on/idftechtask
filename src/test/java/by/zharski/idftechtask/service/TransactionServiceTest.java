package by.zharski.idftechtask.service;

import by.zharski.idftechtask.dto.TransactionDto;
import by.zharski.idftechtask.entity.ExchangeRate;
import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.ExpenseLimit;
import by.zharski.idftechtask.entity.Transaction;
import by.zharski.idftechtask.mapper.MapstructMapper;
import by.zharski.idftechtask.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private ExpenseLimitService expenseLimitService;

    @Mock
    private ExchangeRateService exchangeRateService;

    @Mock
    private MapstructMapper mapper;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void getTransactionsSumForMonthByExpenseCategory_AllInTargetCurrency_ReturnsSum() {
        ZonedDateTime dateTime = ZonedDateTime.of(2023, 10, 15, 12, 0, 0, 0, ZoneOffset.UTC);
        ExpenseCategory category = ExpenseCategory.PRODUCT;
        Long accountId = 1L;
        String targetCurrency = "USD";

        Transaction t1 = new Transaction();
        t1.setSum(BigDecimal.valueOf(200));
        t1.setCurrencyShortname("USD");
        Transaction t2 = new Transaction();
        t2.setSum(BigDecimal.valueOf(300));
        t2.setCurrencyShortname("USD");

        when(transactionRepository.findByDatetimeBetweenAndExpenseCategoryAndAccountFrom(
                any(ZonedDateTime.class), eq(dateTime), eq(category), eq(accountId)
        )).thenReturn(List.of(t1, t2));

        BigDecimal result = transactionService.getTransactionsSumForMonthByExpenseCategory(
                dateTime, category, accountId, targetCurrency
        );

        assertEquals(BigDecimal.valueOf(500), result);
        verify(exchangeRateService, never()).getExchangeRate(any(), any(), any());
    }

    @Test
    void getTransactionsSumForMonthByExpenseCategory_MixedCurrencies_ReturnsConvertedSum() {
        ZonedDateTime dateTime = ZonedDateTime.of(2023, 10, 15, 12, 0, 0, 0, ZoneOffset.UTC);
        ExpenseCategory category = ExpenseCategory.SERVICE;
        Long accountId = 2L;
        String targetCurrency = "USD";

        Transaction t1 = new Transaction();
        t1.setSum(BigDecimal.valueOf(100));
        t1.setCurrencyShortname("EUR");
        t1.setDatetime(dateTime.withDayOfMonth(12));

        Transaction t2 = new Transaction();
        t2.setSum(BigDecimal.valueOf(200));
        t2.setCurrencyShortname("USD");
        t2.setDatetime(dateTime.withDayOfMonth(2));

        ExchangeRate eurToUsdRate = new ExchangeRate();
        eurToUsdRate.setRate(BigDecimal.valueOf(1.2));

        when(transactionRepository.findByDatetimeBetweenAndExpenseCategoryAndAccountFrom(
                any(ZonedDateTime.class), eq(dateTime), eq(category), eq(accountId)
        )).thenReturn(List.of(t1, t2));

        when(exchangeRateService.getExchangeRate("EUR", "USD", dateTime.withDayOfMonth(12).toLocalDate()))
                .thenReturn(eurToUsdRate);

        BigDecimal result = transactionService.getTransactionsSumForMonthByExpenseCategory(
                dateTime, category, accountId, targetCurrency
        );


        assertEquals(BigDecimal.valueOf(320).stripTrailingZeros(), result.stripTrailingZeros());
        verify(exchangeRateService).getExchangeRate("EUR", "USD", dateTime.withDayOfMonth(12).toLocalDate());
    }

    @Test
    void processTransaction_ExistingExpensesUnderLimit_LimitNotExceeded() {
        ZonedDateTime datetime = ZonedDateTime.now();
        ExpenseCategory category = ExpenseCategory.PRODUCT;
        Long accountId = 1L;
        BigDecimal limitSum = BigDecimal.valueOf(1005);

        TransactionDto inputDto = new TransactionDto(
                accountId, 2L, "USD", BigDecimal.valueOf(500), category, datetime, null, null, null
        );
        ExpenseLimit limit = new ExpenseLimit(null, accountId, datetime, limitSum, category);
        Transaction transactionEntity = new Transaction();
        transactionEntity.setCurrencyShortname("USD");
        transactionEntity.setSum(BigDecimal.valueOf(500));

        when(expenseLimitService.getExpenseLimitForDate(datetime, category, accountId))
                .thenReturn(limit);
        when(transactionRepository.findByDatetimeBetweenAndExpenseCategoryAndAccountFrom(
                any(ZonedDateTime.class), eq(datetime), eq(category), eq(accountId)
        )).thenReturn(List.of());
        when(mapper.toTransaction(inputDto)).thenReturn(transactionEntity);
        when(transactionRepository.save(transactionEntity)).thenReturn(transactionEntity);
        when(mapper.toTransactionDto(transactionEntity)).thenReturn(inputDto);

        TransactionDto result = transactionService.processTransaction(inputDto);

        assertFalse(transactionEntity.getLimitExceeded());
        verify(transactionRepository).save(transactionEntity);
        assertEquals(inputDto, result);
    }

    @Test
    void processTransaction_ExistingExpensesExceedLimit_LimitExceededTrue() {
        ZonedDateTime datetime = ZonedDateTime.now();
        ExpenseCategory category = ExpenseCategory.SERVICE;
        Long accountId = 3L;
        BigDecimal limitSum = BigDecimal.valueOf(1000);

        TransactionDto inputDto = new TransactionDto(
                accountId, 4L, "USD", BigDecimal.valueOf(200), category, datetime, null, null, null
        );
        ExpenseLimit limit = new ExpenseLimit(null, accountId, datetime, limitSum, category);
        Transaction transactionEntity = new Transaction();
        transactionEntity.setCurrencyShortname("USD");
        transactionEntity.setSum(BigDecimal.valueOf(200));

        Transaction existingTransaction = new Transaction();
        existingTransaction.setSum(BigDecimal.valueOf(1500));
        existingTransaction.setCurrencyShortname("USD");

        when(expenseLimitService.getExpenseLimitForDate(datetime, category, accountId))
                .thenReturn(limit);
        when(transactionRepository.findByDatetimeBetweenAndExpenseCategoryAndAccountFrom(
                any(ZonedDateTime.class), eq(datetime), eq(category), eq(accountId)
        )).thenReturn(List.of(existingTransaction));
        when(mapper.toTransaction(inputDto)).thenReturn(transactionEntity);
        when(transactionRepository.save(transactionEntity)).thenReturn(transactionEntity);
        when(mapper.toTransactionDto(transactionEntity)).thenReturn(inputDto);

        TransactionDto result = transactionService.processTransaction(inputDto);

        assertTrue(transactionEntity.getLimitExceeded());
        verify(transactionRepository).save(transactionEntity);
    }

    @Test
    void processTransaction_ExistingExpensesDontExceedLimit_CurrencyConversionRequired_LimitExceededTrue() {
        ZonedDateTime datetime = ZonedDateTime.now();
        ExpenseCategory category = ExpenseCategory.SERVICE;
        Long accountId = 3L;
        BigDecimal limitSum = BigDecimal.valueOf(1000);

        TransactionDto inputDto = new TransactionDto(
                accountId, 4L, "USD", BigDecimal.valueOf(200), category, datetime, null, null, null
        );
        ExpenseLimit limit = new ExpenseLimit(null, accountId, datetime, limitSum, category);
        Transaction transactionEntity = new Transaction();
        transactionEntity.setCurrencyShortname("EUR");
        transactionEntity.setSum(BigDecimal.valueOf(200));
        transactionEntity.setDatetime(datetime);

        Transaction existingTransaction = new Transaction();
        existingTransaction.setSum(BigDecimal.valueOf(900));
        existingTransaction.setCurrencyShortname("USD");

        ExchangeRate eurToUsdRate = new ExchangeRate();
        eurToUsdRate.setRate(BigDecimal.valueOf(1.2));

        when(expenseLimitService.getExpenseLimitForDate(datetime, category, accountId))
                .thenReturn(limit);
        when(transactionRepository.findByDatetimeBetweenAndExpenseCategoryAndAccountFrom(
                any(ZonedDateTime.class), eq(datetime), eq(category), eq(accountId)
        )).thenReturn(List.of(existingTransaction));
        when(mapper.toTransaction(inputDto)).thenReturn(transactionEntity);
        when(exchangeRateService.getExchangeRate("EUR", "USD", datetime.toLocalDate()))
                .thenReturn(eurToUsdRate);
        when(transactionRepository.save(transactionEntity)).thenReturn(transactionEntity);
        when(mapper.toTransactionDto(transactionEntity)).thenReturn(inputDto);

        TransactionDto result = transactionService.processTransaction(inputDto);

        assertTrue(transactionEntity.getLimitExceeded());
        verify(transactionRepository).save(transactionEntity);
    }
}