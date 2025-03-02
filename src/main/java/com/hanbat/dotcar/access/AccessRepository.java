package com.hanbat.dotcar.access;

import com.hanbat.dotcar.container.Container;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccessRepository extends JpaRepository<AccessAuthority, Long> {
    List<AccessAuthority> findByContainer(Container container);
    Optional<AccessAuthority> findByContainerAndUserEmail(Container container, String userEmail);
}
