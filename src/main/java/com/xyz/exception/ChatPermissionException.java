package com.xyz.exception;

/**
 * 聊天权限异常
 */
public class ChatPermissionException extends BaseException {
    
    public ChatPermissionException() {
        super();
    }
    
    public ChatPermissionException(String msg) {
        super(msg);
    }
}
