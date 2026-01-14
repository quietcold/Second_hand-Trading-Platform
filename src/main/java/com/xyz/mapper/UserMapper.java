package com.xyz.mapper;

import com.xyz.entity.User;
import org.apache.ibatis.annotations.*;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {
    
    /**
     * 根据账号查询用户
     */
    @Select("SELECT * FROM user WHERE account_num = #{accountNum}")
    User findByAcc( String accountNum);

    
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

}