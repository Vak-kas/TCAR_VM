package com.hanbat.dotcar.kubernetes.service;

import lombok.RequiredArgsConstructor;
import org.apache.http.HttpException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class IngressService {
    private final ValidateService validateService;

    //*** IngressUrl 생성기 *** //
    public String generateIngressUrl(String userEmail, String namespace, String podName) {
        String userRole = validateService.getUserRole(userEmail);
        String ingressUrl;
        if (userRole.equals("BASIC")) {
            ingressUrl = basicIngressUrl(userEmail, namespace, podName);

        } else if (userRole.equals("ADMIN")) {
            ingressUrl = adminIngressUrl(userEmail, namespace, podName);

        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다");
        }
        return ingressUrl;
    }

    //*** UserRole이 BASIC일 경우 URL 주소 ***//
    private String basicIngressUrl(String userEmail, String namespace, String podName){
        return String.format("tcar.basic.connection.com/%s/%s/%s", userEmail, namespace, podName);
    }

    //*** UserRole이 ADMIN일 경우 URL 주소 ***//
    private String adminIngressUrl(String userEmail, String namespace, String podName){
        return String.format("tcar.admin.connection.com/%s/%s/%s", userEmail, namespace, podName);
    }


}
