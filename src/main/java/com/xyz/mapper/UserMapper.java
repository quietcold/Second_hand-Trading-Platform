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
    @Insert("INSERT INTO user(account_num, password, email, phone, nickname, status, create_time, update_time) " +
            "VALUES(#{accountNum}, #{password}, #{email}, #{phone}, #{nickname}, #{status}, #{createTime}, #{updateTime})")
    int insert(User user);



    @Select("SELECT * FROM user WHERE account_num = #{accountNum} and password = #{password} ")
    User findByAcc_Pss(String accountNum,String  password);

    /**
     * 根据用户ID查询用户
     */
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findById(Long id);

}