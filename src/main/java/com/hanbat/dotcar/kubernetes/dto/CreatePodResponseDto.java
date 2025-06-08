package com.hanbat.dotcar.kubernetes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePodResponseDto {
    private String podNamespace;
    private String podName;
    private String ingress;
    private String calledName;
//    private String servicePort;
}
