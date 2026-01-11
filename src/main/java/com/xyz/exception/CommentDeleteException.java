package com.xyz.exception;

/**
 * 评论删除异常
 */
public class CommentDeleteException extends BaseException {
    
    public CommentDeleteException() {
    }
    
    public CommentDeleteException(String msg) {
        super(msg);
    }
}
