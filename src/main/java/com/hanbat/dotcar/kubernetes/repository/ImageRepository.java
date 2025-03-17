package com.hanbat.dotcar.kubernetes.repository;

import com.hanbat.dotcar.kubernetes.Image;
import com.hanbat.dotcar.kubernetes.Pod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByImageName(String imageName);

}
