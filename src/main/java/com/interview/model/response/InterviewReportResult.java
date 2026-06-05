package com.interview.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 面试报告生成结果（LLM 结构化输出）
 *
 * @Author Diamond
 * @Create 2026/6/4
 */
@Data
public class InterviewReportResult {

    /**
     * 综合评分（0-100）
     */
    private BigDecimal overallScore;

    /**
     * 技术能力评分
     */
    private BigDecimal technicalScore;

    /**
     * 沟通能力评分
     */
    private BigDecimal communicationScore;

    /**
     * 项目深度评分
     */
    private BigDecimal projectScore;

    /**
     * 优势分析
     */
    private List<String> strengths;

    /**
     * 待提升项
     */
    private List<String> weaknesses;

    /**
     * 学习建议
     */
    private List<String> recommendations;

}
