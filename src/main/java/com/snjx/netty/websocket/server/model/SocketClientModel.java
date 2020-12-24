package com.snjx.netty.websocket.server.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

/**
 * @author by ernest
 * @version 1.0
 * @apiNote TODO
 * @date 12/23/20 11:34 上午
 */
@Data
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SocketClientModel {

    FluxSink<WebSocketMessage> fluxSink;
    WebSocketSession session;

    public SocketClientModel(FluxSink<WebSocketMessage> fluxSink, WebSocketSession session) {
        this.fluxSink = fluxSink;
        this.session = session;
    }

    /**
     * 数据发送
     * @param data
     */
    public void sendData(String data) {
        fluxSink.next(session.textMessage(data));
    }
}
