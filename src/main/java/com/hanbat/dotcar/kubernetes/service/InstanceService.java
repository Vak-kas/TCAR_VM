package com.hanbat.dotcar.kubernetes.service;

import com.hanbat.dotcar.kubernetes.domain.Pod;
import com.hanbat.dotcar.kubernetes.domain.PodStatus;
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


        // 생성 권한 확인
        if(!validateService.createPodUserPermission(userEmail)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "서버 생성 조건을 만족하지 못합니다.");
        }

        V1Pod pod = podService.createPodSpec(os, version, userEmail);
        String namespace = pod.getMetadata().getNamespace();
        String podName = pod.getMetadata().getName();
        PodStatus podStatus = podService.getPodStatus(pod);

        V1Service service = serviceService.createV1Service(namespace, podName);
        ingressService.addIngresPath(userEmail, namespace, podName);


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



}
