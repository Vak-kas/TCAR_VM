package com.hanbat.dotcar.access;

import com.hanbat.dotcar.container.Container;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccessAuthorityService {
    private final AccessRepository accessRepository;

    // *** 접근 권한 부여 *** //
    public void setAuthority(Container container, String userEmail){
        String containerId = container.getContainerId();

        //접근 권한 중복 확인
        Optional<AccessAuthority> member = accessRepository.findByContainerAndUserEmail(container, userEmail);
        if(member.isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 컨테이너에 접근할 수 있는 권한이 존재합니다.");
        }

        AccessAuthority accessAuthority = AccessAuthority.builder()
                .container(container)
                .userEmail(userEmail)
                .build();

        accessRepository.save(accessAuthority);
    }


    // *** 컨테이너의 접근 권한 가져오기 *** //
    public List<String> getAuthority(Container container){
        List<AccessAuthority> authorities = accessRepository.findByContainer(container);

        return authorities.stream()
                .map(AccessAuthority::getUserEmail)
                .toList();
    }


}
