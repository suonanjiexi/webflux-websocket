package com.snjx.netty.websocket.controller;

import com.snjx.netty.websocket.server.model.SocketClientMapModel;
import com.snjx.netty.websocket.server.model.SocketClientModel;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


/**
 * @author by ernest
 * @version 1.0
 * @apiNote TODO 单聊
 * @date 12/23/20 12:20 下午
 */
@RestController
@RequestMapping("/msg")
public class MsgController {

    private static final SocketClientMapModel clients=SocketClientMapModel.getSingleton();

    /**
     * 单聊
     * @param sessionId
     * @param msg
     * @return
     */
    @GetMapping("/single")
    public Mono<String> sendSingleMessage(@RequestParam String sessionId, @RequestParam String msg) {
        SocketClientModel sender = clients.getClientMap().get(sessionId);
        if (sender != null) {
            sender.sendData(msg);
            return Mono.just(String.format("Message '%s' sent to connection: %s.",msg, sessionId));
        } else {
            return Mono.just(String.format("Connection of id '%s' doesn't exist", sessionId));
        }
    }
    /**
     * 群聊
     * @param sessionId
     * @param msg
     * @return
     */
    @GetMapping("/group")
    public Mono<String> sendGroupMessage(@RequestParam String sessionId, @RequestParam String msg) {
        SocketClientModel sender = clients.getClientMap().get(sessionId);
        if (sender != null) {
            sender.sendData(msg);
            return Mono.just(String.format("Message '%s' sent to connection: %s.",msg, sessionId));
        } else {
            return Mono.just(String.format("Connection of id '%s' doesn't exist", sessionId));
        }
    }
    @GetMapping("/hello")
    public Mono<String> hello() {
        String path = "ChatRoom";
        return Mono.create(monoSink -> monoSink.success(path));
    }

}
