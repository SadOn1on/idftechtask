package by.zharski.idftechtask.repository;

import by.zharski.idftechtask.dto.TransactionWithExceededLimit;
import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDatetimeBetweenAndExpenseCategoryAndAccountFrom(
            ZonedDateTime startDate,
            ZonedDateTime endDate,
            ExpenseCategory expenseCategory,
            Long accountFrom
    );

    List<Transaction> findByExpenseCategory(ExpenseCategory expenseCategory);


    @Query("""
                SELECT NEW by.zharski.idftechtask.dto.TransactionWithExceededLimit(
                    t.id,
                    t.accountFrom,
                    t.accountTo,
                    t.currencyShortname,
                    t.sum,
                    t.expenseCategory,
                    t.datetime,
                    t.limitExceeded,
                    el.sum,
                    el.datetime
                )
                FROM Transaction t
                LEFT JOIN ExpenseLimit el ON el.accountId = t.accountFrom
                    AND el.expenseCategory = t.expenseCategory
                    AND el.datetime <= t.datetime
                    AND YEAR(el.datetime) = YEAR(t.datetime)
                    AND MONTH(el.datetime) = MONTH(t.datetime)
                    AND el.datetime = (
                        SELECT MAX(subEl.datetime)
                        FROM ExpenseLimit subEl
                        WHERE subEl.accountId = t.accountFrom
                            AND subEl.expenseCategory = t.expenseCategory
                            AND subEl.datetime <= t.datetime
                    )
                WHERE t.accountFrom = :accountId
                AND t.limitExceeded = true
            """)
    List<TransactionWithExceededLimit> findTransactionsExceedingLimit(@Param("accountId") Long accountId);

}
