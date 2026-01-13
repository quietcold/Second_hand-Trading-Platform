package com.xyz.exception;

/**
 * 消息撤回时间过期异常
 */
public class ChatRecallTimeExpiredException extends BaseException {
    
    public ChatRecallTimeExpiredException() {
        super();
    }
    
    public ChatRecallTimeExpiredException(String msg) {
        super(msg);
    }
}
