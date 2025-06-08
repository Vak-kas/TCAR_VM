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
public class PrometheusResponse {
    private Data data;

    @lombok.Data
    public static class Data {
        private List<Result> result;
    }

    @lombok.Data
    public static class Result {
        private Map<String, String> metric;
        private List<Object> value;
    }
}

