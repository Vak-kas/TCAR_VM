package com.hanbat.dotcar.kubernetes.service;

import com.hanbat.dotcar.kubernetes.domain.PodStatus;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PodService {
    private final ImageService imageService;
    private final PermissionService permissionService;
    private final CoreV1Api coreV1Api;

    private final String DEFAULT_NAMESPACE = "default";


    public V1Pod createPodSpec(String os, String version){
        String namespace = DEFAULT_NAMESPACE;
        String podName = "pod-" + UUID.randomUUID().toString().substring(0, 8);

        //컨테이너 사양
        V1Container v1Container = new V1Container()
                .name(podName)
                .image(imageService.getImage(os, version))
//                .ports(Collections.singletonList(new V1ContainerPort().containerPort(80)));
//                .command(Collections.singletonList("/bin/bash"))
//                .args(Arrays.asList("-c", "while true; do sleep 30; done"))
                .tty(true)
                .stdin(true);

        //Pod 사양
        V1PodSpec v1PodSpec = new V1PodSpec()
                .containers(Collections.singletonList(v1Container))
                .overhead(null);

        //Pod metaData
        V1ObjectMeta v1ObjectMeta = new V1ObjectMeta()
                .name(podName)
                .namespace(namespace)
                .labels(Collections.singletonMap("app", podName));

        //pod 객체 생성
        V1Pod pod = new V1Pod()
                .apiVersion("v1")
                .kind("Pod")
                .metadata(v1ObjectMeta)
                .spec(v1PodSpec);


        //pod 객체로 Pod 생성
        try{
            coreV1Api.createNamespacedPod(namespace, pod).execute();
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 생성 중 오류 발생");
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 생성 중 알 수 없는 오류 발생");
        }

        return pod;
    }


    //*** Pod의 상태 가져오기 ***//
    public PodStatus getPodStatus(V1Pod pod){
        V1Pod updatedPod;
        try {
            updatedPod = coreV1Api.readNamespacedPod(
                    pod.getMetadata().getName(), pod.getMetadata().getNamespace()).execute();
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "최신 정보 조회 실패");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류");
        }

        if (updatedPod.getStatus() == null || updatedPod.getStatus().getPhase() == null) {
            return PodStatus.UNKNOWN; // 기본값 반환
        }

        String phase = updatedPod.getStatus().getPhase().toUpperCase();
        PodStatus podStatus;
        if(phase == null){
            return PodStatus.UNKNOWN;
        }
        switch (phase.toUpperCase()){
            case "PENDING":
                podStatus = PodStatus.PENDING;
                break;
            case "RUNNING":
                podStatus = PodStatus.RUNNING;
                break;
            case "SUCCEEDED" :
                podStatus = PodStatus.SUCCEEDED;
                break;
            case "FAILED" :
                podStatus = PodStatus.FAILED;
                break;
            default :
                podStatus = PodStatus.UNKNOWN;
                break;
        };
        return podStatus;
    }

    public PodStatus waitForRunning(V1Pod pod){
        int maxRetry = 10;
        int retry = 0;
        int delayMs = 1000; //1초


        //Polling
        while (retry < maxRetry){
            PodStatus status = getPodStatus(pod);
            if(status == PodStatus.RUNNING){
                return status;
            }
            try{
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();;
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "");
            }
            retry++;
        }
        throw new ResponseStatusException(HttpStatus.REQUEST_TIMEOUT, "Pod가 예상 시간 내에 Running 상태가 되지 않았습니다.");

    }


    public V1Pod getPod(String podName, String podNamespace) throws ApiException {
        V1Pod v1Pod = coreV1Api.readNamespacedPod(podName, podNamespace).execute();
        return v1Pod;
    }

    public void deletePod(String podName, String podNamespace) throws ApiException{
        V1Pod v1Pod = coreV1Api.deleteNamespacedPod(podName, podNamespace).execute();
    }


}
