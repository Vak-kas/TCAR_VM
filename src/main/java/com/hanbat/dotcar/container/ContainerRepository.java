package com.hanbat.dotcar.container;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContainerRepository extends JpaRepository<Container, Long> {
    Optional<Container> findByContainerId(String ContainerId);
    Optional<Container> findByMadeBy(String MadeBy);
}
