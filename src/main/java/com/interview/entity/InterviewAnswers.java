package com.interview.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 面试回答表实体类
 * 对应数据库表：interview_answers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterviewAnswers {

    /**
     * 回答ID
     */
    private Long id;

    /**
     * 问题ID
     */
    private Long questionId;

    /**
     * 回答内容
     */
    private String answerText;

    /**
     * 录音地址
     */
    private String audioUrl;

    /**
     * 回答时长（秒）
     */
    private Integer durationSeconds;

    /**
     * 语音转文本结果
     */
    private String transcript;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}