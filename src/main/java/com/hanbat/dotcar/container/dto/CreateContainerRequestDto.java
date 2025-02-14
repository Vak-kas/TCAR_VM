package com.hanbat.dotcar.container.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateContainerRequestDto {
    private String os;
    private String version;
    private String userEmail;
}
