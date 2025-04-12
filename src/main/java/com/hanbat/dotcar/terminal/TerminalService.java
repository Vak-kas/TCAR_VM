package com.hanbat.dotcar.terminal;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class TerminalService {

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

    }



}
