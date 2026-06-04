package com.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试报告表实体类
 * 对应数据库表：interview_reports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewReports {

    /**
     * 报告ID
     */
    private Long id;

    /**
     * 面试会话ID
     */
    private Long sessionId;

    /**
     * 综合评分
     */
    private BigDecimal overallScore;

    /**
     * 技术评分
     */
    private BigDecimal technicalScore;

    /**
     * 沟通评分
     */
    private BigDecimal communicationScore;

    /**
     * 项目深度评分
     */
    private BigDecimal projectScore;

    /**
     * 优势分析（JSON格式）
     */
    private String strengths;

    /**
     * 待提升项（JSON格式）
     */
    private String weaknesses;

    /**
     * 学习建议（JSON格式）
     */
    private String recommendations;

    /**
     * 完整报告（JSON格式）
     */
    private String reportContent;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}