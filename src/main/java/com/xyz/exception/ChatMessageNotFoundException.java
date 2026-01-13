package com.xyz.exception;

/**
 * 消息不存在异常
 */
public class ChatMessageNotFoundException extends BaseException {
    
    public ChatMessageNotFoundException() {
        super();
    }
    
    public ChatMessageNotFoundException(String msg) {
        super(msg);
    }
}
