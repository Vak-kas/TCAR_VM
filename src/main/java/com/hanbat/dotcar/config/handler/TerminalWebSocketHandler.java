package com.hanbat.dotcar.config.handler;

import com.hanbat.dotcar.access.service.AccessService;
import com.hanbat.dotcar.terminal.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class TerminalWebSocketHandler extends TextWebSocketHandler {
    private final TerminalService terminalService;
    private final AccessService accessService;

    //메시지 처리
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            System.out.println("서버에서 보내는 응답: " + payload);
            session.sendMessage(new TextMessage("서버에서 보내는 응답: " + payload));
        } catch (IOException e) {
            System.err.println("Broken pipe: 클라이언트 연결이 끊겼습니다.");
        }
    }

    //연결 설정 후
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        String query = uri.getQuery();

        Map<String, String> params = terminalService.getQueryParam(query);
        String token = params.get("token");
        String podName = params.get("podName");
        String podNamespace = params.get("podNamespace");

        try{
            accessService.validatePresignedUrl(token);
            terminalService.connectToPodTerminal(podName, podNamespace, session);
        } catch (Exception e) {
            session.close(CloseStatus.BAD_DATA);
        }

    }

    //연결 종료 후
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("[+] afterConnectionClosed - Session: " + session.getId() + ", CloseStatus: " + status);
    }
}
