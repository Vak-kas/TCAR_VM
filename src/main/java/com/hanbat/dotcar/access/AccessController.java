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
    private final AccessService accessService;

    @GetMapping("/")
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
}
