package com.hanbat.dotcar.access.service;

import com.hanbat.dotcar.config.PreSignedUrlConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.util.Map;

@Service
public class ValidateService {
    private final SecretKey SECRET_KEY;

    //초기 세팅
    public ValidateService(PreSignedUrlConfig preSignedUrlConfig){
        byte[] keyBytes = Decoders.BASE64.decode(preSignedUrlConfig.getSecretKey());
        this.SECRET_KEY = Keys.hmacShaKeyFor(keyBytes);
    }


    //*** TOKEN 검증 및 payload return
    public Map<String, String> getClaimsFromToken(String token){
        try{
            Claims claims = Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Map.of(
                    "userEmail", claims.getSubject(),
                    "podName", claims.get("podName", String.class),
                    "podNamespace", claims.get("podNamespace", String.class),
                    "ingress", claims.get("ingress", String.class)
            );
        } catch (ExpiredJwtException e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다.");
        } catch (MalformedJwtException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰 형식입니다.");
        } catch (Exception e){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰 검증에 실패하였습니다.");
        }

    }


}
