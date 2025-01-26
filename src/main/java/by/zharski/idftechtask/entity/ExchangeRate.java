package by.zharski.idftechtask.entity;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Table("exchange_rates")
public class ExchangeRate {

    @PrimaryKey
    @NonNull
    private ExchangeRateKey key;

    @NonNull
    private BigDecimal rate;
}

