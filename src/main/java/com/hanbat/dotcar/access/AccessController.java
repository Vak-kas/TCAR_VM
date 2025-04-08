package com.hanbat.dotcar.access;

import com.hanbat.dotcar.access.dto.AccessFailResponseDto;
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

    @GetMapping("/presigned/validate")
    public ResponseEntity<?> accessPod(
            @RequestParam String token,
            @RequestHeader("X-Original-URI") String uri) {
        try{
            //TODO : TOKEN검증, token/uri 일치 여부 확인 / 만료 여부 확인
            return ResponseEntity.ok().build();
        } catch(ResponseStatusException e){
            AccessFailResponseDto accessFailResponseDto = AccessFailResponseDto.builder()
                    .message(e.getReason())
                    .build();
            return ResponseEntity.status(e.getStatusCode()).body(accessFailResponseDto);
        }
    }
}
