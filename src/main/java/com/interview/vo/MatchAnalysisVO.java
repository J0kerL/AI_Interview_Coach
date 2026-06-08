package com.interview.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 简历-JD 匹配分析结果VO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class MatchAnalysisVO {

    /**
     * 匹配记录ID
     */
    private Long id;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * JD ID
     */
    private Long jdId;

    /**
     * 总体匹配分
     */
    private BigDecimal overallScore;

    /**
     * 技能匹配分
     */
    private BigDecimal skillScore;

    /**
     * 经验匹配分
     */
    private BigDecimal experienceScore;

    /**
     * 匹配优势（JSON 字符串）
     */
    private String strengths;

    /**
     * 能力差距（JSON 字符串）
     */
    private String gaps;

    /**
     * 完整分析结果（JSON 字符串）
     */
    private String analysis;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
