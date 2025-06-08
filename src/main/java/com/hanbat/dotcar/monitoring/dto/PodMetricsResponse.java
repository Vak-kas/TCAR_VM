package com.hanbat.dotcar.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PodMetricsResponse {
    private String kind;
    private String apiVersion;
    private Metadata metadata;
    private String timestamp;
    private String window;
    private List<Container> containers;

    @lombok.Data
    public static class Metadata {
        private String name;
        private String namespace;
    }

    @lombok.Data
    public static class Container {
        private String name;
        private Map<String, String> usage; // cpu, memory
    }
}