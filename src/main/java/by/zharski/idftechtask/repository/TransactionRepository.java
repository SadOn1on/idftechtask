package by.zharski.idftechtask.repository;

import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByDatetimeBetweenAndExpenseCategory(
            LocalDateTime startDate,
            LocalDateTime endDate,
            ExpenseCategory expenseCategory
    );

}
