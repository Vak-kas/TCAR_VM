package com.hanbat.dotcar.kubernetes.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePodRequestDto {
    private String os;
    private String version;
    private String calledName;
    private String userEmail;
}
