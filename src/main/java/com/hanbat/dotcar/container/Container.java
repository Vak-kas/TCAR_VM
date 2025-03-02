package com.hanbat.dotcar.container;


import com.hanbat.dotcar.access.AccessAuthority;
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
public class Container {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String containerId; //컨테이너 아이디

    private String containerName; // 컨테이너 이름

    @Column(nullable = false)
    private String os; //운영체제

    private String version; //버전

    private Date createdAt; //컨테이너 생성 시간
    private String status; // 컨테이너 상태
    private String hostPort;  // 컨테이너가 매핑된 호스트 포트 번호

    private String madeBy; //생성한 사람의 이메일



    // 해당 컨테이너에 접근할 수 있는 사용자 목록(양방향)
    @OneToMany(mappedBy = "container", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AccessAuthority> accessAuthorityList;

}
