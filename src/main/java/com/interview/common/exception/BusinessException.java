package com.interview.common.exception;

import lombok.Getter;

/**
 * 自定义业务异常类
 *
 * @Author Diamond
 * @Create 2026/6/3
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误状态码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 通过错误消息构造（默认状态码500）
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }

    /**
     * 通过状态码和错误消息构造
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}