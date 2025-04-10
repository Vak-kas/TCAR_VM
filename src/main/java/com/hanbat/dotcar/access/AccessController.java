package com.hanbat.dotcar.access;

import com.hanbat.dotcar.access.dto.AccessFailResponseDto;
import com.hanbat.dotcar.access.service.AccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/access")
public class AccessController {
    private final AccessService accessService;

    @GetMapping("/presigned/validate")
    public ResponseEntity<?> accessPod(@RequestParam String token) {
        String redirectUrl;

        //토큰 값 확인
        if (token == null || token.isBlank()){
            AccessFailResponseDto accessFailResponseDto = AccessFailResponseDto.builder()
                    .message("토큰이 필요합니다")
                    .build();
            return ResponseEntity.badRequest().body(accessFailResponseDto);
        }
        try{
            redirectUrl = accessService.accessPod(token);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, redirectUrl)
                    .build();
        } catch(ResponseStatusException e){
            AccessFailResponseDto accessFailResponseDto = AccessFailResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(accessFailResponseDto);
        }
    }
}
