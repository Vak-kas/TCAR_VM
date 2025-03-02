package com.hanbat.dotcar.access;
import com.hanbat.dotcar.container.Container;
import com.hanbat.dotcar.container.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.awt.event.ContainerAdapter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccessService {
    private final String myServer = "http://localhost:";
    private final URLValidateService URLValidateService;
    private final ContainerService containerService;
    private final AccessRepository accessRepository;
    private final AccessAuthorityService accessAuthorityService;

    public String accessContainer(String token){
        Map<String, String> claims = URLValidateService.getClaimsFromToken(token);
        String containerId = claims.get("containerId");
        String port = claims.get("port");
        String userEmail = claims.get("userEmail");


        //컨테이너 실행중인지 확인
        Container container = containerService.getContainer(containerId);
        String dockerStatus = containerService.getContainerStatus(containerId);

        if(!dockerStatus.equals("running")){
            containerService.syncContainerStatus(container);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 컨테이너가 실행 중이지 않습니다.");
        }


        //컨테이너 접근 권한 확인
        Optional<AccessAuthority> member = accessRepository.findByContainerAndUserEmail(container, userEmail);
        if(!member.isPresent()){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 컨테이너의 접근 권한이 없습니다.");
        }

        return myServer + port;
    }




}
