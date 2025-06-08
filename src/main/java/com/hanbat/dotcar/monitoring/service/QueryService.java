package com.hanbat.dotcar.monitoring.service;

import org.springframework.stereotype.Service;

@Service
public class QueryService {

    public String getRealTimeCpuUsageQuery(String podName, String podNamespace){
        String query = String.format(
                "container_cpu_usage_seconds_total{pod=\"%s\",namespace=\"%s\"}",
                podName, podNamespace
        );
        System.out.println(query);
        return query;
    }
}
