package com.hanbat.dotcar.container;

import com.hanbat.dotcar.access.AccessService;
import com.hanbat.dotcar.container.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/container")
public class ContainerController {
    private final ContainerService containerService;
    private final AccessService accessService;

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

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteContainer(@RequestBody DeleteContainerRequestDto deleteContainerRequestDto) {
        try {
            String resultMessage = containerService.deleteContainer(deleteContainerRequestDto);
            DeleteContainerResponseDto deleteContainerResponseDto = DeleteContainerResponseDto.builder()
                    .message(resultMessage)
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(deleteContainerResponseDto);
        } catch (ResponseStatusException e) {
            ContainerFailResponseDto errorResponse = ContainerFailResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(errorResponse);
        }
    }


}
