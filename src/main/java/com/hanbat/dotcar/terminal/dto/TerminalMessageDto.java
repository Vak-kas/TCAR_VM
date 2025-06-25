package com.hanbat.dotcar.terminal.dto;


import com.hanbat.dotcar.terminal.domain.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TerminalMessageDto {
    private MessageType type;
    private String message;
    private long currentTime;
}
