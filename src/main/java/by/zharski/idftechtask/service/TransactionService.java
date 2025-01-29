package by.zharski.idftechtask.service;

import by.zharski.idftechtask.dto.TransactionDto;
import by.zharski.idftechtask.entity.ExchangeRate;
import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.ExpenseLimit;
import by.zharski.idftechtask.entity.Transaction;
import by.zharski.idftechtask.mapper.MapstructMapper;
import by.zharski.idftechtask.repository.TransactionRepository;
import by.zharski.idftechtask.util.ExchangeRateUtil;
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
        log.info("Processing transaction: {}", transactionDto);

        Transaction transaction = mapper.toTransaction(transactionDto);
        if (transaction.getCurrencyShortname().equals("USD")) {
            expenses = expenses.add(transaction.getSum());
        } else {
            ExchangeRate exchangeRate = exchangeRateService.getExchangeRate(
                    transaction.getCurrencyShortname(),
                    "USD",
                    transaction.getDatetime().toLocalDate()
            );
            expenses = expenses.add(ExchangeRateUtil.convert(exchangeRate, transaction.getSum()));
        }

        transaction.setLimitExceeded(expenseLimit.getSum().compareTo(expenses) < 0);
        log.info("Saving transaction: {}", transaction);
        return mapper.toTransactionDto(transactionRepository.save(transaction));
    }

    public List<TransactionDto> getLimitExceedingTransactions(Long accountId) {
        return transactionRepository.findTransactionsExceedingLimit(accountId).stream()
                .peek(transaction -> {
                    if (transaction.getLimitDateTime() == null) {
                        transaction.setLimitDateTime(
                                transaction.getDatetime().withZoneSameInstant(ZoneId.systemDefault())
                                        .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0)
                        );
                    }
                    if (transaction.getLimitSum() == null) {
                        transaction.setLimitSum(BigDecimal.valueOf(1000));
                    }
                    transaction.setDatetime(transaction.getDatetime().withZoneSameInstant(ZoneId.systemDefault()));
                    transaction.setLimitDateTime(transaction.getLimitDateTime().withZoneSameInstant(ZoneId.systemDefault()));

                })
                .map(mapper::toTransactionDto)
                .toList();
    }
}
