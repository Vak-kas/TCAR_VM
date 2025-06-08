package com.hanbat.dotcar.kubernetes.domain;

import com.hanbat.dotcar.access.domain.AccessAuthority;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;

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

    @Column(nullable=true)
    private String calledName; //사용자가 지정한 Pod 이름

    @Column(nullable = false)
    private String podName; // pod 이름

    @Column(nullable = false)
    private String podNamespace;

    @Column(nullable = false)
    private String os; //운영체제

    private String version; //버전

    @Temporal(TemporalType.DATE)
    private Date createdAt; //pod 생성 시간

    @Enumerated(EnumType.STRING)
    private PodStatus status; // pod 상태

    private String ingress;  //외부 노출된 포트

    @Column(nullable = false)
    private String userEmail; //생성한 사람의 이메일


    // 해당 파드 접근할 수 있는 사용자 목록(양방향)
    @OneToMany(mappedBy = "pod", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccessAuthority> accessAuthorityList;

}

