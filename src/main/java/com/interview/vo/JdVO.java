package com.interview.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * JD 信息VO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class JdVO {

    /**
     * JD ID
     */
    private Long id;

    /**
     * 岗位名称
     */
    private String title;

    /**
     * 公司名称
     */
    private String companyName;

    /**
     * JD 来源链接
     */
    private String sourceUrl;

    /**
     * JD 原始内容
     */
    private String content;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

}
