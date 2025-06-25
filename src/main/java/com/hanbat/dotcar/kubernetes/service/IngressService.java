package com.hanbat.dotcar.kubernetes.service;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IngressService {
    private final PermissionService permissionService;
    private final NetworkingV1Api networkingV1Api;

    private final String BASIC_HOST = "tcar.basic.connection.com";
    private final String ADMIN_HOST = "tcar.admin.connection.com";
    private final String DEFAULT_NAMESPACE = "default";
    private final String USER_INGRESS = "user-ingress";

    //*** IngressUrl 생성기 *** //
    public String generateIngressUrl(String userEmail, String namespace, String podName) {
        String userRole = permissionService.getUserRole(userEmail);
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

    private String getIngressHost(String userRole){
        String host;
        switch (userRole.toUpperCase()){
            case "BASIC" :
                host = BASIC_HOST;
                break;
            case "ADMIN" :
                host = ADMIN_HOST;
                break;
            default:
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "유저 찾기 오류");
        }
        return host;
    }

    //*** UserRole이 BASIC일 경우 URL 주소 ***//
    private String basicIngressUrl(String userEmail, String namespace, String podName){
        return String.format("tcar.basic.connection.com/%s/%s", namespace, podName);
    }

    //*** UserRole이 ADMIN일 경우 URL 주소 ***//
    private String adminIngressUrl(String userEmail, String namespace, String podName){
        return String.format("tcar.admin.connection.com/%s/%s", namespace, podName);
    }


    //*** Ingress Path 추가 ***//
    public void addIngresPath(String userRole, V1Service v1Service){
        String namespace = v1Service.getMetadata().getNamespace();
        String serviceName = v1Service.getMetadata().getName();
        String podName;
        if(serviceName.startsWith("svc-")){
            podName = serviceName.substring(4);
        }
        else{
            podName = serviceName;
        }
        String ingressName = USER_INGRESS;
        String host = getIngressHost(userRole);
        String path = "/" + namespace + "/" + podName;

        try{
            V1Ingress v1Ingress = networkingV1Api.readNamespacedIngress(ingressName, namespace).execute();
            List<V1IngressRule> rules = v1Ingress.getSpec().getRules();

            for (V1IngressRule rule : rules){
                if(rule.getHost().equals(host)){
                    List<V1HTTPIngressPath> paths = rule.getHttp().getPaths();

                    V1HTTPIngressPath v1HTTPIngressPath = new V1HTTPIngressPath()
                            .path(path)
                            .pathType("Prefix")
                            .backend(new V1IngressBackend()
                                    .service(new V1IngressServiceBackend()
                                            .name(serviceName)
                                            .port(new V1ServiceBackendPort().number(80))));

                    paths.add(v1HTTPIngressPath);
                    break;
                }
            }
            networkingV1Api.replaceNamespacedIngress(ingressName, DEFAULT_NAMESPACE, v1Ingress).execute();

        } catch (ApiException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Ingress 생성 실패");
        }

    }

    public V1Ingress getIngress(String podName, String podNamespace) throws ApiException{
        V1Ingress v1Ingress = networkingV1Api.readNamespacedIngress(USER_INGRESS, podNamespace).execute();
        return v1Ingress;
    }

    public void deleteIngressPath(V1Ingress v1Ingress, V1Service v1Service, String userRole) throws ApiException {
        List<V1IngressRule> rules = v1Ingress.getSpec().getRules();
        String host = getIngressHost(userRole);
        String podNamespace = v1Service.getMetadata().getNamespace();
        String serviceName = v1Service.getMetadata().getName();

        for(V1IngressRule rule : rules){
            if(rule.getHost().equals(host)){
                List<V1HTTPIngressPath> paths = rule.getHttp().getPaths();
                paths.removeIf(path -> path.getBackend().getService().getName().equals(serviceName));
            }
        }

        networkingV1Api.replaceNamespacedIngress(USER_INGRESS, podNamespace, v1Ingress).execute();

    }



}
