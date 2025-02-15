package com.hanbat.dotcar.container;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ValidateService {
    private final ContainerRepository containerRepository;
    private final WebClient webClient; //WebClientConfig에서 Bean 주입


    // Role별 최대 생성 가능 컨테이너 개수 설정
    private static final Map<String, Integer> ROLE_MAX_CONTAINERS = Map.of(
            "ADMIN", Integer.MAX_VALUE, // ADMIN은 무제한
            "BASIC", 1
    );


    //*** 유저 권한(등급) 가져오기 ***//
    //         -> 유저 존재 여부 확인
    public String getUserRole(String userEmail){
        String URL = "/api/user-role";  // -> URL 백엔드에서 받아온 정보 넣기
        String role;
        try {
            role = webClient.get() //GET 요청
                    .uri(uriBuilder ->
                            uriBuilder
                                    .path(URL) //상대경로 설정
                                    .queryParam("email", userEmail) //쿼리 설정
                                    .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            return (role != null && !role.isEmpty()) ? role : null;
        } catch (Exception e){
//            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "유저 정보를 조회 실패");
            return null;
        }
    }


    // *** 해당 유저의 컨테이너 생성 조건 확인
    public boolean createContainerUserPermission(String userEmail, String role){
        // ADMIN이면 무조건 생성 가능
        if("ADMIN".equals(role)){
            return true;
        }

        //해당 유저의 현재 실행중인 컨테이너 개수
        int runningContainerCount = containerRepository.countByMadeByAndStatus(userEmail, "running");

        //해당 유저 권한에 따른 최대 컨테이너 생성 개수
        int maxAllowedContainerCount = ROLE_MAX_CONTAINERS.get(role);

        //최대 생성 개수 안 넘었으면 true,넘었으면 false
        return maxAllowedContainerCount > runningContainerCount;


    }
}
