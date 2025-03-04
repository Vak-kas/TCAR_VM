package com.hanbat.dotcar.access;
import com.hanbat.dotcar.access.dto.AccessibleContainerDto;
import com.hanbat.dotcar.container.Container;
import com.hanbat.dotcar.container.ContainerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.awt.event.ContainerAdapter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AccessService {
//    private final String myServer = "http://localhost:";
    private final String myServer = "http://192.168.1.5:";
    private final URLValidateService URLValidateService;
    private final ContainerService containerService;
    private final AccessRepository accessRepository;
    private final AccessAuthorityService accessAuthorityService;


    // *** 컨테이너 접근하기 *** //
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


    // *** 접근 가능한 컨테이너 목록 조회하기 *** //
    public List<AccessibleContainerDto> getAccessibleContainers(String userEmail){
        List<Container> containers = accessRepository.findAccessibleRunningContainersByUserEmail(userEmail);
        if(containers.isEmpty()){
            return Collections.EMPTY_LIST;
        }
        List<AccessibleContainerDto> result = new ArrayList<>();

        for (Container container : containers){
            String accessType = container.getMadeBy().equals(userEmail) ? "owner" : "guest";
            AccessibleContainerDto accessibleContainerDto = AccessibleContainerDto.builder()
                    .containerId(container.getContainerId())
                    .containerName(container.getContainerName())
                    .port(container.getHostPort())
                    .accessType(accessType)
                    .build();

            result.add(accessibleContainerDto);
        }

        return result;

    }




}
