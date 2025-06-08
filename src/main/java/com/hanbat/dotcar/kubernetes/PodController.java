package com.hanbat.dotcar.kubernetes;

import com.hanbat.dotcar.kubernetes.dto.*;
import com.hanbat.dotcar.kubernetes.service.InstanceService;
import io.kubernetes.client.openapi.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/pod")
public class PodController {
    private final InstanceService instanceService;


    @PostMapping("/create")
    public ResponseEntity<?> createInstance(@RequestBody CreatePodRequestDto createPodRequestDto) {
        try {
            PodInfoDto podInfoDto = instanceService.createInstance(createPodRequestDto);
            CreatePodResponseDto createPodResponseDto = CreatePodResponseDto.builder()
                    .podName(podInfoDto.getPodName())
                    .podNamespace(podInfoDto.getPodNamespace())
                    .ingress(podInfoDto.getIngress())
                    .calledName(podInfoDto.getCalledName())
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


    //TODO : 임시코드
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteInstance(@RequestBody DeletePodRequestDto deletePodRequestDto){
        try{
            instanceService.deleteInstance(deletePodRequestDto);
            DeletePodResponseDto deletePodResponseDto = new DeletePodResponseDto().builder()
                    .message("삭제 성공!")
                    .build();
            return ResponseEntity.status(HttpStatus.OK).body(deletePodResponseDto);

        } catch (ApiException e){
            PodFailResponseDto podFailResponseDto = PodFailResponseDto.builder()
                    .message("Pod 삭제 실패 : " +e.getResponseBody())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(podFailResponseDto);
        } catch (Exception e) {
            PodFailResponseDto podFailResponseDto = PodFailResponseDto.builder()
                    .message("Pod 삭제 실패 : " +e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(podFailResponseDto);
        }
    }

}
