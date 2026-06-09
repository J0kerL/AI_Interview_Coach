package com.interview.model.response;

import lombok.Data;

/**
 * 面试开始结果（LLM 结构化输出）
 * 只包含开场白（开场白中已包含第一道题）
 *
 * @Author Diamond
 * @Create 2026/6/8
 */
@Data
public class InterviewStartResult {

    /**
     * AI面试官开场白（包含引导自我介绍的问题）
     */
    private String greeting;

}
