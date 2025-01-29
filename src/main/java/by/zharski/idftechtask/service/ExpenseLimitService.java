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

@Service
@Slf4j
public class ExpenseLimitService {

    private final ExpenseLimitRepository expenseLimitRepository;
    private final MapstructMapper mapper;

    public ExpenseLimitService(ExpenseLimitRepository expenseLimitRepository, MapstructMapper mapper) {
        this.expenseLimitRepository = expenseLimitRepository;
        this.mapper = mapper;
    }

    @Transactional
    public ExpenseLimitDto createExpenseLimit(ExpenseLimitDto expenseLimitDto) {
        ExpenseLimit expenseLimit = mapper.toExpenseLimit(expenseLimitDto);
        expenseLimit.setDatetime(ZonedDateTime.now(ZoneId.systemDefault()));
        log.info("Creating new expense limit: {}", expenseLimit);
        return mapper.toExpenseLimitDto(expenseLimitRepository.save(expenseLimit));
    }

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

    public List<ExpenseLimitDto> getAllExpenseLimits(Long accountId) {
        return expenseLimitRepository.findByAccountIdOrderByDatetimeAsc(accountId).stream()
                .map(mapper::toExpenseLimitDto)
                .toList();
    }

}
