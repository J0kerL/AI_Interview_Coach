package com.interview.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.interview.common.exception.BusinessException;
import com.interview.dto.ForgotPasswordDTO;
import com.interview.dto.UpdatePasswordDTO;
import com.interview.dto.UpdateProfileDTO;
import com.interview.entity.Users;
import com.interview.mapper.UserMapper;
import com.interview.service.FileService;
import com.interview.service.UserService;
import com.interview.vo.UserVO;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String CAPTCHA_PREFIX = "captcha:";

    @Resource
    private UserMapper userMapper;

    @Resource
    private FileService fileService;

    private final RedisTemplate<Object, Object> redisTemplate;

    public UserServiceImpl(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取当前登录用户信息
     */
    @Override
    public UserVO getUserInfo() {

        // 获取当前登录用户id
        long id = StpUtil.getLoginIdAsLong();

        // 根据id获取用户信息
        Users user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 校验账号状态，禁用则强制下线
        if (user.getStatus() != null && user.getStatus() == 0) {
            StpUtil.logout();
            throw new BusinessException("账号已被禁用");
        }

        // 创建UserVO对象
        UserVO userVO = new UserVO();

        // 复制属性
        BeanUtils.copyProperties(user, userVO);

        // 返回用户信息
        return userVO;
    }

    /**
     * 修改用户信息
     */
    @Override
    public UserVO updateProfile(UpdateProfileDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();

        // 校验邮箱是否被其他用户占用
        if (StringUtils.hasText(dto.getEmail())) {
            Users existing = userMapper.selectByEmailExclude(dto.getEmail(), userId);
            if (existing != null) {
                throw new BusinessException("该邮箱已被其他用户使用");
            }
        }

        // 校验手机号是否被其他用户占用
        if (StringUtils.hasText(dto.getPhone())) {
            Users existing = userMapper.selectByPhoneExclude(dto.getPhone(), userId);
            if (existing != null) {
                throw new BusinessException("该手机号已被其他用户使用");
            }
        }

        // 构建更新对象（仅设置非空字段）
        Users updateUser = Users.builder().id(userId).build();
        if (StringUtils.hasText(dto.getEmail())) {
            updateUser.setEmail(dto.getEmail());
        }
        if (StringUtils.hasText(dto.getPhone())) {
            updateUser.setPhone(dto.getPhone());
        }
        if (dto.getNickname() != null) {
            updateUser.setNickname(dto.getNickname());
        }

        userMapper.updateProfile(updateUser);

        // 查询并返回最新用户信息
        Users updatedUser = userMapper.selectById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(updatedUser, userVO);
        return userVO;
    }

    /**
     * 修改密码
     */
    @Override
    public void updatePassword(UpdatePasswordDTO dto) {
        long userId = StpUtil.getLoginIdAsLong();

        // 1. 新密码与确认密码是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        // 2. 查询当前用户
        Users user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 3. 校验旧密码
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }

        // 4. 加密并更新密码
        String hashedPassword = BCrypt.hashpw(dto.getNewPassword());
        userMapper.updatePassword(userId, hashedPassword);
    }

    /**
     * 忘记密码
     */
    @Override
    public void forgotPassword(ForgotPasswordDTO dto) {
        String account = dto.getAccount();
        String newPassword = dto.getNewPassword();
        String confirmNewPassword = dto.getConfirmNewPassword();
        String captchaId = dto.getCaptchaId();
        String captchaCode = dto.getCaptchaCode();

        // 1. 新密码与确认密码是否一致
        if (!newPassword.equals(confirmNewPassword)) {
            throw new BusinessException("两次输入的新密码不一致");
        }

        // 2. 查询用户（邮箱或手机号）
        Users user = userMapper.selectByEmailOrPhone(account);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }

        // 3. 校验验证码
        String redisKey = CAPTCHA_PREFIX + captchaId;
        Object cachedCode = redisTemplate.opsForValue().get(redisKey);
        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!captchaCode.equalsIgnoreCase(cachedCode.toString())) {
            throw new BusinessException("验证码错误");
        }
        // 验证通过后删除
        redisTemplate.delete(redisKey);

        // 4. 加密并更新密码
        String hashedPassword = BCrypt.hashpw(newPassword);
        userMapper.updatePassword(user.getId(), hashedPassword);
    }

    /**
     * 修改头像
     */
    @Override
    public String updateAvatar(MultipartFile file) {
        long userId = StpUtil.getLoginIdAsLong();

        // 1. 上传到 OSS
        String avatarUrl = fileService.uploadImage(file, "avatar");

        // 2. 更新数据库
        userMapper.updateAvatar(userId, avatarUrl);

        return avatarUrl;
    }

}
