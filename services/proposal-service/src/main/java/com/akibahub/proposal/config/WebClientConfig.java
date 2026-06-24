package com.akibahub.proposal.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${services.group.url}")
    private String groupServiceUrl;

    @Value("${services.payment.url}")
    private String paymentServiceUrl;

    @Bean("groupWebClient")
    public WebClient groupWebClient() {
        return WebClient.builder().baseUrl(groupServiceUrl).build();
    }

    @Bean("paymentWebClient")
    public WebClient paymentWebClient() {
        return WebClient.builder().baseUrl(paymentServiceUrl).build();
    }
}
