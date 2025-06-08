package com.hanbat.dotcar.monitoring.service;

import com.hanbat.dotcar.monitoring.dto.PodMetricsResponse;
import com.hanbat.dotcar.monitoring.dto.PodStatusDto;
import com.hanbat.dotcar.monitoring.dto.PrometheusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class PodMonitoringService {
//    @Qualifier("prometheusWebClient")
//    private final WebClient prometheusWebClient;
//    private final QueryService queryService;
    private final WebClient metricsServer;


    public PodStatusDto getPodStatus(String podName, String podNamespace) {
        PodMetricsResponse metrics = getPodMetrics(podNamespace, podName);
        double cpuMillicores = 0;
        double memoryMi = 0;

        if (metrics != null && metrics.getContainers() != null && !metrics.getContainers().isEmpty()) {
            PodMetricsResponse.Container container = metrics.getContainers().get(0);

            // CPU: n(나노코어) → mCPU(밀리코어)
            String cpuStr = container.getUsage().get("cpu").replace("n", "");
            try {
                double cpuN = Double.parseDouble(cpuStr);
                cpuMillicores = cpuN / 1_000_000.0; // mCPU로 변환
            } catch (NumberFormatException e) {
                cpuMillicores = 0;
            }

            // 메모리: Ki → Mi
            String memStr = container.getUsage().get("memory").replace("Ki", "");
            try {
                double memKi = Double.parseDouble(memStr);
                memoryMi = memKi / 1024.0; // Mi로 변환
            } catch (NumberFormatException e) {
                memoryMi = 0;
            }
        }

        // 단위 그대로 전달 (소수점 2자리로)
        return PodStatusDto.builder()
                .cpu(Math.round(cpuMillicores * 100.0) / 100.0)      // mCPU
                .memory(Math.round(memoryMi * 100.0) / 100.0)         // Mi
                .uplink(0)
                .downlink(0)
                .build();
    }


    public PodMetricsResponse getPodMetrics(String namespace, String podName) {
        PodMetricsResponse podMetricsResponse = metricsServer.get()
                .uri("/apis/metrics.k8s.io/v1beta1/namespaces/{namespace}/pods/{podName}", namespace, podName)
                .retrieve()
                .bodyToMono(PodMetricsResponse.class)
                .block();
        System.out.println(podMetricsResponse);
        return podMetricsResponse;
    }

    // 실시간 CPU(%) 구하기
    public double getRealTimePodCpuUsage(String podName, String podNamespace) {
        PodMetricsResponse metrics = getPodMetrics(podNamespace, podName);

        if (metrics != null && metrics.getContainers() != null && !metrics.getContainers().isEmpty()) {
            // 보통 하나의 컨테이너만 있다고 가정
            String cpuStr = metrics.getContainers().get(0).getUsage().get("cpu"); // ex) "5m"
            if (cpuStr != null) {
                // m(밀리코어)로 끝나는 값 처리
                if (cpuStr.endsWith("n")) {
                    // 나노코어 -> 코어로 변환 (n: nano, 10^-9)
                    double nanoCores = Double.parseDouble(cpuStr.replace("n", ""));
                    return nanoCores / 1000000000.0 * 100.0; // 코어 대비 %
                } else if (cpuStr.endsWith("u")) {
                    // 마이크로코어 -> 코어로 변환
                    double microCores = Double.parseDouble(cpuStr.replace("u", ""));
                    return microCores / 1000000.0 * 100.0;
                } else if (cpuStr.endsWith("m")) {
                    // 밀리코어 -> 코어로 변환
                    double millicores = Double.parseDouble(cpuStr.replace("m", ""));
                    return millicores / 1000.0 * 100.0; // 1000m = 1 core, 1 core = 100%
                } else {
                    // 그냥 코어수 ex) "0.01"
                    double cores = Double.parseDouble(cpuStr);
                    return cores * 100.0; // 1 core = 100%
                }
            }
        }
        return 0.0;
    }

    // 실시간 메모리(%) 구하기 (예: totalMemory는 노드 메모리에서 구해야 정확)
    public double getPodMemoryUsage(String podName, String podNamespace) {
        PodMetricsResponse metrics = getPodMetrics(podNamespace, podName);

        if (metrics != null && metrics.getContainers() != null && !metrics.getContainers().isEmpty()) {
            String memStr = metrics.getContainers().get(0).getUsage().get("memory"); // ex) "10Mi"
            if (memStr != null) {
                // 메모리 단위 처리
                if (memStr.endsWith("Ki")) {
                    double memKi = Double.parseDouble(memStr.replace("Ki", ""));
                    return memKi / 1024.0; // MiB로 변환
                } else if (memStr.endsWith("Mi")) {
                    return Double.parseDouble(memStr.replace("Mi", "")); // 이미 MiB 단위
                } else if (memStr.endsWith("Gi")) {
                    return Double.parseDouble(memStr.replace("Gi", "")) * 1024.0; // GiB -> MiB
                } else {
                    // 단위가 없을 경우
                    return Double.parseDouble(memStr) / (1024.0 * 1024.0); // Byte -> MiB
                }
            }
        }
        return 0.0;
    }


    //CPU 사용량 조회
//    public double getRealTimePodCpuUsage(String podName, String podNamespace) {
//        String query = queryService.getRealTimeCpuUsageQuery(podName, podNamespace);
//        System.out.println("쿼리문: " + query);
//
//        PrometheusResponse response = prometheusWebClient
//                .get()
//                .uri(uriBuilder -> uriBuilder
//                        .path("/api/v1/query")
//                        .queryParam("query", query)
//                        .build(false))
//                .retrieve()
//                .bodyToMono(PrometheusResponse.class)
//                .block();
//
//        System.out.println(response);
//
//        if (response != null
//                && response.getData() != null
//                && !response.getData().getResult().isEmpty()
//                && response.getData().getResult().get(0).getValue().size() >= 2) {
//
//            // [timestamp, value]
//            String valueStr = String.valueOf(response.getData().getResult().get(0).getValue().get(1));
//            try {
//                double cpu = Double.parseDouble(valueStr);
//                System.out.println("cpu : " +cpu);
//                return Math.round(cpu * 10000.0) / 100.0; // % 변환
//            } catch (Exception e) {
//                e.printStackTrace(); // 또는 log.error("에러 발생!", e);
//                return 99.9;
//            }
//        }
//        return 0.0;
//    }


}
