package com.hanbat.dotcar.config.handler;

import com.hanbat.dotcar.access.service.AccessService;
import com.hanbat.dotcar.terminal.service.TerminalHandleService;
import com.hanbat.dotcar.terminal.service.TerminalService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class TerminalWebSocketHandler extends TextWebSocketHandler {
    private final TerminalService terminalService;
    private final AccessService accessService;
    private final TerminalHandleService terminalHandleService;

//    private final Map<String, OutputStream> sessionOutputStreams = new ConcurrentHashMap<>();

    //메시지 처리
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        terminalHandleService.writeToPod(session.getId(), message.getPayload());
    }

    //연결 설정 후
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("[+] WebSocket 연결됨: " + session.getId());
        URI uri = session.getUri();
        String query = uri.getQuery();
//        System.out.println(query);


        Map<String, String> params = terminalService.getQueryParam(query);
        String token = params.get("token");
        String podName = params.get("podName");
        String podNamespace = params.get("podNamespace");
//        System.out.println("token: " + token);
//        System.out.println("podName: " + podName);
//        System.out.println("namespace: " + podNamespace);
//        System.out.println("params: " + params);

        try{
//            System.out.println("2차 검증");
            accessService.validatePresignedUrl(token);

//            System.out.println("2차 검증 통과");
            terminalService.connectToPodTerminal(podName, podNamespace, session);
        } catch (Exception e) {
            session.close(CloseStatus.BAD_DATA);
        }

    }

    //연결 종료 후
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("[+] afterConnectionClosed - Session: " + session.getId() + ", CloseStatus: " + status);
        try{
            terminalHandleService.writeToPod(session.getId(), "exit\n");
        } catch (Exception ignore) {
            //고아 프로세스는 주기적으로 관리
        }
    }
}
