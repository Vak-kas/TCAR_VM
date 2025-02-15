package com.hanbat.dotcar.container;


import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.github.dockerjava.api.exception.NotFoundException;
import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Ports;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.transport.DockerHttpClient;
import com.github.dockerjava.zerodep.ZerodepDockerHttpClient;
import com.hanbat.dotcar.container.dto.ContainerInfoDto;
import com.hanbat.dotcar.container.dto.CreateContainerRequestDto;
import com.hanbat.dotcar.container.dto.DeleteContainerRequestDto;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.DataException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class ContainerService {
    private final ContainerRepository containerRepository;
    private final ValidateService validateService;
    private final ImageService imageService;
    private final DockerClient dockerClient;


    /****** 컨테이너 생성 ********/
    public ContainerInfoDto createContainer(CreateContainerRequestDto createContainerRequestDto){

        String userEmail = createContainerRequestDto.getUserEmail();

        //백엔드로부터 유저 등급 가져오기
        String role = validateService.getUserRole(userEmail);
        if(role == null) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "유저 정보 확인에 실패하였습니다.");
        }

//        // 생성 권한 화인
        if(!validateService.createContainerUserPermission(userEmail, role)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "컨테이너 생성 조건을 만족하지 못합니다.");
        }

        //Version이 비어있을 경우
        String os = createContainerRequestDto.getOs().toLowerCase(); //소문자만 취급하기에, 소문자로 바꾸기
        String version = (createContainerRequestDto.getVersion()) == null || createContainerRequestDto.getVersion().isEmpty()
                ? "latest" : createContainerRequestDto.getVersion();


        // 이미지 불러오기
        String imageName = imageService.getOrPullImage(os, version);


        // ** 포트 바인딩 설정** //
        ExposedPort containerPort = ExposedPort.tcp(22); //컨테이너 내부 포트
        Ports portBindings = new Ports(); //호스트 포트 자동 할당
        portBindings.bind(containerPort, Ports.Binding.bindPort(0));  //컨테이너 내부 포트와 호스트 내부 포트 연결
        HostConfig hostConfig = HostConfig.newHostConfig()
                .withPortBindings(portBindings);



        // ** 컨테이너 생성 및 포트 바인딩** //
        CreateContainerResponse containerResponse;
        try{
            containerResponse = dockerClient.createContainerCmd(imageName)
                    .withCmd("tail", "-f", "/dev/null") // 컨테이너 생성 후 프로세스 유지(바로 꺼지지 않게 하기 위한 장치)
                    .withExposedPorts(containerPort)
                    .withHostConfig(hostConfig)
                    .exec();
        } catch (DockerException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "도커 컨테이너 생성 실패");
        }



        //** 컨테이너 실행 및 프로세스 유지 ** //
        String containerId = containerResponse.getId();
        try{
            dockerClient.startContainerCmd(containerId).exec();
        } catch (DockerException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "도커 컨테이너 실행 실패");
        }


        // ** 컨테이너 상태 및 포트 바인딩 조회 ** //
        String containerName;
        String status;
        String hostPort;

        try{

        containerName = dockerClient.inspectContainerCmd(containerId).exec().getName();
        status = dockerClient.inspectContainerCmd(containerId).exec().getState().getStatus();
        hostPort = dockerClient.inspectContainerCmd(containerId)
                .exec()                                  // InspectContainerResponse 반환
                .getNetworkSettings()                    // 컨테이너의 네트워크 설정 가져오기
                .getPorts()                              // 네트워크 설정에서 Ports 객체 가져오기
                .getBindings()                           // Map<ExposedPort, Binding[]> 형태로 포트 바인딩 정보 가져오기
                .get(containerPort)[0]                   // 특정 ExposedPort (예: 22/tcp)에 대한 첫 번째 Binding 선택
                .getHostPortSpec();                      // 선택된 Binding에서 호스트에 할당된 포트 번호를 문자열로 반환
        } catch (DockerException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "생성된 컨테이너 정보 조회 실패");
        }



        // ** 데이터베이스에 저장 ** //
        try{
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
        } catch (DataException e){
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "데이터베이스 저장 실패");
        }



        // ** 컨테이너 아이디, 포트번호 return ** //
        ContainerInfoDto containerInfoDto = ContainerInfoDto.builder()
                .containerId(containerId)
                .port(hostPort)
                .build();

        return containerInfoDto;

    }


    /****** 컨테이너 삭제 ********/
    public String deleteContainer(DeleteContainerRequestDto deleteContainerRequestDto){

        //TODO : ADMIN일 경우에는?
        String userEmail = deleteContainerRequestDto.getUserEmail();
        String containerId = deleteContainerRequestDto.getContainerId();


        Optional<Container> findContainer = containerRepository.findByContainerId(containerId);

        //컨테이너가 실제로 존재하는지 확인
        if(findContainer.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 컨테이너가 존재하지 않습니다.");
        }
        Container container = findContainer.get();


        //삭제 권한 여부 확인
        if(!(container.getMadeBy()).equals(userEmail)){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 컨테이너를 삭제할 권한이 없습니다.");
        }


        // Docker 컨테이너 상태 확인
        String dbStatus = container.getStatus();
        String dockerStatus;
        try{
            dockerStatus = dockerClient.inspectContainerCmd(containerId).exec().getState().getStatus();
        } catch (NotFoundException e){
            container.setStatus("deleted");
            containerRepository.save(container);
            return "존재하지 않은 컨테이너 입니다.";
        }


        // 컨테이너 종료
        if("running".equals(dockerStatus)){
            dockerClient.stopContainerCmd(containerId).exec();
            container.setStatus("exited");
            containerRepository.save(container);
        }
        else {
            container.setStatus("exited");
            containerRepository.save(container);
        }


        // 컨테이너 삭제
        dockerClient.removeContainerCmd(containerId).exec();
        container.setStatus("deleted");
        containerRepository.save(container);





        return "삭제가 완료되었습니다.";
    }


}
