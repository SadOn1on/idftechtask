package by.zharski.idftechtask.service;

import by.zharski.idftechtask.dto.ExpenseLimitDto;
import by.zharski.idftechtask.entity.ExpenseLimit;
import by.zharski.idftechtask.mapper.MapstructMapper;
import by.zharski.idftechtask.repository.ExpenseLimitRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ExpenseLimitService {

    private final ExpenseLimitRepository expenseLimitRepository;
    private final MapstructMapper mapper;

    public ExpenseLimitService(ExpenseLimitRepository expenseLimitRepository, MapstructMapper mapper) {
        this.expenseLimitRepository = expenseLimitRepository;
        this.mapper = mapper;
    }

    public ExpenseLimitDto createExpenseLimit(ExpenseLimitDto expenseLimitDto) {
        ExpenseLimit expenseLimit = mapper.toExpenseLimit(expenseLimitDto);
        return mapper.toExpenseLimitDto(expenseLimitRepository.save(expenseLimit));
    }

    public ExpenseLimit getExpenseLimitForDate(LocalDateTime dateTime) {
        LocalDateTime beginningOfTheMonth = LocalDateTime.of(
                dateTime.getYear(),
                dateTime.getMonth(),
                1,
                0,
                0,
                0
        );
        return expenseLimitRepository.findByDatetimeBetweenOrderByDatetimeDesc(beginningOfTheMonth, dateTime).getFirst();
    }
}
