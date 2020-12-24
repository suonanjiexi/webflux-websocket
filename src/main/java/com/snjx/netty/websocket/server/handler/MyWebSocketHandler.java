package com.snjx.netty.websocket.server.handler;

import com.alibaba.fastjson.JSON;
import com.snjx.netty.websocket.server.model.SocketClientMapModel;
import com.snjx.netty.websocket.server.model.SocketClientModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * @author by ernest
 * @version 1.0
 * @apiNote TODO  https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-websocket
 * @date 12/22/20 5:46 下午
 */
@Slf4j
public class MyWebSocketHandler implements WebSocketHandler {

    private static final SocketClientMapModel clients=SocketClientMapModel.getSingleton();

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
        //log.info("session 信息: {}",JSON.toJSONString(session));
        log.info("sessionId 信息: {}",session.getId());
        String sessionId=session.getId();
        //向客户端发送数据流，当数据流结束时，往客户端的写操作也会随之结束，此时返回的 Mono<Void> 会发出一个完成信号。
        Mono<Void> output = session.send(Flux.create(sink -> clients.getClientMap().put(sessionId, new SocketClientModel(sink,session))));
        //接收来自客户端的数据流，当连接关闭时数据流结束。
        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText).map(msg ->  msg)
                .doOnNext(msg->sendBroadcast(msg)).then();
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


    /**
     * 广播
     * 遍历所有session
     * @param message
     */
    public void  sendBroadcast(String message) {
        clients.getClientMap().forEach((sessionId,client) ->
                client.sendData("【服务器广播】: " + message)
        );
    }
}