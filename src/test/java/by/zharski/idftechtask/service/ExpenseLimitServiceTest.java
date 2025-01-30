package by.zharski.idftechtask.service;

import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.ExpenseLimit;
import by.zharski.idftechtask.mapper.MapstructMapper;
import by.zharski.idftechtask.repository.ExpenseLimitRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseLimitServiceTest {

    @Mock
    private ExpenseLimitRepository expenseLimitRepository;

    @Mock
    private MapstructMapper mapper;

    @InjectMocks
    private ExpenseLimitService expenseLimitService;

    @Test
    void getExpenseLimitForDate_ExistingLimit_ReturnsFirst() {
        ZonedDateTime dateTime = ZonedDateTime.of(2023, 10, 5, 12, 0, 0, 0, ZoneOffset.UTC);
        ExpenseCategory category = ExpenseCategory.PRODUCT;
        Long accountId = 1L;

        ZonedDateTime expectedStart = ZonedDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ExpenseLimit limit1 = new ExpenseLimit();
        limit1.setDatetime(dateTime.minusDays(1));
        List<ExpenseLimit> limits = List.of(limit1);

        when(expenseLimitRepository.findByDatetimeBetweenAndExpenseCategoryAndAccountIdOrderByDatetimeDesc(
                expectedStart, dateTime, category, accountId
        )).thenReturn(limits);

        ExpenseLimit result = expenseLimitService.getExpenseLimitForDate(dateTime, category, accountId);

        assertEquals(limit1, result);
        verify(expenseLimitRepository).findByDatetimeBetweenAndExpenseCategoryAndAccountIdOrderByDatetimeDesc(
                expectedStart, dateTime, category, accountId
        );
    }

    @Test
    void getExpenseLimitForDate_NoLimits_ReturnsDefault() {
        ZonedDateTime dateTime = ZonedDateTime.of(2023, 10, 5, 12, 0, 0, 0, ZoneOffset.UTC);
        ExpenseCategory category = ExpenseCategory.SERVICE;
        Long accountId = 2L;

        ZonedDateTime expectedStart = ZonedDateTime.of(2023, 10, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        when(expenseLimitRepository.findByDatetimeBetweenAndExpenseCategoryAndAccountIdOrderByDatetimeDesc(
                expectedStart, dateTime, category, accountId
        )).thenReturn(List.of());

        ExpenseLimit result = expenseLimitService.getExpenseLimitForDate(dateTime, category, accountId);

        assertEquals(BigDecimal.valueOf(1000), result.getSum());
        assertEquals(expectedStart, result.getDatetime());
        assertEquals(accountId, result.getAccountId());
        assertEquals(category, result.getExpenseCategory());
    }

}