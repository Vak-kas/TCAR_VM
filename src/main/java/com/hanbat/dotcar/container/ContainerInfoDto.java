package com.hanbat.dotcar.container;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContainerInfoDto {
    private String containerId; //컨테이너 아이디
    private String port; //포트 번호
}
