package com.interview.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试会话VO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class InterviewSessionVO {

    private Long id;
    private Long resumeId;
    private Long jdId;
    private String sessionType;
    private String status;
    private Integer totalQuestions;
    private BigDecimal overallScore;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;

}
