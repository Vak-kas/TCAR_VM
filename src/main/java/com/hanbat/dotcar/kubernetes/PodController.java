package com.hanbat.dotcar.kubernetes;

import com.hanbat.dotcar.kubernetes.dto.CreatePodRequestDto;
import com.hanbat.dotcar.kubernetes.dto.CreatePodResponseDto;
import com.hanbat.dotcar.kubernetes.dto.PodFailResponseDto;
import com.hanbat.dotcar.kubernetes.dto.PodInfoDto;
import com.hanbat.dotcar.kubernetes.service.InstanceService;
import io.kubernetes.client.openapi.ApiException;
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
@RequestMapping("api/pod")
public class PodController {
    private final InstanceService instanceService;


    @PostMapping("/create")
    public ResponseEntity<?> createContainer(@RequestBody CreatePodRequestDto createPodRequestDto) {
        try {
            PodInfoDto podInfoDto = instanceService.createPod(createPodRequestDto);
            CreatePodResponseDto createPodResponseDto = CreatePodResponseDto.builder()
                    .podName(podInfoDto.getPodName())
                    .podNamespace(podInfoDto.getPodNamespace())
                    .build();
            return ResponseEntity.status(HttpStatus.CREATED).body(createPodResponseDto);
        } catch (ResponseStatusException e) {
            PodFailResponseDto podFailResponseDto = PodFailResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(podFailResponseDto);
        } catch (ApiException e) {
            PodFailResponseDto podFailResponseDto = PodFailResponseDto.builder()
                    .message("Pod 생성 실패: " + e.getResponseBody())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(podFailResponseDto);
        }
    }

}
