package com.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 简历与岗位匹配分析表实体类
 * 对应数据库表：resume_job_matches
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeJobMatches {

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
     * 匹配优势（JSON格式）
     */
    private String strengths;

    /**
     * 能力差距（JSON格式）
     */
    private String gaps;

    /**
     * 完整分析结果（JSON格式）
     */
    private String analysis;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}