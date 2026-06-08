package com.interview.vo;

import lombok.Data;

/**
 * JD 解析结果VO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class JdAnalysisVO {

    /**
     * JD ID
     */
    private Long jdId;

    /**
     * 必备技能（JSON 字符串）
     */
    private String requiredSkills;

    /**
     * 加分技能（JSON 字符串）
     */
    private String preferredSkills;

    /**
     * 岗位职责（JSON 字符串）
     */
    private String responsibilities;

    /**
     * 关键词（JSON 字符串）
     */
    private String keywords;

}
