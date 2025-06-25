package com.hanbat.dotcar.access.service;

import com.hanbat.dotcar.access.AccessRepository;
import com.hanbat.dotcar.access.domain.AccessAuthority;
import com.hanbat.dotcar.access.dto.AccessiblePodDto;
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

import java.util.*;

@Service
@RequiredArgsConstructor
public class AccessService {
    private final ValidateService validateService;
    private final PodRepository podRepository;
    private final AccessAuthorityService accessAuthorityService;
    private final IngressService ingressService;
    private final PodService podService;
    private final AccessRepository accessRepository;

    public void accessPod(String token){
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

//        String ingress = claims.get("ingress");
//        String redirectUrl = "http://" + ingress;
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

    public List<AccessiblePodDto> getAccessiblePods(String userEmail) {
        // DB에서 이 사용자가 접근 권한을 가진 pod 이름/namespace를 가져옴
        List<Pod> pods = accessRepository.findAccessibleRunningPodsByUserEmail(userEmail);
        if (pods.isEmpty()) {
            return Collections.emptyList();
        }

        List<AccessiblePodDto> result = new ArrayList<>();
        for (Pod pod : pods) {
            String podName = pod.getPodName();  // AccessAuthority 엔티티에 있어야 함
            String namespace = pod.getPodNamespace();  // 기본 default
            String ingressUrl = pod.getIngress();

            V1Pod realPod;
            try{
                realPod = podService.getPod(podName, namespace);
            } catch (ApiException e) {
                // TODO : 못불러 온 이유 찾고 처리하는 코드 필요
                System.err.println("⚠️ Pod 상태 조회 실패: " + podName + " - 건너뜀");
                continue;
            }

            PodStatus status = podService.getPodStatus(realPod);

            String accessType = pod.getUserEmail().equals(userEmail) ? "owner" : "guest";


            AccessiblePodDto dto = AccessiblePodDto.builder()
                    .podName(podName)
                    .namespace(namespace)
                    .status(status)
                    .ingressUrl(ingressUrl)
                    .accessType(accessType)
                    .build();

            result.add(dto);
        }

        return result;
    }



}
