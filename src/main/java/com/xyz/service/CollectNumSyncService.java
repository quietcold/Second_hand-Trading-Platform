package com.xyz.service;

/**
 * 收藏数同步服务接口
 */
public interface CollectNumSyncService {
    
    /**
     * 标记商品收藏数需要同步
     * @param goodsId 商品ID
     */
    void markForSync(Long goodsId);
    
    /**
     * 批量同步收藏数到数据库
     */
    void batchSyncCollectNum();
    
    /**
     * 强制同步指定商品的收藏数
     * @param goodsId 商品ID
     */
    void forceSyncCollectNum(Long goodsId);
}