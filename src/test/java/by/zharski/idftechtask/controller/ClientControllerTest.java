package by.zharski.idftechtask.controller;

import by.zharski.idftechtask.entity.ExpenseCategory;
import by.zharski.idftechtask.entity.ExpenseLimit;
import by.zharski.idftechtask.entity.Transaction;
import by.zharski.idftechtask.repository.ExpenseLimitRepository;
import by.zharski.idftechtask.repository.TransactionRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.clean-disabled=false"
)
class ClientControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    ExpenseLimitRepository expenseLimitRepository;

    @Autowired
    TransactionRepository transactionRepository;

    @Test
    void saveLimit_success() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        String body = readFileFromResources("saveLimit_success_request.json");
        String expectedResponseBody = readFileFromResources("saveLimit_success_response.json");
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/client/limit",
                HttpMethod.POST,
                httpEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponseBody, response.getBody());
    }

    @Test
    void getAllLimits_success() throws Exception {
        expenseLimitRepository.save(new ExpenseLimit(null, 1L, ZonedDateTime.now(), BigDecimal.TEN, ExpenseCategory.PRODUCT));
        expenseLimitRepository.save(new ExpenseLimit(null, 2L, ZonedDateTime.now(), BigDecimal.TEN, ExpenseCategory.PRODUCT));
        expenseLimitRepository.save(new ExpenseLimit(null, 1L, ZonedDateTime.now().withDayOfMonth(1), BigDecimal.TWO, ExpenseCategory.SERVICE));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        String expectedResponseBody = readFileFromResources("getAllLimits_success_response.json");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/client/limits?accountId=1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponseBody, response.getBody());
    }

    @Test
    void getTransactionsExceededLimits() throws Exception {
        setUpLimits();
        setUpTransactions();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        String expectedResponseBody = readFileFromResources("getTransactionsExceededLimits_response.json");
        HttpEntity<String> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/client/exceededLimitTransactions?accountId=1",
                HttpMethod.GET,
                httpEntity,
                String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponseBody, response.getBody());
    }

    @AfterEach
    void clearDatabase(@Autowired Flyway flyway) {
        flyway.clean();
        flyway.migrate();
    }

    private void setUpTransactions() {
        transactionRepository.save(
                new Transaction(
                        null,
                        1L,
                        2L,
                        "USD",
                        BigDecimal.valueOf(999),
                        ExpenseCategory.PRODUCT,
                        ZonedDateTime.of(2025, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC),
                        false
                )
        );
        transactionRepository.save(
                new Transaction(
                        null,
                        1L,
                        2L,
                        "USD",
                        BigDecimal.valueOf(5),
                        ExpenseCategory.PRODUCT,
                        ZonedDateTime.of(2025, 1, 5, 1, 1, 1, 1, ZoneOffset.UTC),
                        true
                )
        );
        transactionRepository.save(
                new Transaction(
                        null,
                        1L,
                        2L,
                        "USD",
                        BigDecimal.valueOf(999),
                        ExpenseCategory.PRODUCT,
                        ZonedDateTime.of(2025, 1, 17, 1, 1, 1, 1, ZoneOffset.UTC),
                        false
                )
        );transactionRepository.save(
                new Transaction(
                        null,
                        1L,
                        2L,
                        "USD",
                        BigDecimal.valueOf(10000),
                        ExpenseCategory.PRODUCT,
                        ZonedDateTime.of(2025, 1, 18, 1, 1, 1, 1, ZoneOffset.UTC),
                        true
                )
        );
    }

    private void setUpLimits() {
        expenseLimitRepository.save(
                new ExpenseLimit(
                        null,
                        1L,
                        ZonedDateTime.of(2025, 1, 12, 1, 1, 1, 1, ZoneOffset.UTC),
                        BigDecimal.valueOf(3000),
                        ExpenseCategory.SERVICE
                )
        );
        expenseLimitRepository.save(
                new ExpenseLimit(
                        null,
                        1L,
                        ZonedDateTime.of(2025, 1, 15, 1, 1, 1, 1, ZoneOffset.UTC),
                        BigDecimal.valueOf(3000),
                        ExpenseCategory.PRODUCT
                )
        );
    }

    private String readFileFromResources(String fileName) throws IOException, URISyntaxException {
        Path path = Paths.get(
                getClass().getClassLoader().getResource(getClass().getPackageName() + "/" + fileName).toURI()
        );
        return Files.readString(path);
    }
}