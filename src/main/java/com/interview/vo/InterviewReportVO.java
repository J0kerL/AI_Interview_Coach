package com.interview.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 面试报告VO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class InterviewReportVO {

    private Long sessionId;
    private BigDecimal overallScore;
    private BigDecimal technicalScore;
    private BigDecimal communicationScore;
    private BigDecimal projectScore;
    /** 优势分析（JSON） */
    private String strengths;
    /** 待提升项（JSON） */
    private String weaknesses;
    /** 学习建议（JSON） */
    private String recommendations;

}
