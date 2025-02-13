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
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.LineIterator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.print.Doc;
import java.time.Duration;
import java.util.Date;

@RequiredArgsConstructor
@Service
public class ContainerService {
    private final ContainerRepository containerRepository;

    /**************************/
    /** 공식 문서 사용 도커 연결  **/
    // DockerClientConfig 인스턴스 생성 -> Docker 데몬에 접근할 수 있도록 환경 설정(예: DOCKER_HOST, 인증 관련 정보 등)을 제공하는 객체를 생성
    private final DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
//            .withDockerHost("unix:///var/run/docker.sock")
            .build();

    private final DockerHttpClient httpClient = new ZerodepDockerHttpClient.Builder()
            .dockerHost(config.getDockerHost())          // DockerClientConfig에서 Docker 호스트 정보 가져오기
            .sslConfig(config.getSSLConfig())              // SSL 구성 (TLS 인증서 등)
            .maxConnections(100)                           // 최대 연결 수 설정
            .connectionTimeout(Duration.ofSeconds(30))     // 연결 타임아웃 설정
            .responseTimeout(Duration.ofSeconds(45))       // 응답 타임아웃 설정
            .build();


    // DockerClient 인스턴스 생성 -> DockerClientConfig와 DockerHttpClient를 결합하여 Docker 데몬에 명령을 전달할 수 있는 DockerClient 객체를 생성
    private final DockerClient dockerClient = DockerClientImpl.getInstance(config, httpClient);
    /**************************/


    /**************************/
    /****** 컨테이너 생성 ********/
    /**************************/
    public ContainerInfoDto createContainer(CreateContainerRequestDto createContainerRequestDto){
        //TODO : 이메일 검증
        String userEmail = createContainerRequestDto.getUserEmail();



        //TODO : 운영체제별, 버전별로 나누기
        if(!("ubuntu").equals(createContainerRequestDto.getOs()) ||
         !("22.04").equals(createContainerRequestDto.getVersion())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재는 Ubuntu 22.04만 지원합니다.");
        }

        //Version이 비어있을 경우
        String os = createContainerRequestDto.getOs();
        String version = (createContainerRequestDto.getVersion()) == null || createContainerRequestDto.getVersion().isEmpty()
                ? "latest" : createContainerRequestDto.getVersion();


        //TODO :  후에 도커 이미지를 따로 경량화 시켰을 경우 해당 이미지로 변경

        String imageName = os + ":" + version;

        //이미지 존재 여부 확인하고, 없으면 다운로드
        try {
            dockerClient.inspectImageCmd(imageName).exec();
        } catch (NotFoundException e) {
            System.out.println("이미지가 없어서 다운로드 중...");
            try {
                dockerClient.pullImageCmd(os)
                        .withTag(version)
                        .exec(new PullImageResultCallback())
                        .awaitCompletion();
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Docker 이미지 다운로드 실패", ex);
            }
        }

        // ** 포트 바인딩 ** //
        //컨테이너 내부 포트
        ExposedPort containerPort = ExposedPort.tcp(22);

        //호스트 포트 자동 할당
        Ports portBindings = new Ports();

        //컨테이너 내부 포트와 호스트 내부 포트 연결
        portBindings.bind(containerPort, Ports.Binding.bindPort(0));

        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings);



        // ** 컨테이너 생성 및 포트 바인딩** //
        CreateContainerResponse containerResponse = dockerClient.createContainerCmd(imageName)
                .withCmd("tail", "-f", "/dev/null")
                .withExposedPorts(containerPort)
                .withHostConfig(hostConfig)
                .exec();




        //컨테이너 아이디 가져오기
        String containerId = containerResponse.getId();
        System.out.println("컨테이너 생성 완료: " + containerId);

        // 컨테이너 실행
        dockerClient.startContainerCmd(containerId).exec();
        System.out.println("컨테이너 실행 완료" + containerId);

        String hostPort = dockerClient.inspectContainerCmd(containerId)
                .exec()                                  // InspectContainerResponse 반환
                .getNetworkSettings()                    // 컨테이너의 네트워크 설정 가져오기
                .getPorts()                              // 네트워크 설정에서 Ports 객체 가져오기
                .getBindings()                           // Map<ExposedPort, Binding[]> 형태로 포트 바인딩 정보 가져오기
                .get(containerPort)[0]                   // 특정 ExposedPort (예: 22/tcp)에 대한 첫 번째 Binding 선택
                .getHostPortSpec();                      // 선택된 Binding에서 호스트에 할당된 포트 번호를 문자열로 반환


        // ** 데이터베이스에 저장 ** //
        String containerName = dockerClient.inspectContainerCmd(containerId).exec().getName();
        String status = dockerClient.inspectContainerCmd(containerId).exec().getState().getStatus();

        Container container = Container.builder()
                .containerId(containerId)
                .containerName(containerName)
                .os(os)
                .version(version)
                .createdAt(new Date())
                .status(status)
                .hostPort(hostPort)
                .madeBy(userEmail)
                .build();

        containerRepository.save(container);
        System.out.println("컨테이너 저장" + containerId);



        //컨테이너 아이디, 포트번호 return
        ContainerInfoDto containerInfoDto = ContainerInfoDto.builder()
                .containerId(containerId)
                .port(hostPort)
                .build();

        return containerInfoDto;

        //TODO : 예외 처리
    }


}
