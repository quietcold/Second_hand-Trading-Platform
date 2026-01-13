package com.xyz.exception;

/**
 * 会话不存在异常
 */
public class ChatSessionNotFoundException extends BaseException {
    
    public ChatSessionNotFoundException() {
        super();
    }
    
    public ChatSessionNotFoundException(String msg) {
        super(msg);
    }
}
