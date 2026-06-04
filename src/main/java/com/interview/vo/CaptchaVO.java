package com.interview.vo;

import lombok.Data;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
@Data
public class CaptchaVO {

    /**
     * 验证码ID
     */
    private String captchaId;

    /**
     * Base64图片
     */
    private String captchaImage;
}