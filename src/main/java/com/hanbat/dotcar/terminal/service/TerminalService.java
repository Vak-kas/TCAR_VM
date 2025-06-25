package com.hanbat.dotcar.terminal.service;

import io.kubernetes.client.Exec;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.WebSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TerminalService {
    private final TerminalHandleService terminalHandleService;
    private final MessageService messageService;


    public Map<String, String> getQueryParam(String query){
        Map<String, String> queryParams = new HashMap<>();

        if(query == null || query.isEmpty()){
            return queryParams;
        }

        for(String param : query.split("&")){
            String[] pair = param.split("=", 2);
            String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
            String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
            queryParams.put(key, value);
        }
        return queryParams;
    }

    public void connectToPodTerminal(String podName, String podNamespace, WebSocketSession session){
        Exec exec = new Exec();
        String[] commands = {"/bin/bash", "-c", "stty > /dev/null 2>&1 ; bash"};
//        String[] commands = {"/bin/bash"};

        try {
            exec.exec(
                    podNamespace,
                    podName,
                    terminalHandleService.onOpenHandler(session),
                    terminalHandleService.onCloseHandler(session),
                    terminalHandleService.onErrorHandler(session),
                    null,
                    true,
                    commands
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "서버 접속 오류");
        }
    }

    @Scheduled(fixedDelay = 60_000)
    public void cleanInactivateSessions(){
        long now = System.currentTimeMillis(); //현재시간
        System.out.println("현재 시간 : " + now + ", 스케쥴링 시작");

        int numTimeout = 3;
        int numWarnBefore = 1;




        long timeout = numTimeout * 60 * 1000; //세션 종료 시간
        long warnBefore = numWarnBefore * 60 * 1000; // 경고
        Map<String, Long> lastActivityMap = terminalHandleService.getLastActivityMap();
        Map<String, WebSocketSession> sessionMap = terminalHandleService.getSessionMap();


        for (String sessionId : lastActivityMap.keySet()) {
            Long lastActivity = lastActivityMap.get(sessionId);
            if (lastActivity == null) continue; // null 체크
            long elapsed = now - lastActivity;

            //마지막 비활동 시간이 numWarnBefore 시간을 넘었을 때 경고 메시지
            if (elapsed > (timeout - warnBefore) && elapsed < timeout) {
                WebSocketSession session = sessionMap.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        String json = messageService.buildWarningMessage("장시간 활동이 감지되지 않아" + numWarnBefore+ "분 뒤 터미널이 종료됩니다");
                        session.sendMessage(new TextMessage(json));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            //마지막 비활동 시간이 numTimeout 시간을 넘었을 때
            else if (elapsed > timeout) {
                terminalHandleService.writeToPod(sessionId, "exit\n");
                terminalHandleService.removeSession(sessionId);
                System.out.println("[AutoClose] 타임아웃 세션 정리: " + sessionId);
            }


        }
    }



}
