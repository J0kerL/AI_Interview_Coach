package com.interview.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.interview.common.exception.BusinessException;
import com.interview.dto.LoginDTO;
import com.interview.dto.RegisterDTO;
import com.interview.entity.Users;
import com.interview.mapper.AuthMapper;
import com.interview.service.AuthService;
import com.interview.vo.CaptchaVO;
import com.interview.vo.LoginVO;
import com.interview.vo.UserVO;
import com.wf.captcha.SpecCaptcha;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final String CAPTCHA_PREFIX = "captcha:";

    private final RedisTemplate<String, Object> redisTemplate;

    public AuthServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Resource
    private AuthMapper authMapper;

    /**
     * 获取验证码
     */
    @Override
    public CaptchaVO getCaptcha() {
        // 生成验证码
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);
        // 获取验证码文本
        String code = specCaptcha.text();
        // 生成验证码ID
        String captchaId = UUID.randomUUID().toString().replace("-", "");
        // 将验证码存入Redis
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + captchaId, code, 5, TimeUnit.MINUTES);
        // 创建CaptchaVO对象
        CaptchaVO captchaVO = new CaptchaVO();
        captchaVO.setCaptchaId(captchaId);
        captchaVO.setCaptchaImage(specCaptcha.toBase64());
        // 返回CaptchaVO对象
        return captchaVO;
    }

    /**
     * 注册
     */
    @Override
    public UserVO register(RegisterDTO registerDTO) {

        String email = registerDTO.getEmail();
        String password = registerDTO.getPassword();
        String confirmPassword = registerDTO.getConfirmPassword();

        // 1. 密码是否一致
        if (!password.equals(confirmPassword)) {
            throw new BusinessException("密码不一致");
        }

        // 2. 邮箱是否已存在
        if (authMapper.selectByEmail(email) != null) {
            throw new BusinessException("邮箱已存在");
        }

        // 3. 密码加密
        password = BCrypt.hashpw(password);

        // 4. 注册用户
        Users newUser = Users.builder()
                .email(email)
                .password(password)
                .build();
        authMapper.insert(newUser);

        // 5. 查询并返回完整用户信息
        Users savedUser = authMapper.selectByEmail(email);
        UserVO userVO = new UserVO();
        userVO.setId(savedUser.getId());
        userVO.setEmail(savedUser.getEmail());
        userVO.setPhone(savedUser.getPhone());
        userVO.setNickname(savedUser.getNickname());
        userVO.setAvatar(savedUser.getAvatar());
        userVO.setStatus(savedUser.getStatus());
        return userVO;
    }

    /**
     * 登录
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {

        String account = loginDTO.getAccount();
        String password = loginDTO.getPassword();
        String captchaId = loginDTO.getCaptchaId();
        String captchaCode = loginDTO.getCaptchaCode();

        // 1. 查询用户（邮箱或手机号）
        Users user = authMapper.selectByEmailOrPhone(account);
        if (user == null) {
            throw new BusinessException("账号不存在");
        }

        // 2. 校验账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        // 3. 校验密码
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 4. 校验验证码
        String redisKey = CAPTCHA_PREFIX + captchaId;
        Object cachedCode = redisTemplate.opsForValue().get(redisKey);
        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!captchaCode.equalsIgnoreCase(cachedCode.toString())) {
            throw new BusinessException("验证码错误");
        }
        // 验证通过后再删除，防止重复使用
        redisTemplate.delete(redisKey);

        // 5. Sa-Token 登录
        StpUtil.login(user.getId());

        // 6. 构建登录响应
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(StpUtil.getTokenValue());
        loginVO.setTokenTimeout(StpUtil.getTokenTimeout());
        loginVO.setUserId(user.getId());
        loginVO.setEmail(user.getEmail());
        loginVO.setPhone(user.getPhone());
        loginVO.setNickname(user.getNickname());
        loginVO.setAvatar(user.getAvatar());
        return loginVO;
    }
}
