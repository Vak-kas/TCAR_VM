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
    private final ImageService imageService;
    private final CoreV1Api coreV1Api;
    private final ValidateService validateService;

    public PodInfoDto createPod(CreatePodRequestDto requestDto) throws ApiException{
        String os = requestDto.getOs();
        String version = requestDto.getVersion();
        String userEmail = requestDto.getUserEmail();
        String namespace = "default";
        String podName = "pod-" + UUID.randomUUID().toString().substring(0, 8);

        // 생성 권한 확인
        if(!validateService.createPodUserPermission(userEmail)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "서버 생성 조건을 만족하지 못합니다.");
        }

        //TODO : Pod 생성 Service로 넘기기

        //컨테이너 사양
        V1Container v1Container = new V1Container()
                .name(podName)
                .image(imageService.getImage(os, version))
                .ports(Collections.singletonList(new V1ContainerPort().containerPort(80)))
                .command(Collections.singletonList("/bin/bash"))
                .args(Arrays.asList("-c", "while true; do sleep 30; done"));

        //Pod 사양
        V1PodSpec v1PodSpec = new V1PodSpec()
                .containers(Collections.singletonList(v1Container))
                .overhead(null);

        String userRole = validateService.getUserRole(userEmail);
        //Pod metaData
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta()
                .name(podName)
                .namespace(namespace)
                .labels(Collections.singletonMap("role", userRole));



        //pod 생성
        V1Pod pod = new V1Pod()
                .apiVersion("v1")
                .kind("Pod")
                .metadata(v1ObjectMeta)
                .spec(v1PodSpec);

        // CoreV1Api를 사용하여 Pod 생성
        //TODO: 예외 처리
        coreV1Api.createNamespacedPod(namespace, pod).execute();


        //TODO : 데이터베이스 저장
        Pod dbPod = Pod.builder()
                .podName(podName)
                .podNamespace(namespace)
                .os(os)
                .version(version)
                .createdAt(new Date())
                .userEmail(userEmail)
                .ingress("N/A") //TODO: ingress값 생성후에 집어넣기
                .status(PodStatus.RUNNING) //TODO: 실제 방금 생성한 Pod의 상태 집어넣을 것
                .build();


        PodInfoDto podInfoDto = PodInfoDto.builder()
                .podName(podName)
                .podNamespace(namespace)
                .build();


        return podInfoDto;
    }



}
