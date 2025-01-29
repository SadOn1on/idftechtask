package by.zharski.idftechtask.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@EnableWireMock(
        @ConfigureWireMock(
                port = 8080
        )
)
public class ExchangeRateClientTest {

    private ExchangeRateClient exchangeRateClient;

    @BeforeEach
    void setUp() {
        WebClient webClient = WebClient.builder()
                .baseUrl("http://localhost:8080")
                .build();
        exchangeRateClient = new ExchangeRateClient(webClient);
        ReflectionTestUtils.setField(exchangeRateClient, "apiKey", "test-api-key");
    }

    @Test
    void getExchangeRate_Success() throws Exception {
        String responseBody = readFileFromResources("exchange_rate_success.json");

        stubFor(get(urlPathEqualTo("/time_series"))
                .withQueryParam("symbol", equalTo("USD/EUR"))
                .withQueryParam("interval", equalTo("1day"))
                .withQueryParam("start_date", equalTo("2022-12-29"))
                .withQueryParam("end_date", equalTo("2023-01-02"))
                .withQueryParam("apikey", equalTo("test-api-key"))
                .willReturn(okJson(responseBody)));

        var result = exchangeRateClient.getExchangeRate(
                "USD",
                "EUR",
                LocalDate.of(2023, 1, 1)
        ).block();

        assertNotNull(result);
        assertEquals("ok", result.status());

        var meta = result.meta();
        assertEquals("USD/EUR", meta.symbol());
        assertEquals("1day", meta.interval());
        assertEquals("USD", meta.baseCurrency());
        assertEquals("EUR", meta.targetCurrency());
        assertEquals("Physical Currency", meta.type());

        var values = result.values();
        assertEquals(1, values.size());
        var firstValue = values.getFirst();
        assertEquals(LocalDate.of(2023, 1, 1), firstValue.datetime());
        assertEquals(new BigDecimal("0.9500"), firstValue.open());
        assertEquals(new BigDecimal("0.9600"), firstValue.high());
        assertEquals(new BigDecimal("0.9400"), firstValue.low());
        assertEquals(new BigDecimal("0.9550"), firstValue.close());
    }

    @Test
    void getExchangeRate_ClientError() {
        stubFor(get(urlPathEqualTo("/time_series"))
                .willReturn(badRequest()));

        var exception = assertThrows(RuntimeException.class, () ->
                exchangeRateClient.getExchangeRate("USD", "EUR", LocalDate.now()).block()
        );

        assertTrue(exception.getMessage().contains("400"));
        verify(3, getRequestedFor(urlPathEqualTo("/time_series")));
    }

    @Test
    void getExchangeRate_ServerError_Retries() {
        stubFor(get(urlPathEqualTo("/time_series"))
                .willReturn(serverError()));

        var exception = assertThrows(RuntimeException.class, () ->
                exchangeRateClient.getExchangeRate("USD", "EUR", LocalDate.now()).block()
        );

        assertTrue(exception.getMessage().contains("500"));
        verify(3, getRequestedFor(urlPathEqualTo("/time_series")));
    }

    @Test
    void getExchangeRate_InvalidInput() {
        assertThrows(IllegalArgumentException.class, () ->
                exchangeRateClient.getExchangeRate("", "EUR", LocalDate.now())
        );

        assertThrows(IllegalArgumentException.class, () ->
                exchangeRateClient.getExchangeRate("USD", null, LocalDate.now())
        );

        assertThrows(IllegalArgumentException.class, () ->
                exchangeRateClient.getExchangeRate("USD", "EUR", null)
        );
    }

    private String readFileFromResources(String fileName) throws IOException, URISyntaxException {
        Path path = Paths.get(
                getClass().getClassLoader().getResource(getClass().getPackageName() + "/" + fileName).toURI()
        );
        return Files.readString(path);
    }
}