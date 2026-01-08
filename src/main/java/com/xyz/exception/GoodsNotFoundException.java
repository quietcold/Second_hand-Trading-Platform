package com.xyz.exception;

/**
 * 商品不存在或无权操作异常
 */
public class GoodsNotFoundException extends BaseException {
    public GoodsNotFoundException() {
    }

    public GoodsNotFoundException(String message) {
        super(message);
    }
}
