package com.snjx.netty.websocket.server.config;

import com.snjx.netty.websocket.server.handler.MyWebSocketHandler;
import com.snjx.netty.websocket.server.model.SocketClientModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author by ernest
 * @version 1.0
 * @apiNote TODO
 * @date 12/22/20 5:54 下午
 */
@Configuration
public class WebSocketConfiguration {
    @Bean
    public HandlerMapping webSocketMapping() {
        final Map<String,WebSocketHandler> map = new HashMap<>(1);
        map.put("/ws", new MyWebSocketHandler());
        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
        mapping.setUrlMap(map);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

   /* @Bean
    public ConcurrentHashMap<String,SocketClientModel> senderMap() {
        return new ConcurrentHashMap<String,SocketClientModel>();
    }*/
}
