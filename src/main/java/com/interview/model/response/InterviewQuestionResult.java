package com.interview.model.response;

import lombok.Data;

import java.util.List;

/**
 * 面试题目生成结果（LLM 结构化输出）
 *
 * @Author Diamond
 * @Create 2026/6/4
 */
@Data
public class InterviewQuestionResult {

    /**
     * 生成的面试题列表
     */
    private List<Question> questions;

    @Data
    public static class Question {
        /** 题目序号 */
        private Integer sequenceNo;
        /** 题目类型：intro/technical/behavior/project/followup */
        private String questionType;
        /** 问题内容 */
        private String questionText;
        /** AI 出题原因 */
        private String aiReason;
    }

}
