package by.zharski.idftechtask.service;

import by.zharski.idftechtask.dto.TransactionDto;
import by.zharski.idftechtask.entity.ExchangeRate;
import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.ExpenseLimit;
import by.zharski.idftechtask.entity.Transaction;
import by.zharski.idftechtask.mapper.MapstructMapper;
import by.zharski.idftechtask.repository.TransactionRepository;
import by.zharski.idftechtask.util.ExchangeRateUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ExpenseLimitService expenseLimitService;
    private final ExchangeRateService exchangeRateService;
    private final MapstructMapper mapper;

    public TransactionService(
            TransactionRepository transactionRepository,
            ExpenseLimitService expenseLimitService,
            ExchangeRateService exchangeRateService,
            MapstructMapper mapper
    ) {
        this.transactionRepository = transactionRepository;
        this.expenseLimitService = expenseLimitService;
        this.exchangeRateService = exchangeRateService;
        this.mapper = mapper;
    }

    public BigDecimal getTransactionsSumForMonthByExpenseCategory(
            ZonedDateTime dateTime,
            ExpenseCategory expenseCategory,
            Long account,
            String targetCurrency
    ) {
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
        return transactionRepository.
                findByDatetimeBetweenAndExpenseCategoryAndAccountFrom(
                        beginningOfTheMonth,
                        dateTime,
                        expenseCategory,
                        account
                )
                .stream()
                .map(transaction -> {
                    if (!transaction.getCurrencyShortname().equals(targetCurrency)) {
                        ExchangeRate exchangeRate = exchangeRateService.getExchangeRate(
                                transaction.getCurrencyShortname(),
                                targetCurrency,
                                transaction.getDatetime().toLocalDate()
                        );
                        return ExchangeRateUtil.convert(exchangeRate, transaction.getSum());
                    } else {
                        return transaction.getSum();
                    }
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public TransactionDto processTransaction(TransactionDto transactionDto) {
        ExpenseLimit expenseLimit = expenseLimitService.getExpenseLimitForDate(transactionDto.datetime(), transactionDto.expenseCategory(), transactionDto.accountFrom());
        BigDecimal expenses = getTransactionsSumForMonthByExpenseCategory(transactionDto.datetime(), transactionDto.expenseCategory(), transactionDto.accountFrom(), "USD");

        Transaction transaction = mapper.toTransaction(transactionDto);
        transaction.setLimitExceeded(expenseLimit.getSum().compareTo(expenses) < 0);
        return mapper.toTransactionDto(transactionRepository.save(transaction));
    }
}
