package com.interview.mapper;

import com.interview.entity.Users;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
public interface AuthMapper {

    @Select("SELECT * FROM users WHERE email = #{email}")
    Users selectByEmail(String email);

    @Select("SELECT * FROM users WHERE email = #{account} OR phone = #{account}")
    Users selectByEmailOrPhone(@Param("account") String account);

    void insert(@Param("user") Users user);
}
