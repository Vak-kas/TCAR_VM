package com.hanbat.dotcar.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class KubernetesClientConfig {

    @Bean
    public ApiClient apiClient() throws IOException {
        ApiClient client = Config.defaultClient(); // 로컬 kubeconfig 사용
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);
        return client;
    }
}
