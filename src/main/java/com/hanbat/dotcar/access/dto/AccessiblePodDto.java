package com.hanbat.dotcar.access.dto;

import com.hanbat.dotcar.kubernetes.domain.PodStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessiblePodDto {
    private String podName;
    private String namespace;
    private PodStatus status;
    private String ingressUrl;
    private String accessType; // "owner" 또는 "guest"
}
