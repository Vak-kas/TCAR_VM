package com.hanbat.dotcar.access;

import com.hanbat.dotcar.access.domain.AccessAuthority;
import com.hanbat.dotcar.kubernetes.domain.Pod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccessRepository extends JpaRepository<AccessAuthority, Long> {
    List<AccessAuthority> findByPod(Pod pod);
    Optional<AccessAuthority> findByPodAndUserEmail(Pod pod, String userEmail);


    // 현재 사용자가 접근할 수 있는 컨테이너 반환
    @Query("SELECT a.pod FROM AccessAuthority a " +
            "JOIN a.pod c " +
            "WHERE a.userEmail = :userEmail " +
            "AND c.status = 'running'")
    List<Pod> findAccessibleRunningPodsByUserEmail(@Param("userEmail") String userEmail);
}
