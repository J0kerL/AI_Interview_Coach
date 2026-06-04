package com.interview.controller.user;

import cn.dev33.satoken.stp.StpUtil;
import com.interview.common.Result;
import com.interview.dto.LoginDTO;
import com.interview.dto.RegisterDTO;
import com.interview.service.AuthService;
import com.interview.vo.CaptchaVO;
import com.interview.vo.LoginVO;
import com.interview.vo.UserVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 *
 * @Author Diamond
 * @Create 2026/6/3
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha() {
        return Result.success(authService.getCaptcha());
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success(authService.register(registerDTO));
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        StpUtil.logout();
        return Result.success();
    }

}