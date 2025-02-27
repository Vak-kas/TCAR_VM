package com.hanbat.dotcar.container;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final DockerClient dockerClient;

    public String getOrPullImage(String os, String version){
        String imageName = "romanseo/" + os + ":" + version;
        //이미지 존재 여부 확인하고, 없으면 다운로드
        try {
            dockerClient.inspectImageCmd(imageName).exec();
            return imageName;
        } catch (NotFoundException e) {
            System.out.println("이미지가 없어서 다운로드 중...");
            try {
                dockerClient.pullImageCmd(os)
                        .withTag(version)
                        .exec(new PullImageResultCallback())
                        .awaitCompletion();
                return imageName;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); // 현재 스레드 인터럽트 상태로 유지 -> 없으면 인터럽트가 발생했다는 인지 못하고 비정상적인 동작 발생 가능성
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Docker 이미지 다운로드 중 인터럽트 발생");
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Docker 이미지 다운로드 실패");
            }
        }
    }
}
