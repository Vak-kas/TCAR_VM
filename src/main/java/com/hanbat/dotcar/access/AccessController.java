package com.hanbat.dotcar.access;

import com.hanbat.dotcar.access.domain.AccessAuthority;
import com.hanbat.dotcar.access.dto.AccessFailResponseDto;
import com.hanbat.dotcar.access.dto.AccessiblePodDto;
import com.hanbat.dotcar.access.service.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/access")
public class AccessController {
    private final AccessService accessService;

    private final String KUBE_IP = "127.0.0.1";

    @GetMapping("/presigned/validate")
    public ResponseEntity<?> accessPod(@RequestParam String token,
                                       @RequestParam String podName,
                                       @RequestParam String podNamespace) {
        String redirectUrl;
        System.out.println("1차 검증");

        //토큰 값 확인
        if (token == null || token.isBlank()){
            AccessFailResponseDto accessFailResponseDto = AccessFailResponseDto.builder()
                    .message("토큰이 필요합니다")
                    .build();
            return ResponseEntity.badRequest().body(accessFailResponseDto);
        }

        //podName 값 확인
        if (podName == null || podName.isBlank()){
            AccessFailResponseDto accessFailResponseDto = AccessFailResponseDto.builder()
                    .message("서버 이름이 필요합니다")
                    .build();
            return ResponseEntity.badRequest().body(accessFailResponseDto);
        }

        //podNamespace값 확인
        if (podNamespace == null || podNamespace.isBlank()){
            AccessFailResponseDto accessFailResponseDto = AccessFailResponseDto.builder()
                    .message("서버 범위가 필요합니다")
                    .build();
            return ResponseEntity.badRequest().body(accessFailResponseDto);
        }

        try{
            accessService.accessPod(token);

            redirectUrl = String.format(
                    "ws://%s/ws/terminal?token=%s&podName=%s&podNamespace=%s",
                    KUBE_IP, token, podName, podNamespace
            );
            return ResponseEntity.status(HttpStatus.OK).body(redirectUrl);
        } catch(ResponseStatusException e){
            AccessFailResponseDto accessFailResponseDto = AccessFailResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(accessFailResponseDto);
        }
    }

    @GetMapping("/pods")
    public ResponseEntity<List<AccessiblePodDto>> getAccessiblePods(@RequestParam String userEmail) {
        List<AccessiblePodDto> pods = accessService.getAccessiblePods(userEmail);
        return ResponseEntity.ok(pods);
    }



}
