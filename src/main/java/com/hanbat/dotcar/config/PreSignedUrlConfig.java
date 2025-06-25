package com.hanbat.dotcar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PreSignedUrlConfig {

    @Value("${pre-signed.url}")  // pre_signed_url 값을 가져옴 (JWT 검증 키)
    private String SECRET_KEY;

    public String getSecretKey() {
        return SECRET_KEY;
    }
}