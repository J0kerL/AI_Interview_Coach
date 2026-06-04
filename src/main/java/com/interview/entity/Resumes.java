package com.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户简历表实体类
 * 对应数据库表：resumes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Resumes {

    /**
     * 简历ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 简历名称
     */
    private String resumeName;

    /**
     * 简历文件地址
     */
    private String fileUrl;

    /**
     * 解析状态：0待解析 1解析中 2成功 3失败
     */
    private Integer parseStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 逻辑删除时间
     */
    private LocalDateTime deletedAt;
}