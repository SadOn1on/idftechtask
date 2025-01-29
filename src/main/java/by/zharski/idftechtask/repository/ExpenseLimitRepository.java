package by.zharski.idftechtask.repository;

import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.ExpenseLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;

public interface ExpenseLimitRepository extends JpaRepository<ExpenseLimit, Long> {

    List<ExpenseLimit> findByDatetimeBetweenAndExpenseCategoryAndAccountIdOrderByDatetimeDesc(
            ZonedDateTime datetimeStart,
            ZonedDateTime datetimeEnd,
            ExpenseCategory expenseCategory,
            Long accountId
    );

    List<ExpenseLimit> findByAccountIdOrderByDatetimeAsc(Long accountId);

}
