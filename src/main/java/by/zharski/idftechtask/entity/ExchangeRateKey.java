package by.zharski.idftechtask.entity;

import lombok.*;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.CLUSTERED;
import static org.springframework.data.cassandra.core.cql.PrimaryKeyType.PARTITIONED;

@PrimaryKeyClass
@Data
@AllArgsConstructor
public class ExchangeRateKey implements Serializable {

    @PrimaryKeyColumn(name = "base_currency", type = PARTITIONED)
    @NonNull
    private String baseCurrency;

    @PrimaryKeyColumn(name = "target_currency", type = PARTITIONED)
    @NonNull
    private String targetCurrency;

    @PrimaryKeyColumn(name = "timestamp", type = CLUSTERED, ordering = org.springframework.data.cassandra.core.cql.Ordering.DESCENDING)
    @NonNull
    private LocalDate timestamp;
}
