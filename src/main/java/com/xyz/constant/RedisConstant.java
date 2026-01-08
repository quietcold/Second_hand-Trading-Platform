package com.xyz.constant;

/**
 * Redis 缓存 Key 常量
 */
public class RedisConstant {
    
    // ========== 商品相关 ==========
    /** 商品列表缓存前缀 - goods:list:categoryId */
    public static final String GOODS_LIST_KEY = "goods:list:";
    
    /** 商品详情缓存前缀 - goods:detail:goodsId */
    public static final String GOODS_DETAIL_KEY = "goods:detail:";
    
    /** 商品列表缓存过期时间（分钟） */
    public static final long GOODS_LIST_TTL = 30;
    
    /** 商品详情缓存过期时间（分钟） */
    public static final long GOODS_DETAIL_TTL = 60;
    
    // ========== 用户相关 ==========
    /** 用户信息缓存前缀 - user:info:userId */
    public static final String USER_INFO_KEY = "user:info:";
    
    /** 用户信息缓存过期时间（分钟） */
    public static final long USER_INFO_TTL = 30;
    
    // ========== Token 相关 ==========
    /** JWT Token 黑名单前缀 - token:blacklist:token */
    public static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";
}
