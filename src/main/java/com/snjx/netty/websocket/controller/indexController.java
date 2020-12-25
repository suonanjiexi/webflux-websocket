package com.snjx.netty.websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;


/**
 * @author by ernest
 * @version 1.0
 * @apiNote TODO 单聊
 * @date 12/23/20 12:20 下午
 */
@Controller
@RequestMapping("/index")
public class indexController {

    @GetMapping("/chatRoom")
    public Mono<String> hello() {
        String path = "ChatRoom";
        return Mono.create(monoSink -> monoSink.success(path));
    }

}
