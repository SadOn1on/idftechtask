package by.zharski.idftechtask.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

    @Value("${twelvedata.api.url}")
    private String BASE_URI;

    @Bean
    WebClient twelveDataWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder.baseUrl(BASE_URI).build();
    }

}
