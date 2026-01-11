package com.xyz.constant;


public  class MessageConstant {
    public static final String Register_SUCCESS = "注册成功";
    public static final String ACCOUNT_NOT_EXIST = "账号不存在";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_ALREADY_EXISTS= "该账号名已被使用";
    
    // 商品相关
    public static final String GOODS_NOT_FOUND_OR_NO_PERMISSION = "商品不存在或无权操作";
    public static final String GOODS_IN_RENT_CANNOT_OFFLINE = "该商品租借中，无法下架";
    public static final String GOODS_UPDATE_FAILED = "商品更新失败，商品不存在或无权操作";
    
    // 评论相关
    public static final String COMMENT_NOT_FOUND = "评论不存在";
    public static final String COMMENT_DELETE_NO_PERMISSION = "无权删除该评论";
    public static final String COMMENT_DELETE_FAILED = "删除失败，评论不存在或无权限";
}
