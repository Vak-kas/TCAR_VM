package com.hanbat.dotcar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private static final String BACKEND_SERVER_IP = "https://127.0.0.1/";

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(BACKEND_SERVER_IP) // 기본 URL 설정
                .build();
    }

}
