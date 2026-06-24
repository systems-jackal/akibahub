package com.akibahub.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Configuration
public class WebClientConfig {

    @Value("${payhero.api.url}")
    private String payheroApiUrl;

    @Value("${payhero.api.username}")
    private String username;

    @Value("${payhero.api.password}")
    private String password;

    @Bean
    public WebClient payheroWebClient() {
        String credentials = Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes());
        return WebClient.builder()
                .baseUrl(payheroApiUrl)
                .defaultHeader("Authorization", "Basic " + credentials)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
