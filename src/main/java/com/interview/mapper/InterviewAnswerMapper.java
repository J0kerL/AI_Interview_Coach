package com.interview.mapper;

import com.interview.entity.InterviewAnswers;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface InterviewAnswerMapper {

    @Insert("INSERT INTO interview_answers (question_id, answer_text, audio_url, duration_seconds) " +
            "VALUES (#{questionId}, #{answerText}, #{audioUrl}, #{durationSeconds})")
    void insert(InterviewAnswers answer);

    @Select("SELECT a.* FROM interview_answers a " +
            "JOIN interview_questions q ON a.question_id = q.id " +
            "WHERE q.session_id = #{sessionId} ORDER BY q.sequence_no ASC")
    List<InterviewAnswers> selectBySessionId(@Param("sessionId") Long sessionId);

    @Select("SELECT * FROM interview_answers WHERE question_id = #{questionId}")
    InterviewAnswers selectByQuestionId(@Param("questionId") Long questionId);

    @Update("UPDATE interview_answers SET audio_url = #{audioUrl} WHERE id = #{id}")
    void updateAudioUrl(@Param("id") Long id, @Param("audioUrl") String audioUrl);

}
