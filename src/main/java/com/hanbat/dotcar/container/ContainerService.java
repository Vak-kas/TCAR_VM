package com.hanbat.dotcar.container;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.io.LineIterator;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class ContainerService {
    /**************************/
    /** 공식 문서 사용 도커 연결  **/
    // DockerClientConfig 인스턴스 생성 -> Docker 데몬에 접근할 수 있도록 환경 설정(예: DOCKER_HOST, 인증 관련 정보 등)을 제공하는 객체를 생성
    private final DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();

    // DockerHttpClient 인스턴스 생성 -> Docker 데몬과 HTTP 통신을 하기 위한 클라이언트를 생성
    private final DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())          // DockerClientConfig에서 Docker 호스트 정보 가져오기
            .sslConfig(config.getSSLConfig())              // SSL 구성 (TLS 인증서 등)
            .maxConnections(100)                           // 최대 연결 수 설정
            .connectionTimeout(Duration.ofSeconds(30))     // 연결 타임아웃 설정
            .responseTimeout(Duration.ofSeconds(45))       // 응답 타임아웃 설정
            .build();

    // DockerClient 인스턴스 생성 -> DockerClientConfig와 DockerHttpClient를 결합하여 Docker 데몬에 명령을 전달할 수 있는 DockerClient 객체를 생성
    private final DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
    /**************************/
}
