package com.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 简历解析结果表实体类
 * 对应数据库表：resume_profiles
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeProfiles {

    /**
     * 简历画像ID
     */
    private Long id;

    /**
     * 简历ID
     */
    private Long resumeId;

    /**
     * 候选人姓名
     */
    private String candidateName;

    /**
     * 工作年限
     */
    private BigDecimal experienceYears;

    /**
     * AI生成个人总结
     */
    private String summary;

    /**
     * 技能标签（JSON格式）
     */
    private String skills;

    /**
     * 工作经历（JSON格式）
     */
    private String workExperiences;

    /**
     * 项目经历（JSON格式）
     */
    private String projectExperiences;

    /**
     * 教育经历（JSON格式）
     */
    private String education;

    /**
     * 解析模型
     */
    private String parsedModel;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}