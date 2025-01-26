package by.zharski.idftechtask.repository;

import by.zharski.idftechtask.entity.ExpenseLimit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseLimitRepository extends JpaRepository<ExpenseLimit, Long> {

    List<ExpenseLimit> findByDatetimeBetweenOrderByDatetimeDesc(LocalDateTime startDate, LocalDateTime endDate);

}
