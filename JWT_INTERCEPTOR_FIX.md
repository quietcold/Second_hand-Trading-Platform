# JWT拦截器优化 - 支持可选登录

## 问题描述

原来的JWT拦截器配置过于严格，拦截了所有接口，导致：
1. 未登录用户无法访问首页商品列表
2. 未登录用户无法查看商品详情
3. 未登录用户无法进行商品搜索

## 解决方案

### 1. 创建新的用户端JWT拦截器 (`JwtTokenUserInterceptor`)

**特点：**
- 支持可选登录和强制登录两种模式
- 根据接口路径和HTTP方法判断是否需要强制登录
- 未登录用户可以访问公开接口，登录用户可以访问所有接口

**判断逻辑：**
```java
// 需要强制登录的接口
- /user/goods-query/my/favorite      (我的收藏)
- /user/goods-query/published/my     (我的发布)
- /user/goods-query/offline/my       (我下架的商品)
- /user/chat/**                      (聊天相关)
- /user/comment/**                   (评论相关)
- POST/PUT/DELETE /user/goods/**     (商品的增删改操作)
- /user/goods/**/favorite            (收藏相关操作)

// 公开接口（不需要登录）
- GET /user/goods-query/category/page  (分类商品查询)
- GET /user/goods-query/all/page       (所有商品查询)  
- GET /user/goods/search               (商品搜索)
- GET /user/goods/{id}                 (商品详情查询)
- GET /user/category/**                (分类查询)
```

### 2. 重构拦截器配置 (`WebMvcConfiguration`)

**新的配置策略：**
- **管理员接口** (`/admin/**`): 使用严格的 `JwtTokenAdminInterceptor`
- **用户端接口** (`/user/**`): 使用灵活的 `JwtTokenUserInterceptor`
- **文档接口**: 不拦截

### 3. 处理逻辑

**有token的情况：**
1. 解析token成功 → 设置用户ID到ThreadLocal → 放行
2. 解析token失败 → 根据接口类型决定是否放行

**无token的情况：**
1. 访问需要强制登录的接口 → 返回401
2. 访问公开接口 → 直接放行（ThreadLocal中userId为null）

### 4. 服务层适配

服务层需要适配可能为null的用户ID：

```java
// 示例：商品详情服务
public GoodsDetailVO getGoodsDetailById(Long id) {
    // 获取基本商品信息（不需要用户登录）
    GoodsDetailVO detail = goodsMapper.getGoodsDetailById(id);
    
    // 如果用户已登录，可以添加个性化信息
    Long currentUserId = BaseContext.getCurrentId();
    if (currentUserId != null) {
        // 设置是否已收藏等个性化信息
        detail.setIsFavorited(checkIfFavorited(currentUserId, id));
    }
    
    return detail;
}
```

## 现在的访问行为

### ✅ 未登录用户可以访问
- 首页商品列表
- 商品搜索
- 商品详情查看
- 分类浏览
- 评论查看

### 🔐 需要登录才能访问
- 发布商品
- 编辑/删除商品
- 收藏商品
- 发表评论
- 查看个人中心
- 聊天功能

### 💡 前端体验优化
- 未登录用户可以正常浏览
- 点击需要登录的功能时显示精美弹窗
- 不会强制跳转到登录页

## 部署说明

1. **重启后端服务**：新的拦截器配置需要重启才能生效
2. **清除前端缓存**：避免旧的token导致问题
3. **测试各种场景**：
   - 未登录访问首页
   - 未登录查看商品详情
   - 登录后的完整功能
   - 登录状态的保持和恢复

## 技术优势

1. **更好的用户体验**：降低使用门槛，用户可以先浏览再决定是否注册
2. **灵活的权限控制**：精确控制哪些功能需要登录
3. **向后兼容**：现有的登录功能不受影响
4. **安全性保障**：敏感操作仍然需要登录验证

这个方案既保证了安全性，又提供了良好的用户体验，符合现代Web应用的设计理念。