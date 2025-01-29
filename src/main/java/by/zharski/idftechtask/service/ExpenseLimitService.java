package by.zharski.idftechtask.service;

import by.zharski.idftechtask.dto.ExpenseLimitDto;
import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.ExpenseLimit;
import by.zharski.idftechtask.mapper.MapstructMapper;
import by.zharski.idftechtask.repository.ExpenseLimitRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Service for managing expense limits.
 * This service provides functionality to create, retrieve, and list expense limits for user accounts.
 */
@Service
@Slf4j
public class ExpenseLimitService {

    private final ExpenseLimitRepository expenseLimitRepository;
    private final MapstructMapper mapper;

    public ExpenseLimitService(ExpenseLimitRepository expenseLimitRepository, MapstructMapper mapper) {
        this.expenseLimitRepository = expenseLimitRepository;
        this.mapper = mapper;
    }

    /**
     * Creates a new expense limit.
     * The expense limit is assigned the current system time before being saved.
     *
     * @param expenseLimitDto the expense limit data transfer object
     * @return the saved expense limit DTO
     */
    @Transactional
    public ExpenseLimitDto createExpenseLimit(ExpenseLimitDto expenseLimitDto) {
        ExpenseLimit expenseLimit = mapper.toExpenseLimit(expenseLimitDto);
        expenseLimit.setDatetime(ZonedDateTime.now(ZoneId.systemDefault()));
        log.info("Creating new expense limit: {}", expenseLimit);
        return mapper.toExpenseLimitDto(expenseLimitRepository.save(expenseLimit));
    }

    /**
     * Retrieves the most recent expense limit for a given category and account within a specified date range.
     * If no expense limit is found, a default limit of 1000 is returned.
     *
     * @param dateTime the reference date and time
     * @param expenseCategory the category of the expense
     * @param accountId the account ID
     * @return the applicable expense limit
     */
    public ExpenseLimit getExpenseLimitForDate(ZonedDateTime dateTime, ExpenseCategory expenseCategory, Long accountId) {
        ZonedDateTime beginningOfTheMonth = ZonedDateTime.of(
                dateTime.getYear(),
                dateTime.getMonth().getValue(),
                1,
                0,
                0,
                0,
                0,
                ZoneOffset.UTC
        );
        List<ExpenseLimit> expenseLimitList =
                expenseLimitRepository.findByDatetimeBetweenAndExpenseCategoryAndAccountIdOrderByDatetimeDesc(beginningOfTheMonth, dateTime, expenseCategory, accountId);
        return expenseLimitList.isEmpty() ? new ExpenseLimit(null, accountId, beginningOfTheMonth, BigDecimal.valueOf(1000), expenseCategory) :
                expenseLimitList.getFirst();
    }

    /**
     * Retrieves all expense limits for a given account, ordered by date in ascending order.
     *
     * @param accountId the account ID
     * @return a list of expense limit DTOs
     */
    public List<ExpenseLimitDto> getAllExpenseLimits(Long accountId) {
        return expenseLimitRepository.findByAccountIdOrderByDatetimeAsc(accountId).stream()
                .map(mapper::toExpenseLimitDto)
                .toList();
    }

}
