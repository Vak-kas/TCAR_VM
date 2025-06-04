package com.hanbat.dotcar.terminal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanbat.dotcar.terminal.domain.MessageType;
import com.hanbat.dotcar.terminal.dto.TerminalMessageDto;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
    private final ObjectMapper objectMapper = new ObjectMapper();


    public String buildInputMessage(String msg){
        TerminalMessageDto terminalMessageDto = TerminalMessageDto.builder()
                .type(MessageType.INPUT)
                .message(msg)
                .currentTime(System.currentTimeMillis())
                .build();
        return toJson(terminalMessageDto);
    }

    public String buildNoticeMessage(String msg){
        TerminalMessageDto terminalMessageDto = TerminalMessageDto.builder()
                .type(MessageType.NOTICE)
                .message(msg)
                .currentTime(System.currentTimeMillis())
                .build();
        return toJson(terminalMessageDto);
    }

    public String buildWarningMessage(String msg){
        TerminalMessageDto terminalMessageDto = TerminalMessageDto.builder()
                .type(MessageType.WARNING)
                .message(msg)
                .currentTime(System.currentTimeMillis())
                .build();
        return toJson(terminalMessageDto);
    }



    public String toJson(TerminalMessageDto terminalMessageDto){
        try {
            return objectMapper.writeValueAsString(terminalMessageDto);
        } catch (JsonProcessingException e) {
            return "{\"type\":\"ERROR\",\"message\":\"메시지 변환 오류\",\"currentTime\":null}";
        }
    }



}
