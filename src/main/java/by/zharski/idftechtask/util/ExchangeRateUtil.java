package by.zharski.idftechtask.util;

import by.zharski.idftechtask.entity.ExchangeRate;

import java.math.BigDecimal;

public class ExchangeRateUtil {

    public static BigDecimal convert(ExchangeRate exchangeRate, BigDecimal value) {
        return value.multiply(exchangeRate.getRate());
    }

}
