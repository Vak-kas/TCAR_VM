package com.hanbat.dotcar.access;

import com.hanbat.dotcar.container.Container;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccessRepository extends JpaRepository<AccessAuthority, Long> {
    List<AccessAuthority> findByContainer(Container container);
    Optional<AccessAuthority> findByContainerAndUserEmail(Container container, String userEmail);


    // 현재 사용자가 접근할 수 있는 컨테이너 반환
    @Query("SELECT a.container FROM AccessAuthority a " +
            "JOIN a.container c " +
            "WHERE a.userEmail = :userEmail " +
            "AND c.status = 'running'")
    List<Container> findAccessibleRunningContainersByUserEmail(@Param("userEmail") String userEmail);
}
