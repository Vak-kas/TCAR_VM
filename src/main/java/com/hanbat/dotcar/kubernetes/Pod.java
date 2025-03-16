package com.hanbat.dotcar.kubernetes;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class Pod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String podName; // pod 이름

    @Column(nullable = false, unique = true)
    private String podNamespace;

    @Column(nullable = false)
    private String os; //운영체제

    private String version; //버전

    @Temporal(TemporalType.DATE)
    private Date createdAt; //pod 생성 시간

    @Enumerated(EnumType.STRING)
    private PodStatus status; // pod 상태

    private String servicePort;  //외부 노출된 포트

    @Column(nullable = false)
    private String userEmail; //생성한 사람의 이메일

}

