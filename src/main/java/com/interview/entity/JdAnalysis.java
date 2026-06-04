package com.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * JD分析结果表实体类
 * 对应数据库表：jd_analysis
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JdAnalysis {

    /**
     * JD分析ID
     */
    private Long id;

    /**
     * JD ID
     */
    private Long jdId;

    /**
     * 必备技能（JSON格式）
     */
    private String requiredSkills;

    /**
     * 加分技能（JSON格式）
     */
    private String preferredSkills;

    /**
     * 岗位职责（JSON格式）
     */
    private String responsibilities;

    /**
     * 关键词（JSON格式）
     */
    private String keywords;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}