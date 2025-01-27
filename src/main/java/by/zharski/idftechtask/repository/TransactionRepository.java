package by.zharski.idftechtask.repository;

import by.zharski.idftechtask.dto.TransactionWithExceededLimit;
import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.ZonedDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDatetimeBetweenAndExpenseCategoryAndAccountFrom(
            ZonedDateTime startDate,
            ZonedDateTime endDate,
            ExpenseCategory expenseCategory,
            Long accountFrom
    );

    @Query(value = """
    SELECT t.id, t.account_from, t.account_to, t.currency_shortname, t.sum,
           t.expense_category, t.datetime AT TIME ZONE 'UTC' as datetime, 
           t.limit_exceeded, el.sum as limit_sum, 
           el.datetime AT TIME ZONE 'UTC' as limit_datetime
    FROM transactions t
    LEFT JOIN expense_limit el ON el.account_id = t.account_from
        AND el.expense_category = t.expense_category
        AND el.datetime <= t.datetime
        AND EXTRACT(YEAR FROM el.datetime) = EXTRACT(YEAR FROM t.datetime)
        AND EXTRACT(MONTH FROM el.datetime) = EXTRACT(MONTH FROM t.datetime)
        AND el.datetime = (
            SELECT MAX(sub_el.datetime)
            FROM expense_limit sub_el
            WHERE sub_el.account_id = t.account_from 
                AND sub_el.expense_category = t.expense_category 
                AND sub_el.datetime <= t.datetime
        )
    WHERE t.limit_exceeded = true;
    """, nativeQuery = true)
    List<TransactionWithExceededLimit> findTransactionsExceedingLimit();

}
