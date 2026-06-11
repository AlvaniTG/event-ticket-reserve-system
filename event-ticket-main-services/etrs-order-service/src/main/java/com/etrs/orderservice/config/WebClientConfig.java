package com.etrs.orderservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${EVENT_SERVICE_URL:http://localhost:8081}")
    private String eventServiceUrl;

    @Bean
    public WebClient eventServiceWebClient() {
        return WebClient.builder()
                .baseUrl(eventServiceUrl)
                .build();
    }
}
