package com.interview.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 简历解析结果VO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class ResumeProfileVO {

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
     * AI 生成个人总结
     */
    private String summary;

    /**
     * 技能标签（JSON 字符串）
     */
    private String skills;

    /**
     * 工作经历（JSON 字符串）
     */
    private String workExperiences;

    /**
     * 项目经历（JSON 字符串）
     */
    private String projectExperiences;

    /**
     * 教育经历（JSON 字符串）
     */
    private String education;

}
