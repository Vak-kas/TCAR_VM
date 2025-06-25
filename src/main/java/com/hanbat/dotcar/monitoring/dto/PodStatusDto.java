package com.hanbat.dotcar.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PodStatusDto {
    private double cpu;
    private double memory;
    private double uplink;
    private double downlink;
}
