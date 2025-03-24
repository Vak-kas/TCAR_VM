package com.hanbat.dotcar.kubernetes.service;

import com.hanbat.dotcar.kubernetes.domain.PodStatus;
import com.hanbat.dotcar.kubernetes.dto.UserRoleResponseDto;
import com.hanbat.dotcar.kubernetes.repository.PodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class ValidateService {
    private final WebClient webClient;
    private final PodRepository podRepository;


    private static final Map<String, Integer> ROLE_MAX_CONTAINERS = Map.of(
            "ADMIN", Integer.MAX_VALUE, // ADMIN은 무제한
            "BASIC", 1
    );


    //테스트용
    public String getUserRole(String userEmail){
        return "BASIC";
    }
    //*** 유저 권한(등급) 가져오기 ***//
    //         -> 유저 존재 여부 확인
    public String getUserRole2(String userEmail){
        String URL = "/api/users/role";  // -> URL 백엔드에서 받아온 정보 넣기
        UserRoleResponseDto userRoleResponseDto;
        try {
            userRoleResponseDto = webClient.get() //GET 요청
                    .uri(uriBuilder ->
                            uriBuilder
                                    .path(URL) //상대경로 설정
                                    .queryParam("email", userEmail) //쿼리 설정
                                    .build())
                    .retrieve()
                    .bodyToMono(UserRoleResponseDto.class)
                    .block();

            String role = userRoleResponseDto.getRole();
            if (role == null || role.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저가 존재하지 않습니다.");
            }
            return role;
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "유저 정보를 조회 실패");
        }
    }


    //*** 해당 유저의 Pod 생성 조건 확인 ***//
    public boolean createPodUserPermission(String userEmail){
        String role = getUserRole(userEmail);

        //해당 유저의 현재 실행중인 컨테이너 개수
        int runningContainerCount = podRepository.countByUserEmailAndStatus(userEmail, PodStatus.RUNNING);

        //해당 유저 권한에 따른 최대 컨테이너 생성 개수
        int maxAllowedContainerCount = ROLE_MAX_CONTAINERS.get(role);

        //최대 생성 개수 안 넘었으면 true,넘었으면 false -> 생성 가능하면 true, 생성 불가능하면 false
        if(maxAllowedContainerCount <= runningContainerCount){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "컨테이너 생성 조건을 만족하지 못합니다.");
        }
        return true;

    }

    public boolean deletePodUserPermission(String userEmail){
        String role = getUserRole(userEmail);
        return true;

    }

}
