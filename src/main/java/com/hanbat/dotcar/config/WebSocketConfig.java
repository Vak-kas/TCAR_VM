package com.hanbat.dotcar.config;


import com.hanbat.dotcar.config.handler.TerminalWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {
    private final TerminalWebSocketHandler terminalWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {//엔드포인트 등록
        registry.addHandler(terminalWebSocketHandler, "/ws/terminal")
//                .setAllowedOrigins("*") //CORS 정책 허용
                .setAllowedOriginPatterns("*");
//                .withSockJS(); //WebSocket을 지원하지 않는 브라우저에 대한 대체 솔루션 제공
    }
}
