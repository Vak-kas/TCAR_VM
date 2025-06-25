package com.hanbat.dotcar.kubernetes.repository;

import com.hanbat.dotcar.kubernetes.domain.Pod;
import com.hanbat.dotcar.kubernetes.domain.PodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PodRepository extends JpaRepository<Pod, Long> {
    int countByUserEmailAndStatus(String userEmail, PodStatus podStatus);
    Optional<Pod> findByPodNameAndPodNamespace(String podName, String podNamespace);
    void deleteByPodNameAndPodNamespace(String podName, String podNamespace);
}
