package by.zharski.idftechtask.service;

import by.zharski.idftechtask.client.ExchangeRateClient;
import by.zharski.idftechtask.dto.CurrencyValuesDto;
import by.zharski.idftechtask.dto.ExchangeRateResponseDto;
import by.zharski.idftechtask.entity.ExchangeRate;
import by.zharski.idftechtask.entity.ExchangeRateKey;
import by.zharski.idftechtask.mapper.MapstructMapper;
import by.zharski.idftechtask.repository.ExchangeRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExchangeRateServiceTest {

    @Mock
    private ExchangeRateRepository exchangeRateRepository;

    @Mock
    private ExchangeRateClient exchangeRateClient;

    @Mock
    private MapstructMapper mapper;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void getExchangeRate_whenExistsInRepository_shouldReturnFromRepository() {
        String baseCurrency = "USD";
        String targetCurrency = "EUR";
        LocalDate date = LocalDate.now();
        ExchangeRateKey key = new ExchangeRateKey(baseCurrency, targetCurrency, date);
        ExchangeRate expectedRate = new ExchangeRate(key, BigDecimal.valueOf(12.1));
        when(exchangeRateRepository.findById(key)).thenReturn(Optional.of(expectedRate));

        ExchangeRate result = exchangeRateService.getExchangeRate(baseCurrency, targetCurrency, date);

        verify(exchangeRateRepository).findById(key);
        verifyNoInteractions(exchangeRateClient, mapper);
        verify(exchangeRateRepository, never()).save(any());

        assertEquals(expectedRate, result);
    }

    @Test
    void getExchangeRate_whenNotInRepository_shouldFetchFromClientAndSave() {
        String baseCurrency = "USD";
        String targetCurrency = "EUR";
        LocalDate date = LocalDate.now();
        ExchangeRateKey key = new ExchangeRateKey(baseCurrency, targetCurrency, date);
        when(exchangeRateRepository.findById(key)).thenReturn(Optional.empty());


        ExchangeRateResponseDto firstExchangeRateResponseDto = new ExchangeRateResponseDto(
                null,
                List.of(new CurrencyValuesDto(date, BigDecimal.ONE, BigDecimal.TWO, BigDecimal.ZERO, BigDecimal.valueOf(1.2))),
                "ok"
        );
        ExchangeRateResponseDto secondExchangeRateResponseDto = new ExchangeRateResponseDto(
                null,
                List.of(new CurrencyValuesDto(date, BigDecimal.ONE, BigDecimal.TWO, BigDecimal.ZERO, BigDecimal.valueOf(0.83))),
                "ok"
        );
        when(exchangeRateClient.getExchangeRate(baseCurrency, targetCurrency, date))
                .thenReturn(Mono.just(firstExchangeRateResponseDto));
        when(exchangeRateClient.getExchangeRate(targetCurrency, baseCurrency, date))
                .thenReturn(Mono.just(secondExchangeRateResponseDto));

        ExchangeRate firstExchangeRate = new ExchangeRate();
        firstExchangeRate.setKey(new ExchangeRateKey(baseCurrency, targetCurrency, date));
        ExchangeRate secondExchangeRate = new ExchangeRate();
        secondExchangeRate.setKey(new ExchangeRateKey(targetCurrency, baseCurrency, date));

        when(mapper.toExchangeRate(firstExchangeRateResponseDto)).thenReturn(firstExchangeRate);
        when(mapper.toExchangeRate(secondExchangeRateResponseDto)).thenReturn(secondExchangeRate);

        ExchangeRate result = exchangeRateService.getExchangeRate(baseCurrency, targetCurrency, date);

        verify(exchangeRateRepository).findById(key);
        verify(exchangeRateClient).getExchangeRate(baseCurrency, targetCurrency, date);
        verify(exchangeRateClient).getExchangeRate(targetCurrency, baseCurrency, date);
        verify(mapper).toExchangeRate(firstExchangeRateResponseDto);
        verify(mapper).toExchangeRate(secondExchangeRateResponseDto);
        verify(exchangeRateRepository).save(firstExchangeRate);
        verify(exchangeRateRepository).save(secondExchangeRate);
        assertEquals(firstExchangeRate, result);
    }
}