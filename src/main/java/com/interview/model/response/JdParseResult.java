package com.interview.model.response;

import lombok.Data;

import java.util.List;

/**
 * JD 解析结果（LLM 结构化输出）
 *
 * @Author Diamond
 * @Create 2026/6/4
 */
@Data
public class JdParseResult {

    /**
     * 必备技能列表
     */
    private List<String> requiredSkills;

    /**
     * 加分技能列表
     */
    private List<String> preferredSkills;

    /**
     * 岗位职责
     */
    private List<String> responsibilities;

    /**
     * 关键词
     */
    private List<String> keywords;

    /**
     * 最低工作年限要求
     */
    private Integer minExperienceYears;

    /**
     * 学历要求
     */
    private String educationRequirement;

}
