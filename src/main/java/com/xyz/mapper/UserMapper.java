package com.xyz.mapper;

import com.xyz.entity.User;
import com.xyz.vo.UserListVO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据账号查询用户
     */
    @Select("SELECT * FROM user WHERE account_num = #{accountNum}")
    User findByAcc(String accountNum);
    
    /**
     * 根据昵称查询用户（用于检查昵称是否已存在）
     */
    @Select("SELECT * FROM user WHERE nickname = #{nickname}")
    User findByNickname(String nickname);

    
    /**
     * 插入新用户信息
     *
     * @return
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO user(account_num, password, email, phone, nickname, bio, status, create_time, update_time) " +
            "VALUES(#{accountNum}, #{password}, #{email}, #{phone}, #{nickname}, #{bio}, #{status}, #{createTime}, #{updateTime})")
    int insert(User user);



    @Select("SELECT * FROM user WHERE account_num = #{accountNum} and password = #{password} ")
    User findByAcc_Pss(String accountNum,String  password);

    /**
     * 根据用户ID查询用户
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

    /**
     * 更新用户信息
     */
    @Update("UPDATE user SET email = #{email}, phone = #{phone}, nickname = #{nickname}, " +
            "gender = #{gender}, image = #{image}, bio = #{bio}, update_time = #{updateTime} WHERE id = #{id}")
    int update(User user);

    /**
     * 修改用户密码
     */
    @Update("UPDATE user SET password = #{password}, update_time = #{updateTime} WHERE id = #{id}")
    int updatePassword(User user);

    /**
     * 分页查询用户列表（管理员）
     */
    @Select("SELECT id, account_num AS accountNum, nickname, email, phone, image, status, create_time AS createTime " +
            "FROM user ORDER BY create_time DESC LIMIT #{offset}, #{size}")
    List<UserListVO> findUserList(@Param("offset") int offset, @Param("size") int size);

    /**
     * 查询用户总数
     */
    @Select("SELECT COUNT(*) FROM user")
    int countUsers();

    /**
     * 更新用户状态（封禁/解封）
     */
    @Update("UPDATE user SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status, @Param("updateTime") java.time.LocalDateTime updateTime);

    /**
     * 游标分页查询用户列表（根据注册时间倒序）
     */
    @Select("SELECT id, account_num AS accountNum, nickname, email, phone, image, status, " +
            "create_time AS createTime, UNIX_TIMESTAMP(create_time) * 1000 AS createTimeTimestamp " +
            "FROM user WHERE UNIX_TIMESTAMP(create_time) * 1000 < #{cursor} " +
            "ORDER BY create_time DESC LIMIT #{size}")
    List<UserListVO> getUserListByCursor(@Param("cursor") long cursor, @Param("size") int size);

    /**
     * 查询所有用户ID和注册时间（用于ZSet缓存）
     */
    @Select("SELECT id, UNIX_TIMESTAMP(create_time) * 1000 AS createTime FROM user")
    List<Map<String, Object>> getAllUserIdsWithTime();

    /**
     * 根据ID集合批量查询用户卡片
     */
    @Select("<script>" +
            "SELECT id, account_num AS accountNum, nickname, email, phone, image, status, " +
            "create_time AS createTime, UNIX_TIMESTAMP(create_time) * 1000 AS createTimeTimestamp " +
            "FROM user WHERE id IN " +
            "<foreach item='id' collection='ids' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    List<UserListVO> getUserCardsByIds(@Param("ids") List<Long> ids);

}