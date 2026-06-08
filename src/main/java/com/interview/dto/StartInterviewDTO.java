package com.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 开始面试请求DTO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class StartInterviewDTO {

    /**
     * 关联简历ID（简历面/混合面时必填）
     */
    private Long resumeId;

    /**
     * 关联JD ID（岗位面/混合面时必填）
     */
    private Long jdId;

    /**
     * 面试类型：resume-简历面, job-岗位面, mixed-综合面
     */
    @NotBlank(message = "面试类型不能为空")
    private String sessionType;

    /**
     * 生成题目数量（默认5题）
     */
    private Integer questionCount = 5;

    /**
     * 面试模式：text-纯文字, voice-语音面试（默认 text）
     */
    private String mode = "text";

}
