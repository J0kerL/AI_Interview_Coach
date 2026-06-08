package com.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 面试问题表实体类
 * 对应数据库表：interview_questions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewQuestions {

    /**
     * 问题ID
     */
    private Long id;

    /**
     * 面试会话ID
     */
    private Long sessionId;

    /**
     * 父问题ID（用于追问）
     */
    private Long parentQuestionId;

    /**
     * 题目类型：intro-自我介绍, technical-技术题, behavior-行为题, project-项目题, followup-追问
     */
    private String questionType;

    /**
     * 题目序号
     */
    private Integer sequenceNo;

    /**
     * 问题内容
     */
    private String questionText;

    /**
     * AI出题原因
     */
    private String aiReason;

    /**
     * 问题语音 URL（voice 模式）
     */
    private String questionAudioUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}