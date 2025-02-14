package com.hanbat.dotcar.container.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateContainerResponseDto {
    private String containerId; //컨테이너 아이디
    private String port; //포트 번호

}
