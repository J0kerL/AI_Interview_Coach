package com.interview.mapper;

import com.interview.entity.Users;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
public interface UserMapper {

    @Select("select * from users where id = #{id}")
    Users selectById(long id);

    @Select("SELECT * FROM users WHERE email = #{account} OR phone = #{account}")
    Users selectByEmailOrPhone(@Param("account") String account);

    @Select("SELECT * FROM users WHERE email = #{email} AND id != #{excludeId}")
    Users selectByEmailExclude(@Param("email") String email, @Param("excludeId") long excludeId);

    @Select("SELECT * FROM users WHERE phone = #{phone} AND id != #{excludeId}")
    Users selectByPhoneExclude(@Param("phone") String phone, @Param("excludeId") long excludeId);

    void updateProfile(@Param("user") Users user);

    void updatePassword(@Param("id") long id, @Param("password") String password);

    void updateAvatar(@Param("id") long id, @Param("avatar") String avatar);

}
