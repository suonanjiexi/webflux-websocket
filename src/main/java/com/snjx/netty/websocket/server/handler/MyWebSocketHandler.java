package com.snjx.netty.websocket.server.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.snjx.netty.websocket.server.model.SocketClientMapModel;
import com.snjx.netty.websocket.server.model.SocketClientModel;
import io.netty.handler.codec.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author by ernest
 * @version 1.0
 * @apiNote TODO  https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-websocket
 * @date 12/22/20 5:46 下午
 */
@Slf4j
public class MyWebSocketHandler implements WebSocketHandler, CorsConfigurationSource {


    public static ConcurrentHashMap<String,Map<String,SocketClientModel>> roomCacheMap = new ConcurrentHashMap<>();

    /***
     * 接受和返回的handle方法 指示会话的应用程序处理何时完成
     * 通过两个流处理会话，一个流用于入站消息，一个流用于出站消息
     * Flux<WebSocketMessage> receive() 提供对入站消息流的访问，并在关闭连接时完成。
     * Mono<Void> send(Publisher<WebSocketMessage>)  获取传出消息的源，编写消息，并Mono<Void>在源完成并完成写入时返回完成的消息。
     * @param session
     * @return
     */
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        //获取握手信息
        HandshakeInfo handshakeInfo = session.getHandshakeInfo();
        //获取远程地址
        InetSocketAddress remoteAddress = handshakeInfo.getRemoteAddress();
        //获取参数
        String params = handshakeInfo.getUri().getQuery();
        //解析参数
        Map<String, String> paramMap = getQueryMap(params);

        String roomName = paramMap.get("roomName");
        String userId = paramMap.get("userId");

        String sessionId=session.getId();
        //向客户端发送数据流，当数据流结束时，往客户端的写操作也会随之结束，此时返回的 Mono<Void> 会发出一个完成信号。
        Mono<Void> output = session.send(Flux.create(sink -> handleClient(roomName,userId, new SocketClientModel(sink,session))));
        //接收来自客户端的数据流，当连接关闭时数据流结束。
        Mono<Void> input = session.receive()
                .doOnSubscribe(conn -> {
                    log.info("建立连接：{}，用户ip：{}，房间号：{}，用户：{}", session.getId(),remoteAddress.getHostName(), roomName, userId);
                })
                .map(WebSocketMessage::getPayloadAsText).map(message ->  message)
                .doOnNext(message->{
                    sendBroadcast(roomName,userId,message);
                })
                .doOnComplete(() -> {
                    log.info("关闭连接：{}", session.getId());
                    exitRoom(session, roomName, userId);
                }).doOnCancel(() -> {
                    log.info("关闭连接：{}", session.getId());
                    exitRoom(session, roomName, userId);
                }).then();
        /**
         * Mono.zip() 会将多个 Mono 合并为一个新的 Mono，
         * 任何一个 Mono 产生 error 或 complete 都会导致合并后的 Mono
         * 也随之产生 error 或 complete，此时其它的 Mono 则会被执行取消操作。
         */
        return  Mono.zip(input,output).then();
       /* return session.receive()
                .doOnSubscribe(s -> {
                    log.info("doOnSubscribe->发起订阅，连接成功");
                })
                .doOnTerminate(() -> {
                    log.info("doOnTerminate->完成一次数据的传输，或者出错都会调用");
                })
                .doOnComplete(() -> {
                    log.info("doOnComplete->会话完成，终止会话,sessionId:{}", session.getId());
                })
                .doOnCancel(() -> {
                    log.info("doOnCancel->取消，终止会话,sessionId:{}", session.getId());
                })
                .doOnError(e -> {
                    e.printStackTrace();
                    log.info("doOnError->遇到错误，终止会话，错误信息：{},sessionId:{}", e, session.getId());
                })
                .onErrorResume(e -> Mono.error(e))
                .doOnNext(msg -> {
                        session.send(Flux.just(session.textMessage("哈哈哈"))).subscribe();
                })
                .then();*/
    }

    private void handleClient(String roomName, String userId,SocketClientModel client) {
        if (!roomCacheMap.containsKey(roomName)) {
            log.info("用户：{}，创建群聊：【{}】", userId, roomName);
            Map<String,SocketClientModel> socketClientCacheMap = new HashMap<>();
            socketClientCacheMap.put(userId,client);
            roomCacheMap.put(roomName, socketClientCacheMap);
        } else {
            Map<String,SocketClientModel> socketClientCacheMap = roomCacheMap.get(roomName);
            if (!socketClientCacheMap.containsKey(userId)) {
                log.info("用户：{}，进入群聊：{}", userId, roomName);
                sendBroadcast(roomName,userId, "进入群聊");
                socketClientCacheMap.put(userId, client);
            }
        }
    }

    /**
     * 退出群聊
      * @param session
     * @param roomName
     * @param userId
     */
    private void exitRoom(WebSocketSession session, String roomName, String userId) {
        session.close().toProcessor().then();
        sendBroadcast(roomName,userId, "退出群聊");
        removeUser(roomName, userId);
    }

    /**
     * 发送消息给除了自己的所有用户
     * @param roomName
     * @param userId
     * @param message
     */
    public static void sendBroadcast(String roomName, String userId, String message) {
        Map<String,SocketClientModel> clients = roomCacheMap.get(roomName);
        clients.forEach((user,client) -> {
            if (!userId.equals(user)) {
                log.info("用户：{} 发送消息：{}",userId,message);
                client.sendData(message);
            }
        });
    }

    /**
     * 移除用户
     * @param roomName
     * @param userId
     */
    private void removeUser(String roomName, String userId) {
        log.info("用户：{}，退出群聊：【{}】", userId, roomName);
        Map<String,SocketClientModel> socketClientCacheMap = roomCacheMap.get(roomName);
        socketClientCacheMap.remove(userId);
        if (socketClientCacheMap.isEmpty()) {
            log.info("房间：【{}】没人了，解散群聊", roomName);
            roomCacheMap.remove(roomName);
        }
    }

    /**
     * 跨域处理
     * @param serverWebExchange
     * @return
     */
    @Override
    public CorsConfiguration getCorsConfiguration(ServerWebExchange serverWebExchange) {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        return configuration;
    }

    //用于获取url参数
    private Map<String, String> getQueryMap(String queryStr) {
        Map<String, String> queryMap = new HashMap<>();
        if (!StringUtils.isEmpty(queryStr)) {
            String[] queryParam = queryStr.split("&");
            Arrays.stream(queryParam).forEach(s -> {
                String[] kv = s.split("=", 2);
                String value = kv.length == 2 ? kv[1] : "";
                queryMap.put(kv[0], value);
            });
        }
        return queryMap;
    }
}