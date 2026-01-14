package com.xyz.exception;

/**
 * 账号被封禁异常
 */
public class AccountBannedException extends BaseException {
    public AccountBannedException(String msg) {
        super(msg);
    }
}
