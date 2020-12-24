package com.snjx.netty.websocket.server.model;

import com.alibaba.fastjson.JSON;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author by ernest
 * @version 1.0
 * @apiNote TODO
 * @date 12/24/20 10:19 上午
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class SocketClientMapModel {

    private volatile static SocketClientMapModel singleton=null;
    public SocketClientMapModel(){}
    public static SocketClientMapModel getSingleton() {
        if (singleton == null) {
            synchronized (SocketClientMapModel.class) {
                if (singleton == null) {
                    singleton = new SocketClientMapModel();
                }
            }
        }
        return singleton;
    }

    public  Map<String,SocketClientModel> clientMap = new ConcurrentHashMap<>();

}
