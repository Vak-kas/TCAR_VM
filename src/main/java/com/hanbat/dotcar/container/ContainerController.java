package com.hanbat.dotcar.container;

import com.hanbat.dotcar.container.dto.ContainerFailResponseDto;
import com.hanbat.dotcar.container.dto.ContainerInfoDto;
import com.hanbat.dotcar.container.dto.CreateContainerRequestDto;
import com.hanbat.dotcar.container.dto.CreateContainerResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/container")
public class ContainerController {
    private final ContainerService containerService;


    @PostMapping("/create")
    public ResponseEntity<?> createContainer(@RequestBody CreateContainerRequestDto createContainerRequestDto){
        try{
            ContainerInfoDto containerInfoDto = containerService.createContainer(createContainerRequestDto);
            CreateContainerResponseDto createContainerResponseDto = CreateContainerResponseDto.builder()
                    .containerId(containerInfoDto.getContainerId())
                    .port(containerInfoDto.getPort())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(createContainerResponseDto);
        } catch (ResponseStatusException e){
            ContainerFailResponseDto containerFailResponseDto = ContainerFailResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(containerFailResponseDto);
        }



    }

}
