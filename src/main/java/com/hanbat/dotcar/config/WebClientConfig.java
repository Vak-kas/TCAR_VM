package com.hanbat.dotcar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private static final String BACKEND_SERVER_IP = "http://192.168.1.10:8080";

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(BACKEND_SERVER_IP) // 기본 URL 설정
                .build();
    }

}
