package com.hanbat.dotcar.kubernetes;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PodRepository extends JpaRepository<Pod, Long> {
}
