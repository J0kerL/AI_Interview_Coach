package com.interview.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 匹配分析请求DTO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class MatchAnalyzeDTO {

    /**
     * 简历ID
     */
    @NotNull(message = "简历ID不能为空")
    private Long resumeId;

    /**
     * JD ID
     */
    @NotNull(message = "JD ID不能为空")
    private Long jdId;

}
