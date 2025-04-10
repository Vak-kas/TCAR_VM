package com.hanbat.dotcar.access.service;

import com.hanbat.dotcar.access.AccessRepository;
import com.hanbat.dotcar.access.domain.AccessAuthority;
import com.hanbat.dotcar.kubernetes.domain.Pod;
import com.hanbat.dotcar.kubernetes.domain.PodStatus;
import com.hanbat.dotcar.kubernetes.repository.PodRepository;
import com.hanbat.dotcar.kubernetes.service.IngressService;
import com.hanbat.dotcar.kubernetes.service.PermissionService;
import com.hanbat.dotcar.kubernetes.service.PodService;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1Pod;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccessService {
    private final ValidateService validateService;
    private final PodRepository podRepository;
    private final AccessAuthorityService accessAuthorityService;
    private final IngressService ingressService;
    private final PodService podService;
    private final AccessRepository accessRepository;

    public String accessPod(String token){
        // 토큰 유효 여부 확인
        Map<String, String> claims = validatePresignedUrl(token);
        String userEmail = claims.get("userEmail");
        String podName = claims.get("podName");
        String podNamespace = claims.get("podNamespace");

        //해당 pod에 접근권한이 있는지 확인
        Optional<Pod> findPod = podRepository.findByPodNameAndPodNamespace(podName, podNamespace);
        if(findPod.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 서버가 존재하지 않습니다.");
        }
        Pod pod = findPod.get();
        V1Pod v1Pod;
        PodStatus podStatus;

        try{
            v1Pod = podService.getPod(podName, podNamespace);
            podStatus = podService.getPodStatus(v1Pod);
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "해당 서버가 존재하지 않습니다.");
        }

        if(!PodStatus.RUNNING.equals(podStatus)){
            //TODO : PodStatus 동기화
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 서버가 실행 중이지 않습니다.");
        }


        //파드에 접근 권한 확인
        boolean isOwner = userEmail.equals(pod.getUserEmail()); //소유주가 본인일 경우에
        List<String> grantedUsers = accessAuthorityService.getAuthority(pod);
        boolean isGranted = grantedUsers.contains(userEmail); //접근 권한이 존재할 때

        if(!(isOwner || isGranted)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 서버에 접근 권한이 없습니다.");
        }

        String ingress = claims.get("ingress");
        String redirectUrl = "http://" + ingress;

        return redirectUrl;
    }


    //*** token으로부터 ingress와 ingressPath 일치 여부 확인, token 유효 여부 확인
    public Map<String, String> validatePresignedUrl(String token){
        Map<String, String> claims = validateService.getClaimsFromToken(token);
        String userEmail = claims.get("userEmail");
        String podName = claims.get("podName");
        String podNamespace = claims.get("podNamespace");
        String ingressPath = claims.get("ingress");

        //podName, popdNamespace, userEmail을 이용해서 조합한 ingress와 ingressPath값이 일치하는지 확인
        String ingress = ingressService.generateIngressUrl(userEmail, podNamespace, podName);

        if(!ingress.equals(ingressPath)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "토큰 값이 유효하지 않습니다.");
        }

        return claims;

    }



}
