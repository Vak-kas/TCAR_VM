package com.hanbat.dotcar.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    private static final String BACKEND_SERVER_IP = "http://192.168.1.19:8080";
    private static final String PROMETHEUS_SERVER_IP = "http://127.0.0.1:9090";
    private static final String METRICS_SERVER_IP = "http://127.0.0.1:8001";

    @Bean(name = "webClient")
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(BACKEND_SERVER_IP) // 기본 URL 설정
                .build();
    }


    @Bean(name = "prometheusWebClient")
    public WebClient prometheusWebClient() {
        return WebClient.builder()
                .baseUrl(PROMETHEUS_SERVER_IP)
                .build();
        }

    @Bean(name = "metricsServer")
    public WebClient metricsServer(){
        return WebClient.builder()
                .baseUrl(METRICS_SERVER_IP)
                .build();
    }



}