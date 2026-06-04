package com.interview.controller.user;

import com.interview.common.Result;
import com.interview.dto.ForgotPasswordDTO;
import com.interview.dto.UpdatePasswordDTO;
import com.interview.dto.UpdateProfileDTO;
import com.interview.service.UserService;
import com.interview.vo.UserVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        UserVO userVO = userService.getUserInfo();
        return Result.success(userVO);
    }

    /**
     * 修改用户信息
     */
    @PutMapping("/profile")
    public Result<UserVO> updateProfile(@Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        UserVO userVO = userService.updateProfile(updateProfileDTO);
        return Result.success(userVO);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordDTO updatePasswordDTO) {
        userService.updatePassword(updatePasswordDTO);
        return Result.success();
    }

    /**
     * 忘记密码
     */
    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO forgotPasswordDTO) {
        userService.forgotPassword(forgotPasswordDTO);
        return Result.success();
    }

    /**
     * 修改头像
     */
    @PutMapping("/avatar")
    public Result<String> updateAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.updateAvatar(file);
        return Result.success(avatarUrl);
    }

}
