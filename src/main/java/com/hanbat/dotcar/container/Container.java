package com.hanbat.dotcar.container;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Builder
public class Container {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String containerID; //컨테이너 아이디

    private String containerName; // 컨테이너 이름

    @Column(nullable = false)
    private String OS; //운영체제

    private String version; //버전

    private Date createdAt; //컨테이너 생성 시간
    private String status; // 컨테이너 상태
    private int hostPort;  // 컨테이너가 매핑된 호스트 포트 번호

    private String madeBy; //생성한 사람의 이메일



}
