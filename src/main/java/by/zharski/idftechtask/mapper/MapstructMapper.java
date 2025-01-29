package by.zharski.idftechtask.mapper;

import by.zharski.idftechtask.config.MapstructConfiguration;
import by.zharski.idftechtask.dto.ExchangeRateResponseDto;
import by.zharski.idftechtask.dto.ExpenseLimitDto;
import by.zharski.idftechtask.dto.TransactionDto;
import by.zharski.idftechtask.dto.TransactionWithExceededLimit;
import by.zharski.idftechtask.entity.ExchangeRate;
import by.zharski.idftechtask.entity.ExpenseLimit;
import by.zharski.idftechtask.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(config = MapstructConfiguration.class)
public interface MapstructMapper {

    @Mapping(target = "key.timestamp", expression = "java(exchangeRateResponseDto.values().getFirst().datetime())")
    @Mapping(target = "key.baseCurrency", expression = "java(exchangeRateResponseDto.meta().symbol().substring(0, 3))")
    @Mapping(target = "key.targetCurrency", expression = "java(exchangeRateResponseDto.meta().symbol().substring(4, 7))")
    @Mapping(target = "rate", expression = "java(exchangeRateResponseDto.values().getFirst().close())")
    ExchangeRate toExchangeRate(ExchangeRateResponseDto exchangeRateResponseDto);

    Transaction toTransaction(TransactionDto transactionDto);

    TransactionDto toTransactionDto(Transaction transaction);

    ExpenseLimit toExpenseLimit(ExpenseLimitDto expenseLimitDto);

    ExpenseLimitDto toExpenseLimitDto(ExpenseLimit expenseLimit);

    @Mapping(target = "limitCurrencyShortname", expression = "java(\"USD\")")
    TransactionDto toTransactionDto(TransactionWithExceededLimit transactionWithExceededLimit);
}
