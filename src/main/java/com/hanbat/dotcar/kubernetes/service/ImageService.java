package com.hanbat.dotcar.kubernetes.service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.PullImageResultCallback;
import com.github.dockerjava.api.exception.NotFoundException;
import com.hanbat.dotcar.kubernetes.domain.Image;
import com.hanbat.dotcar.kubernetes.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImageService {
    //TODO: 이미지 다운 실패시 디비에 저장되는 문제 해결
    private static final String DEFAULT_IMAGE_REPOSITORY = "";

    private final ImageRepository imageRepository;
    private final DockerClient dockerClient;


    public String getImage(String os, String version) {
        if (version == null || version.isEmpty()){
            version = "latest";
        }

        String imageName = DEFAULT_IMAGE_REPOSITORY + os + ":" + version;


        //과거 사용 내역 확인
        if(isImageHistory(imageName)){
            return imageName;
        }

        //이미지 불러오기
        if(getOrPullImage(imageName)){
            Image image = Image.builder()
                    .imageName(imageName)
                    .build();
            imageRepository.save(image);
            return imageName;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,  os + ":" + version + " 이미지를 찾을 수 없습니다.");
    }


    //*** 과거 사용 내역 확인 ***//
    private boolean isImageHistory(String imageName){
        Optional<Image> image = imageRepository.findByImageName(imageName);
        if(image.isPresent()){
            return true;
        }
        return false;
    }


    //*** 이미지 불러오기 ***//
    private boolean getOrPullImage(String imageName) {
        //이미지 존재 여부 확인하고, 없으면 다운로드
        try {
            dockerClient.inspectImageCmd(imageName).exec();
            return true;
        } catch (NotFoundException e) {
            System.out.println("이미지가 없어서 다운로드 중...");
            try {
                dockerClient.pullImageCmd(imageName)
//                        .withTag(version)
                        .exec(new PullImageResultCallback())
                        .awaitCompletion();
                return true;
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt(); // 현재 스레드 인터럽트 상태로 유지 -> 없으면 인터럽트가 발생했다는 인지 못하고 비정상적인 동작 발생 가능성
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Docker 이미지 다운로드 중 인터럽트 발생");
            } catch (Exception ex) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Docker 이미지 다운로드 실패");
            }
        }
    }
}
