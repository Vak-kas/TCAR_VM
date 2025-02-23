package com.hanbat.dotcar.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PreSignedUrlConfig {

    @Value("${pre-signed.url}")
    private String preSignedUrl;

    public String getPreSignedUrl() {
        return preSignedUrl;
    }
}
