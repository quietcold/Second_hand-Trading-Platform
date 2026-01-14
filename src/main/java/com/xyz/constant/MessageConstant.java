package com.xyz.constant;


public  class MessageConstant {
    public static final String Register_SUCCESS = "注册成功";
    public static final String ACCOUNT_NOT_EXIST = "账号不存在";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_ALREADY_EXISTS= "该账号名已被使用";
    public static final String OLD_PASSWORD_ERROR = "旧密码错误";
    public static final String PASSWORD_UPDATE_SUCCESS = "密码修改成功";
    public static final String USER_NOT_FOUND = "用户不存在";
    
    // 商品相关
    public static final String GOODS_NOT_FOUND_OR_NO_PERMISSION = "商品不存在或无权操作";
    public static final String GOODS_IN_RENT_CANNOT_OFFLINE = "该商品租借中，无法下架";
    public static final String GOODS_UPDATE_FAILED = "商品更新失败，商品不存在或无权操作";
    
    // 评论相关
    public static final String COMMENT_NOT_FOUND = "评论不存在";
    public static final String COMMENT_DELETE_NO_PERMISSION = "无权删除该评论";
    public static final String COMMENT_DELETE_FAILED = "删除失败，评论不存在或无权限";
    
    // 聊天相关
    public static final String CHAT_SESSION_NOT_FOUND = "会话不存在";
    public static final String CHAT_SESSION_CREATE_FAILED = "创建会话失败";
    public static final String CHAT_MESSAGE_SEND_FAILED = "消息发送失败";
    public static final String CHAT_NO_PERMISSION = "无权访问该会话";
    public static final String CHAT_MESSAGE_NOT_FOUND = "消息不存在";
    public static final String CHAT_RECALL_TIME_EXPIRED = "消息发送超过2分钟,无法撤回";
    public static final String CHAT_RECALL_NO_PERMISSION = "只能撤回自己发送的消息";
    public static final String CHAT_MESSAGE_ALREADY_RECALLED = "消息已被撤回";
    
    // 分类相关
    public static final String CATEGORY_NOT_FOUND = "分类不存在";
    public static final String CATEGORY_CODE_EXISTS = "分类编码已存在";
    public static final String CATEGORY_HAS_GOODS = "该分类下有商品，无法删除";
    public static final String CATEGORY_CREATE_SUCCESS = "分类创建成功";
    public static final String CATEGORY_UPDATE_SUCCESS = "分类更新成功";
    public static final String CATEGORY_DELETE_SUCCESS = "分类删除成功";
    public static final String CATEGORY_STATUS_ONLINE = "分类已上架";
    public static final String CATEGORY_STATUS_OFFLINE = "分类已下架";
    
    // 用户管理相关
    public static final String USER_BAN_SUCCESS = "用户封禁成功";
    public static final String USER_UNBAN_SUCCESS = "用户解封成功";
    public static final String ACCOUNT_BANNED = "账号已被封禁，请联系管理员";
    
    // 商品管理相关（管理员）
    public static final String GOODS_VIOLATION_OFFLINE_SUCCESS = "商品违规下架成功";
    public static final String GOODS_RESTORE_SUCCESS = "商品恢复成功";
    public static final String GOODS_ALREADY_OFFLINE = "商品已下架";
    public static final String GOODS_NOT_FOUND = "商品不存在";
}
