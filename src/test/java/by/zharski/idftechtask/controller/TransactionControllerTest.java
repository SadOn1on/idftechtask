package by.zharski.idftechtask.controller;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.clean-disabled=false"
)
class TransactionControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    void saveTransaction_success() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        String body = readFileFromResources("saveTransaction_success_request.json");
        String expectedResponseBody = readFileFromResources("saveTransaction_success_response.json");
        HttpEntity<String> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/transaction",
                HttpMethod.POST,
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

    private String readFileFromResources(String fileName) throws IOException, URISyntaxException {
        Path path = Paths.get(
                getClass().getClassLoader().getResource(getClass().getPackageName() + "/" + fileName).toURI()
        );
        return Files.readString(path);
    }
}