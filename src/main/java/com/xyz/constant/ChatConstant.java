package com.xyz.constant;

/**
 * 聊天相关常量
 */
public class ChatConstant {
    
    /**
     * 消息类型
     */
    public static final int MESSAGE_TYPE_TEXT = 1;      // 文本消息
    public static final int MESSAGE_TYPE_IMAGE = 2;     // 图片消息
    public static final int MESSAGE_TYPE_GOODS = 3;     // 商品卡片
    
    /**
     * 消息状态
     */
    public static final int MESSAGE_UNREAD = 0;         // 未读
    public static final int MESSAGE_READ = 1;           // 已读
    
    /**
     * 默认分页大小
     */
    public static final int DEFAULT_PAGE_SIZE = 50;
    
    /**
     * 消息撤回时间限制(毫秒) - 2分钟
     */
    public static final long RECALL_TIME_LIMIT = 2 * 60 * 1000;
    
    /**
     * 用户在线状态过期时间(秒) - 5分钟
     */
    public static final long ONLINE_STATUS_EXPIRE = 5 * 60;
    
    /**
     * WebSocket Session Attributes Key
     */
    public static final String WS_ATTR_USER_ID = "userId";  // WebSocket会话中存储的用户ID
}
