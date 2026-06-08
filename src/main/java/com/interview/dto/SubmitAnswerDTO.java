package com.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 提交回答请求DTO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class SubmitAnswerDTO {

    /**
     * 问题ID
     */
    @NotNull(message = "问题ID不能为空")
    private Long questionId;

    /**
     * 回答内容（文本）
     */
    @NotBlank(message = "回答内容不能为空")
    private String answerText;

}
