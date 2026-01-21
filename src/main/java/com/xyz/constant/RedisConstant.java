 package com.xyz.constant;

/**
 * Redis 缓存 Key 常量
 */
public class RedisConstant {
    
    // ========== 商品相关 ==========
    /** 商品详情缓存前缀 - goods:detail:{goodsId} */
    public static final String GOODS_DETAIL_KEY = "goods:detail:";
    
    /** 商品详情缓存过期时间（分钟），设置30分钟 */
    public static final long GOODS_DETAIL_TTL = 30;
    
    /** 商品详情缓存过期时间随机浮动范围（分钟），防止缓存雪崩 */
    public static final long GOODS_DETAIL_TTL_RANDOM = 5;


    // ========== 用户相关 ==========
    /** 用户信息缓存前缀 - user:info:userId */
    public static final String USER_INFO_KEY = "user:info:";
    
    /** 用户信息缓存过期时间（分钟） */
    public static final long USER_INFO_TTL = 30;
    
    /** 用户ID ZSet缓存Key - user:list:ids，Score为注册时间戳 */
    public static final String USER_LIST_IDS_KEY = "user:list:ids";
    
    /** 用户列表ZSet缓存过期时间（分钟） */
    public static final long USER_LIST_IDS_TTL = 60;
    
    /** 用户卡片缓存前缀 - user:card:{userId} */
    public static final String USER_CARD_KEY = "user:card:";
    
    /** 用户卡片缓存过期时间（分钟） */
    public static final long USER_CARD_TTL = 30;
    
    /** 用户卡片缓存过期时间随机浮动范围（分钟），防止缓存雪崩 */
    public static final long USER_CARD_TTL_RANDOM = 5;


    // ========== Token 相关 ==========
    /** JWT Token 黑名单前缀 - token:blacklist:token */
    public static final String TOKEN_BLACKLIST_KEY = "token:blacklist:";


    // ========== 评论相关 ==========
    /** 评论点赞集合前缀 - comment:like:{commentId}，存储点赞该评论的用户ID集合 */
    public static final String COMMENT_LIKE_KEY = "comment:like:";
    
    /** 用户点赞评论集合前缀 - user:like:comments:{userId}，存储用户点赞的评论ID集合 */
    public static final String USER_LIKE_COMMENTS_KEY = "user:like:comments:";
    
    /** 评论回复数缓存前缀 - comment:reply:count:{parentId} */
    public static final String COMMENT_REPLY_COUNT_KEY = "comment:reply:count:";
    
    /** 评论最新回复ID缓存前缀 - comment:latest:reply:{parentId} */
    public static final String COMMENT_LATEST_REPLY_KEY = "comment:latest:reply:";
    
    /** 评论点赞数据缓存过期时间（分钟） */
    public static final long COMMENT_LIKE_TTL = 60;
    
    /** 评论统计数据缓存过期时间（分钟） */
    public static final long COMMENT_COUNT_TTL = 30;


    // ========== 商品查询相关 ==========
    /** 用户收藏商品ID ZSet缓存前缀 - favorite:user:{userId}:ids，Score为收藏时间戳 */
    public static final String FAVORITE_USER_IDS_KEY = "favorite:user:";
    public static final String FAVORITE_USER_IDS_SUFFIX = ":ids";

    /** 分类商品ID ZSet缓存前缀 - goods:cat:{categoryId}:ids，Score为更新时间戳 */
    public static final String GOODS_CAT_IDS_KEY = "goods:cat:";
    public static final String GOODS_CAT_IDS_SUFFIX = ":ids";

    /** 用户商品ID ZSet缓存前缀 - goods:owner:{ownerId}:ids，Score为更新时间戳 */
    public static final String GOODS_OWNER_IDS_KEY = "goods:owner:";
    public static final String GOODS_OWNER_IDS_SUFFIX = ":ids";

    /** 所有商品ID ZSet缓存Key - goods:all:ids，Score为更新时间戳 */
    public static final String GOODS_ALL_IDS_KEY = "goods:all";
    public static final String GOODS_ALL_IDS_SUFFIX = ":ids";

    /** 用户下架商品ID ZSet缓存前缀 - goods:offline:{ownerId}:ids，Score为更新时间戳 */
    public static final String GOODS_OFFLINE_IDS_KEY = "goods:offline:";
    public static final String GOODS_OFFLINE_IDS_SUFFIX = ":ids";


    /** 商品卡片缓存前缀 - goods:card:{goodsId} */
    public static final String GOODS_CARD_KEY = "goods:card:";

    /** 商品卡片缓存过期时间（分钟） */
    public static final long GOODS_CARD_TTL = 30;

    /** ZSet缓存过期时间（分钟） */
    public static final long GOODS_IDS_TTL = 60;
}
