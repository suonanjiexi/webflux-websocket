package com.snjx.netty.websocket.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

/**
 * @author by ernest
 * @version 1.0
 * @apiNote TODO
 * @date 12/24/20 2:22 下午
 */
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    String userId;
    String userName;
    String passWord;
    String groupId;
    String groupName;
}
