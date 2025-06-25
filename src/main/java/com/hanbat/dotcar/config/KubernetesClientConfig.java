package com.hanbat.dotcar.config;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class KubernetesClientConfig {

    @Bean
    public ApiClient apiClient() throws IOException, ApiException {
        ApiClient client = Config.defaultClient();
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(client);

        // ✅ 요청 로그 활성화 (디버깅용)
        client.setDebugging(true);
        Logger.getLogger("io.kubernetes.client").setLevel(Level.ALL);

        return client;
    }


    @Bean
    public CoreV1Api coreV1Api(ApiClient apiClient) {
        return new CoreV1Api(apiClient);
    }

    // ingress 리소스 관리
    @Bean
    public NetworkingV1Api networkingV1Api(ApiClient apiClient){
        return new NetworkingV1Api(apiClient);
    }
}
