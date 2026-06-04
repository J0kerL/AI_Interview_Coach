package com.interview.service;

import com.interview.dto.ForgotPasswordDTO;
import com.interview.dto.UpdatePasswordDTO;
import com.interview.dto.UpdateProfileDTO;
import com.interview.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
public interface UserService {

    /**
     * 获取当前登录用户信息
     */
    UserVO getUserInfo();

    /**
     * 修改用户信息
     */
    UserVO updateProfile(UpdateProfileDTO updateProfileDTO);

    /**
     * 修改密码
     */
    void updatePassword(UpdatePasswordDTO updatePasswordDTO);

    /**
     * 忘记密码
     */
    void forgotPassword(ForgotPasswordDTO forgotPasswordDTO);

    /**
     * 修改头像
     */
    String updateAvatar(MultipartFile file);

}
