package com.hanbat.dotcar.access;
import com.hanbat.dotcar.container.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccessService {
    private final URLValidateService URLValidateService;
    private final ContainerService containerService;

    public String accessContainer(String token){
        Map<String, String> claims = URLValidateService.getClaimsFromToken(token);
        String containerId = claims.get("containerId");
        String port = claims.get("port");
        String userEmail = claims.get("userEmail");


        //TODO : 컨테이너 실행중인지 확인

        //TODO : 컨테이너 접근 권한 있는지 확인


        return "http://localhost:" + port;


    }



}
