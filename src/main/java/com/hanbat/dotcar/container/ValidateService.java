package com.hanbat.dotcar.container;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ValidateService {
    private final ContainerRepository containerRepository;
    private final WebClient webClient; //WebClientConfig에서 Bean 주입


    //유저 권한(등급) 가져오기 -> 유저 존재 여부 확인
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

    public void createContainerUserPermission(String userEmail, String role, Container container){


    }
}
