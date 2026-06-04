package com.interview.service;

import com.interview.dto.LoginDTO;
import com.interview.dto.RegisterDTO;
import com.interview.vo.CaptchaVO;
import com.interview.vo.LoginVO;
import com.interview.vo.UserVO;
import jakarta.validation.Valid;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
public interface AuthService {

    /**
     * 获取验证码
     */
    CaptchaVO getCaptcha();

    /**
     * 注册
     */
    UserVO register(@Valid RegisterDTO registerDTO);

    /**
     * 登录
     */
    LoginVO login(LoginDTO loginDTO);
}
