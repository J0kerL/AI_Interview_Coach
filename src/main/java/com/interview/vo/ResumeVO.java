package com.interview.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
@Data
public class ResumeVO {

    private Long id;
    private String resumeName;
    private String fileUrl;
    private Integer parseStatus;
    private LocalDateTime createdAt;

}
