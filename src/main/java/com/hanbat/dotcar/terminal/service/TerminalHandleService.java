package com.hanbat.dotcar.terminal.service;

import io.kubernetes.client.custom.IOTrio;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class TerminalHandleService {
    @Getter
    private final ConcurrentHashMap<String, Long> lastActivityMap = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, OutputStream> sessionOutputStreams = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    private final MessageService messageService;

    private final String START_CONNECT = "터미널에 연결되었습니다.\r\n";
    private final String END_CONNECT = "연결이 종료되었습니다.\r\n";


    public void writeToPod(String sessionId, String message) {
        try {
            OutputStream podIn = sessionOutputStreams.get(sessionId);
            if (podIn != null) {
                podIn.write(message.getBytes(StandardCharsets.UTF_8));
                lastActivityMap.put(sessionId, System.currentTimeMillis());
                podIn.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void handlePodToWebSocketStreaming(InputStream podOut, WebSocketSession session, MessageService messageService) {
        Thread outputThread = new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = podOut.read(buffer)) != -1) {
                    if (session.isOpen()) {
                        String output = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                        String json = messageService.buildInputMessage(output);
                        session.sendMessage(new TextMessage(json));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        outputThread.start();
    }

    public Consumer<IOTrio> onOpenHandler(WebSocketSession session) {
        return new Consumer<IOTrio>() {
            @Override
            public void accept(IOTrio ioTrio) {
                try {
                    InputStream podOut = ioTrio.getStdout();
                    OutputStream podIn = ioTrio.getStdin();

                    // 여기서 sessionId -> podIn 매핑 저장
                    sessionOutputStreams.put(session.getId(), podIn);
                    lastActivityMap.put(session.getId(), System.currentTimeMillis());
                    sessionMap.put(session.getId(), session);

                    // Pod → WebSocket
                    handlePodToWebSocketStreaming(podOut, session, messageService);

                    session.setTextMessageSizeLimit(4096);
                    session.setBinaryMessageSizeLimit(4096);

                    String json = messageService.buildNoticeMessage(START_CONNECT);
                    session.sendMessage(new TextMessage(json));


                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void removeSession(String sessionId) {
        sessionOutputStreams.remove(sessionId);
        lastActivityMap.remove(sessionId);
        sessionMap.remove(sessionId);
    }

    public BiConsumer<Integer, IOTrio> onCloseHandler(WebSocketSession session){
        return new BiConsumer<Integer, IOTrio>() {
            @Override
            public void accept(Integer code, IOTrio ioTrio) {
                try{
                    String json = messageService.buildNoticeMessage(END_CONNECT);
                    session.sendMessage(new TextMessage(json));
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    removeSession(session.getId()); // 종료 시 정리
                }
            }
        };
    }

    public BiConsumer<Throwable, IOTrio> onErrorHandler(WebSocketSession session) {
        return new BiConsumer<Throwable, IOTrio>() {
            @Override
            public void accept(Throwable t, IOTrio ioTrio) {
                try {
                    session.sendMessage(new TextMessage("[-] 에러 발생: " + t.getMessage()));
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    removeSession(session.getId()); // 에러 시 정리
                }
            }
        };
    }
}