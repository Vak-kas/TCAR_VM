package com.hanbat.dotcar.access;

import com.hanbat.dotcar.container.Container;
import jakarta.persistence.*;
import lombok.*;

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


    // 다대일 관계 -> 하나의 컨테이너에 여러 명이 접근 가능
    @ManyToOne
    @JoinColumn(name="container_id")
    private Container container;

}
