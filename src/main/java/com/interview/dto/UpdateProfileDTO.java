package com.interview.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改用户信息请求DTO
 * 所有字段均可选，仅修改需要变更的字段
 *
 * @Author Diamond
 * @Create 2026/6/4
 */
@Data
public class UpdateProfileDTO {

    /**
     * 邮箱（可选，不传则不修改）
     */
    @Pattern(regexp = "^$|^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$", message = "邮箱格式不正确")
    private String email;

    /**
     * 手机号（可选，不传则不修改）
     */
    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 昵称（可选，不传则不修改）
     */
    @Size(max = 100, message = "昵称长度不能超过100个字符")
    private String nickname;

}
