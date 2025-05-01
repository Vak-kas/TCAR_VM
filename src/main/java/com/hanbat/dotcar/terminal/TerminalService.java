package com.hanbat.dotcar.terminal;

import io.kubernetes.client.Exec;
import io.kubernetes.client.custom.IOTrio;
import io.kubernetes.client.openapi.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class TerminalService {
    private final TerminalHandleService terminalHandleService;

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
        String[] commands = {"/bin/bash", "-c", "stty raw -onlcr ; bash"};
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


}
