package com.interview.model.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 简历-JD 匹配分析结果（LLM 结构化输出）
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class MatchAnalysisResult {

    /**
     * 总体匹配分（0-100）
     */
    private BigDecimal overallScore;

    /**
     * 技能匹配分（0-100）
     */
    private BigDecimal skillScore;

    /**
     * 经验匹配分（0-100）
     */
    private BigDecimal experienceScore;

    /**
     * 匹配优势
     */
    private List<String> strengths;

    /**
     * 能力差距
     */
    private List<String> gaps;

    /**
     * 完整分析总结
     */
    private String analysis;

}
