package com.interview.vo;

import lombok.Data;

/**
 * 登录响应VO
 *
 * @Author Diamond
 * @Create 2026/6/4
 */
@Data
public class LoginVO {

    /**
     * Token
     */
    private String token;

    /**
     * Token 剩余有效期（秒）
     */
    private Long tokenTimeout;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像地址
     */
    private String avatar;

}
