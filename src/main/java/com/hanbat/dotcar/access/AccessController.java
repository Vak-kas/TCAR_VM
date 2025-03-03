package com.hanbat.dotcar.access;


import com.hanbat.dotcar.access.dto.AccessFailResponseDto;
import com.hanbat.dotcar.access.dto.AccessibleContainerDto;
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

    // ***  컨테이너 접속 *** //
    @GetMapping("/container")
    public ResponseEntity<?> accessContainer(@RequestParam String token){
        try{
            String containerURL = accessService.accessContainer(token);
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, containerURL)
                    .build();
        } catch(ResponseStatusException e){
            AccessFailResponseDto accessFailResponseDto = AccessFailResponseDto.builder()
                    .message(e.getReason())
                    .build();

            return ResponseEntity.status(e.getStatusCode()).body(accessFailResponseDto);
        }
    }



    //*** 컨테이너 접근 가능 목록 조회 *** //
    @GetMapping("/accessible")
    public ResponseEntity<?> getAccessibleContainer(@RequestParam String userEmail){
        try{
            List<AccessibleContainerDto> accessibleContainerDtos = accessService.getAccessibleContainers(userEmail);
            return ResponseEntity.status(HttpStatus.OK).body(accessibleContainerDtos);
        } catch (ResponseStatusException e) {
            AccessFailResponseDto accessFailResponseDto = AccessFailResponseDto.builder()
                    .message("접근 가능한 컨테이너를 확인할 수 없습니다.")
                    .build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(accessFailResponseDto);
        }

    }
}
