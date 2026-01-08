package com.xyz.exception;

/**
 * 商品租借中无法操作异常
 */
public class GoodsInRentException extends BaseException {
    public GoodsInRentException() {
    }

    public GoodsInRentException(String message) {
        super(message);
    }
}
