package com.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 面试会话表实体类
 * 对应数据库表：interview_sessions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewSessions {

    /**
     * 面试会话ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联简历ID
     */
    private Long resumeId;

    /**
     * 关联JD ID
     */
    private Long jdId;

    /**
     * 面试类型：resume-基于简历, job-基于岗位, mixed-混合
     */
    private String sessionType;

    /**
     * 面试状态：pending-待开始, running-进行中, completed-已完成, terminated-已终止
     */
    private String status;

    /**
     * 面试模式：text-纯文字, voice-语音面试
     */
    private String mode;

    /**
     * AI面试官开场白
     */
    private String greeting;

    /**
     * 开场白语音URL（voice模式时有值）
     */
    private String greetingAudioUrl;

    /**
     * 题目总数
     */
    private Integer totalQuestions;

    /**
     * 最终评分
     */
    private BigDecimal overallScore;

    /**
     * 开始时间
     */
    private LocalDateTime startedAt;

    /**
     * 结束时间
     */
    private LocalDateTime endedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}