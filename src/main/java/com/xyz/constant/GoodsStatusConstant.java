package com.xyz.constant;

/**
 * 商品状态常量
 */
public class GoodsStatusConstant {
    
    /** 上架(待售卖/待租赁) */
    public static final int ON_SALE = 1;
    
    /** 已售出 */
    public static final int SOLD = 2;
    
    /** 租借中 */
    public static final int RENTING = 3;
    
    /** 已下架 */
    public static final int OFF_SHELF = 4;
    
    /** 用户删除 */
    public static final int USER_DELETED = 5;
    
    /** 系统屏蔽/违规删除 */
    public static final int SYSTEM_BLOCKED = 6;
}
