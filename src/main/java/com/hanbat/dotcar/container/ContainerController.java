package com.hanbat.dotcar.container;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/container")
public class ContainerController {
    private final ContainerService containerService;


    @PostMapping("/create")
    public ResponseEntity<CreateContainerResponseDto> createContainer(@RequestBody CreateContainerRequestDto createContainerRequestDto){
        ContainerInfoDto containerInfoDto = containerService.createContainer(createContainerRequestDto);

        //TODO : 컨테이너 생성 실패 시에 나오는 예외처리
        CreateContainerResponseDto createContainerResponseDto = CreateContainerResponseDto.builder()
                .containerId(containerInfoDto.getContainerId())
                .port(containerInfoDto.getPort())
                .build();

        return ResponseEntity.status(200).body(createContainerResponseDto);



    }

}
