package com.hanbat.dotcar.access.service;

import com.hanbat.dotcar.access.AccessRepository;
import com.hanbat.dotcar.access.domain.AccessAuthority;
import com.hanbat.dotcar.kubernetes.domain.Pod;
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
    public void setAuthority(Pod pod, String userEmail){
        String podName = pod.getPodName();
        String podNamespace = pod.getPodNamespace();

        //접근 권한 중복 확인
        Optional<AccessAuthority> member = accessRepository.findByPodAndUserEmail(pod, userEmail);
        if(member.isPresent()){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "이미 해당 파드에 접근할 수 있는 권한이 존재합니다.");
        }

        AccessAuthority accessAuthority = AccessAuthority.builder()
                .pod(pod)
                .userEmail(userEmail)
                .build();

        accessRepository.save(accessAuthority);
    }


    // *** pod의 접근 권한 가져오기 *** //
    public List<String> getAuthority(Pod pod){
        List<AccessAuthority> authorities = accessRepository.findByPod(pod);

        return authorities.stream()
                .map(AccessAuthority::getUserEmail)
                .toList();
    }
}
