package com.hanbat.dotcar.access.service;

import com.hanbat.dotcar.access.domain.AccessAuthority;
import com.hanbat.dotcar.kubernetes.repository.PodRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AccessService {
    private final ValidateService validateService;
    private final PodRepository podRepository;
    private final AccessAuthorityService accessAuthorityService;

    public void accessPod(String token){
        // 토큰 유효 여부 확인
        Map<String, String> claims = validatePresignedUrl(token);
        String userEmail = claims.get("userEmail");
        String podName = claims.get("podName");
        String podNamespace = claims.get("podNamespace");
        String ingressPath = claims.get("ingress");

        //TODO : 해당 podName에 접근권한이 있는지 확인
    }


    //*** token으로부터 ingress와 ingressPath 일치 여부 확인, token 유효 여부 확인
    public Map<String, String> validatePresignedUrl(String token){
        Map<String, String> claims = validateService.getClaimsFromToken(token);
        String userEmail = claims.get("userEmail");
        String podName = claims.get("podName");
        String podNamespace = claims.get("podNamespace");
        String ingressPath = claims.get("ingress");

        //TODO : podName, popdNamespace, userEmail을 이용해서 조합한 ingress와 ingressPath값이 일치하는지 확인


        return claims;

    }



}
