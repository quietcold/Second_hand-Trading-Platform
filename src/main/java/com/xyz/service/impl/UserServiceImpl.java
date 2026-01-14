package com.xyz.service.impl;

import com.xyz.constant.MessageConstant;
import com.xyz.constant.RedisConstant;
import com.xyz.dto.PasswordUpdateDTO;
import com.xyz.dto.UserLoginDTO;
import com.xyz.dto.UserRegisterDTO;
import com.xyz.dto.UserUpdateDTO;
import com.xyz.entity.User;
import com.xyz.exception.AccountNotFoundException;
import com.xyz.exception.AccountBannedException;
import com.xyz.exception.PasswordErrorException;
import com.xyz.mapper.UserMapper;
import com.xyz.service.UserService;
import com.xyz.vo.UserInfoVO;
import com.xyz.vo.UserPublicVO;
import com.xyz.vo.UserListVO;
import com.xyz.vo.UserDetailVO;
import com.xyz.vo.PageResult;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    

    
    @Override
    @Transactional  // 添加事务管理
    public User register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User userExist = userMapper.findByAcc(registerDTO.getAccountNum());
        if (userExist != null) {
            throw new RuntimeException("用户已存在");
        }

        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        
        // 处理空字符串转 null（避免唯一约束冲突）
        if (user.getEmail() != null && user.getEmail().trim().isEmpty()) {
            user.setEmail(null);
        }
        if (user.getPhone() != null && user.getPhone().trim().isEmpty()) {
            user.setPhone(null);
        }
        
        // 设置默认值
        user.setStatus(1); // 1-启用
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 保存用户到数据库
        userMapper.insert(user);
        
        // 添加到ZSet缓存（只有当ZSet已存在时才添加）
        String zsetKey = RedisConstant.USER_LIST_IDS_KEY;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(zsetKey))) {
            long timestamp = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(zsetKey, user.getId(), timestamp);
        }

        // 返回用户VO（不包含密码）
        return user;
    }

    
    @Override
    public User login(UserLoginDTO loginDTO) {
        // 根据用户名查询用户
        User userExist = userMapper.findByAcc(loginDTO.getAccountNum());
        if (userExist == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_EXIST);
        }
        
        // 验证密码
        User user=userMapper.findByAcc_Pss(loginDTO.getAccountNum(), loginDTO.getPassword());
        if (user == null) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }
        
        // 检查账号状态
        if (user.getStatus() == 0) {
            throw new AccountBannedException(MessageConstant.ACCOUNT_BANNED);
        }

        return user;
    }

    @Override
    public UserInfoVO getUserInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException("用户不存在");
        }

        // 将User转换为UserInfoVO（不包含密码）
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }

    @Override
    @Transactional
    public UserInfoVO updateUserInfo(Long userId, UserUpdateDTO updateDTO) {
        // 查询用户是否存在
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException("用户不存在");
        }

        // 更新用户信息
        BeanUtils.copyProperties(updateDTO, user);
        
        // 处理空字符串转 null（避免唯一约束冲突）
        if (user.getEmail() != null && user.getEmail().trim().isEmpty()) {
            user.setEmail(null);
        }
        if (user.getPhone() != null && user.getPhone().trim().isEmpty()) {
            user.setPhone(null);
        }
        
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
        
        // 清除用户卡片缓存
        clearUserCardCache(userId);

        // 返回更新后的用户信息
        UserInfoVO userInfoVO = new UserInfoVO();
        BeanUtils.copyProperties(user, userInfoVO);
        return userInfoVO;
    }

    @Override
    public UserPublicVO getUserPublicInfo(Long userId) {
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.USER_NOT_FOUND);
        }

        // 将User转换为UserPublicVO（只包含公开信息）
        UserPublicVO userPublicVO = new UserPublicVO();
        BeanUtils.copyProperties(user, userPublicVO);
        return userPublicVO;
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, PasswordUpdateDTO passwordUpdateDTO) {
        // 查询用户是否存在
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!user.getPassword().equals(passwordUpdateDTO.getOldPassword())) {
            throw new PasswordErrorException(MessageConstant.OLD_PASSWORD_ERROR);
        }

        // 更新密码
        user.setPassword(passwordUpdateDTO.getNewPassword());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updatePassword(user);
    }

    @Override
    public PageResult<UserListVO> getUserList(Long cursor, int size) {
        // 首次请求或cursor为null，使用当前时间
        long cursorTime = (cursor == null || cursor <= 0) ? System.currentTimeMillis() : cursor;
        
        String zsetKey = RedisConstant.USER_LIST_IDS_KEY;
        
        // 尝试从Redis ZSet获取ID列表
        List<Long> ids = getIdsFromZSet(zsetKey, cursorTime, size + 1);
        
        List<UserListVO> list;
        if (ids != null && !ids.isEmpty()) {
            // ZSet命中，先从缓存批量获取，未命中的再查MySQL
            list = getUserCardsFromCacheOrDB(ids);
        } else {
            // ZSet未命中，直接查MySQL并重建缓存
            list = userMapper.getUserListByCursor(cursorTime, size + 1);
            rebuildUserListZSet();
            // 将查询结果存入卡片缓存
            cacheUserCards(list);
        }
        
        return buildPageResult(list, size);
    }

    @Override
    public UserDetailVO getUserDetail(Long userId) {
        // 复用 getUserInfo 逻辑
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.USER_NOT_FOUND);
        }

        // 转换为 UserDetailVO
        UserDetailVO userDetailVO = new UserDetailVO();
        BeanUtils.copyProperties(user, userDetailVO);
        return userDetailVO;
    }

    @Override
    @Transactional
    public void banUser(Long userId) {
        // 检查用户是否存在
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.USER_NOT_FOUND);
        }

        // 更新状态为禁用
        userMapper.updateStatus(userId, 0, LocalDateTime.now());
        
        // 清除用户卡片缓存
        clearUserCardCache(userId);
    }

    @Override
    @Transactional
    public void unbanUser(Long userId) {
        // 检查用户是否存在
        User user = userMapper.findById(userId);
        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.USER_NOT_FOUND);
        }

        // 更新状态为启用
        userMapper.updateStatus(userId, 1, LocalDateTime.now());
        
        // 清除用户卡片缓存
        clearUserCardCache(userId);
    }
    
    // ==================== Redis缓存辅助方法 ====================
    
    /**
     * 从ZSet中获取用户ID列表
     * @param key ZSet的key
     * @param cursor 游标时间戳
     * @param count 获取数量
     * @return ID列表，如果ZSet不存在返回null
     */
    private List<Long> getIdsFromZSet(String key, long cursor, int count) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            return null;
        }
        // reverseRangeByScore: 按score从大到小排序
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeByScoreWithScores(key, 0, cursor - 1, 0, count);
        
        if (tuples == null || tuples.isEmpty()) {
            return List.of();
        }
        
        return tuples.stream()
                .map(t -> ((Number) t.getValue()).longValue())
                .collect(Collectors.toList());
    }
    
    /**
     * 重建用户列表ZSet缓存
     */
    private void rebuildUserListZSet() {
        String key = RedisConstant.USER_LIST_IDS_KEY;
        List<Map<String, Object>> data = userMapper.getAllUserIdsWithTime();
        if (data == null || data.isEmpty()) {
            return;
        }
        
        Set<ZSetOperations.TypedTuple<Object>> tuples = data.stream()
                .map(m -> ZSetOperations.TypedTuple.of(
                        (Object) ((Number) m.get("id")).longValue(),
                        ((Number) m.get("createTime")).doubleValue()
                ))
                .collect(Collectors.toSet());
        
        redisTemplate.opsForZSet().add(key, tuples);
        redisTemplate.expire(key, RedisConstant.USER_LIST_IDS_TTL, TimeUnit.MINUTES);
    }
    
    /**
     * 从缓存或数据库获取用户卡片列表
     * @param ids 用户ID列表
     * @return 用户卡片列表，保持原始ids顺序
     */
    private List<UserListVO> getUserCardsFromCacheOrDB(List<Long> ids) {
        // 1. 构建缓存keys
        List<String> keys = ids.stream()
                .map(id -> RedisConstant.USER_CARD_KEY + id)
                .collect(Collectors.toList());
        
        // 2. 批量从缓存获取
        List<Object> cached = redisTemplate.opsForValue().multiGet(keys);
        
        // 3. 找出缓存未命中的ids
        List<Long> missIds = new ArrayList<>();
        Map<Long, UserListVO> resultMap = new HashMap<>();
        
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            Object value = (cached != null && i < cached.size()) ? cached.get(i) : null;
            if (value != null) {
                resultMap.put(id, (UserListVO) value);
            } else {
                missIds.add(id);
            }
        }
        
        // 4. 缓存未命中的，从MySQL查询并存入缓存
        if (!missIds.isEmpty()) {
            List<UserListVO> fromDB = userMapper.getUserCardsByIds(missIds);
            for (UserListVO vo : fromDB) {
                resultMap.put(vo.getId(), vo);
                // 存入缓存
                String key = RedisConstant.USER_CARD_KEY + vo.getId();
                long ttl = RedisConstant.USER_CARD_TTL + new Random().nextInt((int) RedisConstant.USER_CARD_TTL_RANDOM);
                redisTemplate.opsForValue().set(key, vo, ttl, TimeUnit.MINUTES);
            }
        }
        
        // 5. 按原始ids顺序返回
        return ids.stream()
                .map(resultMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    /**
     * 批量将用户卡片存入缓存
     */
    private void cacheUserCards(List<UserListVO> cards) {
        if (cards == null || cards.isEmpty()) {
            return;
        }
        for (UserListVO vo : cards) {
            String key = RedisConstant.USER_CARD_KEY + vo.getId();
            long ttl = RedisConstant.USER_CARD_TTL + new Random().nextInt((int) RedisConstant.USER_CARD_TTL_RANDOM);
            redisTemplate.opsForValue().set(key, vo, ttl, TimeUnit.MINUTES);
        }
    }
    
    /**
     * 构建分页结果
     */
    private PageResult<UserListVO> buildPageResult(List<UserListVO> list, int size) {
        if (list == null || list.isEmpty()) {
            return PageResult.empty();
        }
        
        boolean hasMore = list.size() > size;
        if (hasMore) {
            list = list.subList(0, size); // 移除多查的那一条
        }
        
        // 下一页游标 = 本页最后一条的时间戳
        Long nextCursor = list.isEmpty() ? null : list.get(list.size() - 1).getCreateTimeTimestamp();
        
        return PageResult.of(list, nextCursor, hasMore);
    }
    
    /**
     * 清除用户卡片缓存
     */
    private void clearUserCardCache(Long userId) {
        redisTemplate.delete(RedisConstant.USER_CARD_KEY + userId);
    }
    
    /**
     * 更新用户在ZSet中的score
     */
    private void updateUserInZSet(Long userId, long timestamp) {
        String key = RedisConstant.USER_LIST_IDS_KEY;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.opsForZSet().add(key, userId, timestamp);
        }
    }

}