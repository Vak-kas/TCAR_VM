package com.hanbat.dotcar.container;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import org.apache.commons.io.LineIterator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.print.Doc;
import java.time.Duration;

@Service
public class ContainerService {

    /**************************/
    /** 공식 문서 사용 도커 연결  **/
    // DockerClientConfig 인스턴스 생성 -> Docker 데몬에 접근할 수 있도록 환경 설정(예: DOCKER_HOST, 인증 관련 정보 등)을 제공하는 객체를 생성
    private final DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//            .withDockerHost("unix:///var/run/docker.sock")
            .build();


    // DockerHttpClient 인스턴스 생성 -> Docker 데몬과 HTTP 통신을 하기 위한 클라이언트를 생성
//    private final DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
//            .dockerHost(config.getDockerHost())          // DockerClientConfig에서 Docker 호스트 정보 가져오기
//            .sslConfig(config.getSSLConfig())              // SSL 구성 (TLS 인증서 등)
//            .maxConnections(100)                           // 최대 연결 수 설정
//            .connectionTimeout(Duration.ofSeconds(30))     // 연결 타임아웃 설정
//            .responseTimeout(Duration.ofSeconds(45))       // 응답 타임아웃 설정
//            .build();

    DockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())          // DockerClientConfig에서 Docker 호스트 정보 가져오기
            .sslConfig(config.getSSLConfig())              // SSL 구성 (TLS 인증서 등)
            .maxConnections(100)                           // 최대 연결 수 설정
            .connectionTimeout(Duration.ofSeconds(30))     // 연결 타임아웃 설정
            .responseTimeout(Duration.ofSeconds(45))       // 응답 타임아웃 설정
            .build();


    // DockerClient 인스턴스 생성 -> DockerClientConfig와 DockerHttpClient를 결합하여 Docker 데몬에 명령을 전달할 수 있는 DockerClient 객체를 생성
    private final DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
    /**************************/

    public ContainerInfoDto createContainer(CreateContainerRequestDto createContainerRequestDto){
        //TODO : 이메일 검증


        //TODO : 버전별로 나누기
        if(!("Ubuntu").equals(createContainerRequestDto.getOs()) ||
         !("22.04").equals(createContainerRequestDto.getVersion())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재는 Ubuntu 22.04만 지원합니다.");
        }

        //TODO :  후에 도커 이미지를 따로 경량화 시켰을 경우 해당 이미지로 변경

        //TODO : 입력받은 것으로 하기
        String imageName = "ubuntu:22.04";


        //컨테이너 생성
        CreateContainerResponse containerResponse = dockerClient.createContainerCmd(imageName)
                .exec();

        System.out.println("컨텐 아이디 : " + containerResponse.getId());


        //컨테이너 아이디 가져오기
        String containerId = containerResponse.getId();

        //컨테이너 아이디, 포트번호 return
        ContainerInfoDto containerInfoDto = ContainerInfoDto.builder()
                .containerId(containerId)
                .port("N/A") //TODO
                .build();

        //TODO : 데이터베이스 저장

        return containerInfoDto;
        //TODO : 예외 처리
    }


}
