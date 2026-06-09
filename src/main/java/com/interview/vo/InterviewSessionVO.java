package com.interview.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    /**
     * 面试模式：text-纯文字, voice-语音面试
     */
    private String mode;
    private Integer totalQuestions;
    private BigDecimal overallScore;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;

}
