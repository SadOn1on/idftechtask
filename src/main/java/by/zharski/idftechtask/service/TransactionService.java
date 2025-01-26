package by.zharski.idftechtask.service;

import by.zharski.idftechtask.client.ExchangeRateClient;
import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.Transaction;
import by.zharski.idftechtask.mapper.MapstructMapper;
import by.zharski.idftechtask.repository.ExchangeRateRepository;
import by.zharski.idftechtask.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ExpenseLimitService expenseLimitService;
    private final ExchangeRateRepository exchangeRateRepository;
    private final ExchangeRateClient exchangeRateClient;
    private final MapstructMapper mapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            ExpenseLimitService expenseLimitService,
            ExchangeRateRepository exchangeRateRepository,
            ExchangeRateClient exchangeRateClient,
            MapstructMapper mapper
    ) {
        this.transactionRepository = transactionRepository;
        this.expenseLimitService = expenseLimitService;
        this.exchangeRateRepository = exchangeRateRepository;
        this.exchangeRateClient = exchangeRateClient;
        this.mapper = mapper;
    }

    public BigDecimal getTransactionsSumForMonthByExpenseCategory(
            LocalDateTime dateTime,
            ExpenseCategory expenseCategory
    ) {
        LocalDateTime beginningOfTheMonth = LocalDateTime.of(
                dateTime.getYear(),
                dateTime.getMonth(),
                1,
                0,
                0,
                0
        );
        return transactionRepository
                .findByDatetimeBetweenAndExpenseCategory(beginningOfTheMonth, dateTime, expenseCategory)
                .stream()
                .map(Transaction::getSum)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
