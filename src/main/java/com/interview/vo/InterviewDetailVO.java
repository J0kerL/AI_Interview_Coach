package com.interview.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试详情VO（包含完整问答）
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class InterviewDetailVO {

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
    /** 面试模式：text/voice */
    private String mode;

    /**
     * 面试问答列表
     */
    private List<QAPair> qaPairs;

    @Data
    public static class QAPair {
        /** 问题ID */
        private Long questionId;
        /** 父问题ID（追问时有值） */
        private Long parentQuestionId;
        /** 题目类型 */
        private String questionType;
        /** 题目序号 */
        private Integer sequenceNo;
        /** 问题内容 */
        private String questionText;
        /** 问题语音 URL（voice 模式时有值） */
        private String questionAudioUrl;
        /** 回答内容（未回答时为 null） */
        private String answerText;
    }

}
