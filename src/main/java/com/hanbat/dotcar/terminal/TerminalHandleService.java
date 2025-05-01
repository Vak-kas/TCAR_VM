package com.hanbat.dotcar.terminal;

import io.kubernetes.client.custom.IOTrio;
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
public class TerminalHandleService {
    private final Map<String, OutputStream> sessionOutputStreams = new ConcurrentHashMap<>();

    public void writeToPod(String sessionId, String message) {
        System.out.println("sessionOutputStreams 키들: " + sessionOutputStreams.keySet());
        try {
            System.out.println("🔁 writeToPod: " + message);
            OutputStream podIn = sessionOutputStreams.get(sessionId);
            if (podIn != null) {
                podIn.write(message.getBytes(StandardCharsets.UTF_8));
                podIn.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

                    // Pod → WebSocket
                    Thread outputThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                byte[] buffer = new byte[1024];
                                int bytesRead;
                                while ((bytesRead = podOut.read(buffer)) != -1) {
                                    if (session.isOpen()) {
                                        String output = new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
                                        System.out.println(buffer);
                                        session.sendMessage(new TextMessage(output));
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    outputThread.start();

                    session.setTextMessageSizeLimit(4096);
                    session.setBinaryMessageSizeLimit(4096);

                    session.sendMessage(new TextMessage("[+] 터미널 접속됨\r\n"));

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void removeSession(String sessionId) {
        sessionOutputStreams.remove(sessionId);
    }

    public BiConsumer<Integer, IOTrio> onCloseHandler(WebSocketSession session){
        return new BiConsumer<Integer, IOTrio>() {
            @Override
            public void accept(Integer code, IOTrio ioTrio) {
                try{
                    session.sendMessage(new TextMessage("[+] 연결 종료됨. 코드: " + code));
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