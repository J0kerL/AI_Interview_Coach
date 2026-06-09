package com.interview.mapper;

import com.interview.entity.InterviewQuestions;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface InterviewQuestionMapper {

    void batchInsert(@Param("list") List<InterviewQuestions> questions);

    void insert(@Param("question") InterviewQuestions question);

    @Select("SELECT * FROM interview_questions WHERE session_id = #{sessionId} ORDER BY sequence_no ASC")
    List<InterviewQuestions> selectBySessionId(@Param("sessionId") Long sessionId);

    @Select("SELECT * FROM interview_questions WHERE id = #{id}")
    InterviewQuestions selectById(Long id);

}
