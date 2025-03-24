package com.hanbat.dotcar.kubernetes.service;

import com.hanbat.dotcar.kubernetes.domain.Pod;
import com.hanbat.dotcar.kubernetes.domain.PodStatus;
import com.hanbat.dotcar.kubernetes.dto.DeletePodRequestDto;
import com.hanbat.dotcar.kubernetes.repository.PodRepository;
import com.hanbat.dotcar.kubernetes.dto.CreatePodRequestDto;
import com.hanbat.dotcar.kubernetes.dto.PodInfoDto;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class InstanceService {
    private final PodRepository podRepository;
    private final CoreV1Api coreV1Api;
    private final ValidateService validateService;
    private final IngressService ingressService;
    private final PodService podService;
    private final ServiceService serviceService;

    public PodInfoDto createInstance(CreatePodRequestDto requestDto) throws ApiException{
        String os = requestDto.getOs();
        String version = requestDto.getVersion();
        String userEmail = requestDto.getUserEmail();
        String userRole = validateService.getUserRole(userEmail);


        // 생성 권한 확인
        if(!validateService.createPodUserPermission(userEmail)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "서버 생성 조건을 만족하지 못합니다.");
        }

        //컨테이너 생성
        V1Pod pod = podService.createPodSpec(os, version);
        String namespace = pod.getMetadata().getNamespace();
        String podName = pod.getMetadata().getName();
        PodStatus podStatus = podService.getPodStatus(pod);

        //서비스 생성
        V1Service service = serviceService.createV1Service(namespace, podName);

        //ingress rule 생성
        ingressService.addIngresPath(userRole, service);



        //TODO : PENDING으로 저장되는 과정 확인
        Pod dbPod = Pod.builder()
                .podName(podName)
                .podNamespace(namespace)
                .os(os)
                .version(version)
                .createdAt(new Date())
                .userEmail(userEmail)
                .ingress(ingressService.generateIngressUrl(userEmail, namespace, podName))
                .status(podStatus)
                .build();

        podRepository.save(dbPod);


        PodInfoDto podInfoDto = PodInfoDto.builder()
                .podName(podName)
                .podNamespace(namespace)
                .build();


        return podInfoDto;
    }


    public void deleteInstance(DeletePodRequestDto deletePodRequestDto) throws ApiException{
        String userEmail = deletePodRequestDto.getUserEmail();
        String userRole = validateService.getUserRole(userEmail);

        String podNamespace = deletePodRequestDto.getPodNamespace();
        String podName = deletePodRequestDto.getPodName();

        String serviceName = "svc-" + podName;

        //TODO : 삭제 권한 확인

        //podNamespace와 podName으로 pod 찾기
        V1Pod v1Pod = podService.getPod(podName, podNamespace);

        //해당 pod와 연결된 service찾기
        V1Service v1Service = serviceService.getService(serviceName, podNamespace);

        //Ingress 불러오기
        V1Ingress v1Ingress = ingressService.getIngress(podName, podNamespace);

        //해당 ingress에서 해당 서비스를 찾아보는 rule 삭제
        ingressService.deleteIngressPath(v1Ingress, v1Service, userRole);

        //서비스 삭제
        serviceService.deleteService(v1Service);

        //Pod 삭제
        podService.deletePod(podName, podNamespace);



    }



}
