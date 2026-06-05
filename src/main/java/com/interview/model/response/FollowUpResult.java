package com.interview.model.response;

import lombok.Data;

/**
 * AI 追问决策结果（LLM 结构化输出）
 *
 * @Author Diamond
 * @Create 2026/6/4
 */
@Data
public class FollowUpResult {

    /**
     * 是否需要追问
     */
    private Boolean needFollowUp;

    /**
     * 追问内容（needFollowUp=true 时有值）
     */
    private String followUpQuestion;

    /**
     * 追问原因
     */
    private String reason;

}
