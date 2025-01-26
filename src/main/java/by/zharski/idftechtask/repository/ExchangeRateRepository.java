package by.zharski.idftechtask.repository;

import by.zharski.idftechtask.entity.ExchangeRate;
import by.zharski.idftechtask.entity.ExchangeRateKey;
import org.springframework.data.cassandra.repository.CassandraRepository;

public interface ExchangeRateRepository extends CassandraRepository<ExchangeRate, ExchangeRateKey> {
}
