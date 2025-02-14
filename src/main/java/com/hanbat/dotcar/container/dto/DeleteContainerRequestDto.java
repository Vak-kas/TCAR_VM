package com.hanbat.dotcar.container.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteContainerRequestDto {
    private String containerId;
    private String userEmail;

}
