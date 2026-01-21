# 评论功能 API 接口文档

## 1. 发表评论
**接口**: `POST /comments`

**请求体**:
```json
{
  "goodsId": 1,
  "content": "这是一条评论",
  "parentId": null,  // 顶级评论时为null，回复评论时传父评论ID
  "replyToUserId": null  // 顶级评论时为null，回复评论时传被回复的用户ID
}
```

**响应**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "commentId": 1,
    "userId": 100,
    "userNickname": "张三",
    "userAvatar": "http://...",
    "isSeller": false,
    "content": "这是一条评论",
    "replyToUserId": null,
    "replyToUserNickname": null,
    "likeCount": 0,
    "hasLiked": false,
    "createTime": "2026-01-11T10:00:00"
  }
}
```

---

## 2. 删除评论
**接口**: `DELETE /comments/{commentId}`

**响应**:
```json
{
  "code": 1,
  "msg": "success",
  "data": "删除成功"
}
```

---

## 3. 点赞/取消点赞评论
**接口**: `POST /comments/{commentId}/like`

**响应**:
```json
{
  "code": 1,
  "msg": "success",
  "data": "操作成功"
}
```

---

## 4. 获取商品的顶层评论列表
**接口**: `GET /comments/goods/{goodsId}?cursor={timestamp}&size=10`

**参数**:
- `cursor`: 游标（时间戳），首次请求不传或传0
- `size`: 每页数量，默认10

**响应**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "list": [
      {
        "commentId": 1,
        "userId": 100,
        "userNickname": "张三",
        "userAvatar": "http://...",
        "isSeller": true,
        "content": "这是一条父评论",
        "likeCount": 10,
        "hasLiked": false,
        "replyCount": 5,
        "createTime": "2026-01-11T10:00:00",
        "latestReply": {
          "commentId": 101,
          "userId": 200,
          "userNickname": "李四",
          "userAvatar": "http://...",
          "isSeller": false,
          "content": "这是最新的回复",
          "replyToUserId": 100,
          "replyToUserNickname": "张三",
          "likeCount": 2,
          "hasLiked": false,
          "createTime": "2026-01-11T12:00:00"
        }
      }
    ],
    "nextCursor": 1736568000000,
    "hasMore": true
  }
}
```

---

## 5. 获取评论的回复列表
**接口**: `GET /comments/{parentId}/replies?cursor={timestamp}&size=20`

**参数**:
- `cursor`: 游标（时间戳），首次请求不传或传0
- `size`: 每页数量，默认20

**响应**:
```json
{
  "code": 1,
  "msg": "success",
  "data": {
    "list": [
      {
        "commentId": 102,
        "userId": 300,
        "userNickname": "王五",
        "userAvatar": "http://...",
        "isSeller": false,
        "content": "这是另一条回复",
        "replyToUserId": 100,
        "replyToUserNickname": "张三",
        "likeCount": 3,
        "hasLiked": true,
        "createTime": "2026-01-11T11:30:00"
      }
    ],
    "nextCursor": 1736564000000,
    "hasMore": true
  }
}
```

---

## 前端使用说明

### 1. 显示评论时间为相对时间
前端需要将 `createTime` 转换为 "几小时前"、"几天前" 等格式。

**JavaScript 示例**:
```javascript
function formatRelativeTime(createTime) {
  const now = new Date();
  const commentTime = new Date(createTime);
  const diff = now - commentTime;
  
  const minutes = Math.floor(diff / 1000 / 60);
  const hours = Math.floor(diff / 1000 / 60 / 60);
  const days = Math.floor(diff / 1000 / 60 / 60 / 24);
  const months = Math.floor(diff / 1000 / 60 / 60 / 24 / 30);
  
  if (minutes < 1) return '刚刚';
  if (minutes < 60) return `${minutes}分钟前`;
  if (hours < 24) return `${hours}小时前`;
  if (days < 30) return `${days}天前`;
  return `${months}个月前`;
}
```

### 2. 展开/收起回复
前端初始显示：
- 如果 `replyCount > 0`，显示 `latestReply`
- 如果 `replyCount > 1`，显示按钮："展开X条回复"

点击展开后：
- 调用接口 5 获取所有回复
- 替换显示为完整的回复列表

### 3. 游标分页使用
- 首次请求不传 `cursor` 或传 `0`
- 后续请求使用上次返回的 `nextCursor`
- 如果 `hasMore` 为 `false`，则没有更多数据

---

## Redis 缓存策略

### 点赞功能
- `comment:like:{commentId}` - 存储点赞该评论的用户ID集合（Set）
- `user:like:comments:{userId}` - 存储用户点赞的评论ID集合（Set）
- 过期时间：60分钟

### 评论统计
- `comment:reply:count:{parentId}` - 回复数量缓存（String）
- `comment:latest:reply:{parentId}` - 最新回复ID缓存（String）
- 过期时间：30分钟

---

## 数据库表结构

见文件：`src/main/resources/sql/comments.sql`

需要执行该 SQL 创建 `comments` 表。
