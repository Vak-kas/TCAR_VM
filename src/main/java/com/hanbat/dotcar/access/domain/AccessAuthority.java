package com.hanbat.dotcar.access.domain;

import com.hanbat.dotcar.kubernetes.domain.Pod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder
public class AccessAuthority {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;


    // 다대일 관계 -> 하나의 파드에 여러 명이 접근 가능
    @ManyToOne
    @JoinColumn(name="pod_id")
    private Pod pod;

}
