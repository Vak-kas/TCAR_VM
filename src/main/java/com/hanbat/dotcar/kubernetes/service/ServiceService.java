package com.hanbat.dotcar.kubernetes.service;

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.proto.V1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ServiceService {
    private final CoreV1Api coreV1Api;


    public V1Service createV1Service(String namespace, String podName) {
        String serviceName = "svc-" + podName;

        // Service 객체 생성
        V1Service service = new V1Service()
                .apiVersion("v1")
                .kind("Service")
                .metadata(new V1ObjectMeta()
                        .name(serviceName)
                        .namespace(namespace)
                        .labels(Collections.singletonMap("app", podName)))
                .spec(new V1ServiceSpec()
                        .selector(Collections.singletonMap("app", podName))
                        .ports(Collections.singletonList(new V1ServicePort()
                                .port(80)
                                .targetPort(new IntOrString(80)))));

        try {
            coreV1Api.createNamespacedService(namespace, service).execute();
        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Service 생성 중 오류 발생");
        }

        return service;
    }


    public V1Service getService(String serviceName, String podNamespace) throws ApiException{
        V1Service v1Service = coreV1Api.readNamespacedService(serviceName, podNamespace).execute();
        return v1Service;
    }

    public void deleteService(V1Service v1Service) throws  ApiException{
        String serviceName = v1Service.getMetadata().getName();
        String podNamespace = v1Service.getMetadata().getNamespace();

        coreV1Api.deleteNamespacedService(serviceName, podNamespace).execute();
    }
}
