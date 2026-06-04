package com.interview.vo;

import lombok.Data;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
@Data
public class UserVO {

    /**
     * 用户ID
     */
    private Long id;

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

    /**
     * 状态：1正常 0禁用
     */
    private Integer status;

}
