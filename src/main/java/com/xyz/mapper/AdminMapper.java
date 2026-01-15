package com.xyz.mapper;

import com.xyz.entity.Admin;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 管理员Mapper接口
 */
@Mapper
public interface AdminMapper {

    /**
     * 根据用户名查询管理员
     */
    @Select("SELECT * FROM admin WHERE username = #{username}")
    Admin findByUsername(String username);

    /**
     * 根据用户名和密码查询管理员
     */
    @Select("SELECT * FROM admin WHERE username = #{username} AND password = #{password}")
    Admin findByUsernameAndPassword(String username, String password);

    /**
     * 根据ID查询管理员
     */
    @Select("SELECT * FROM admin WHERE id = #{id}")
    Admin findById(Long id);

    /**
     * 插入管理员
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("INSERT INTO admin(username, password, real_name, phone, email, status, create_time, update_time) " +
            "VALUES(#{username}, #{password}, #{realName}, #{phone}, #{email}, #{status}, #{createTime}, #{updateTime})")
    int insert(Admin admin);

    /**
     * 更新最后登录时间
     */
    @Update("UPDATE admin SET last_login_time = #{lastLoginTime}, update_time = #{updateTime} WHERE id = #{id}")
    int updateLastLoginTime(Admin admin);

    /**
     * 更新管理员状态
     */
    @Update("UPDATE admin SET status = #{status}, update_time = #{updateTime} WHERE id = #{id}")
    int updateStatus(Long id, Integer status, java.time.LocalDateTime updateTime);
}
