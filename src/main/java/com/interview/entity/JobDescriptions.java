package com.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 岗位JD表实体类
 * 对应数据库表：job_descriptions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDescriptions {

    /**
     * 岗位JD ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 岗位名称
     */
    private String title;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * JD来源链接
     */
    private String sourceUrl;

    /**
     * JD原始内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}