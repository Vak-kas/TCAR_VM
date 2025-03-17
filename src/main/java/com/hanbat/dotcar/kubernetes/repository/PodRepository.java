package com.hanbat.dotcar.kubernetes.repository;

import com.hanbat.dotcar.kubernetes.Pod;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PodRepository extends JpaRepository<Pod, Long> {
}
