package com.hanbat.dotcar.kubernetes.repository;

import com.hanbat.dotcar.kubernetes.domain.Pod;
import com.hanbat.dotcar.kubernetes.domain.PodStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PodRepository extends JpaRepository<Pod, Long> {
    int countByUserEmailAndStatus(String userEmail, PodStatus podStatus);
}
