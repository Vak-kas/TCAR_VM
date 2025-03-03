package com.hanbat.dotcar.access.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessibleContainerDto {
    private String containerId;
    private String port;
    private String containerName;
    private String accessType; // 소유자인가, 게스트인가
}
