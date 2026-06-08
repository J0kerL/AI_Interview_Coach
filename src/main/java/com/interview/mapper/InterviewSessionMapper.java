package com.interview.mapper;

import com.interview.entity.InterviewSessions;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface InterviewSessionMapper {

    void insert(@Param("s") InterviewSessions session);

    @Select("SELECT * FROM interview_sessions WHERE id = #{id}")
    InterviewSessions selectById(Long id);

    @Select("SELECT * FROM interview_sessions WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<InterviewSessions> selectByUserId(@Param("userId") long userId);

    @Update("UPDATE interview_sessions SET status = #{status}, total_questions = #{totalQuestions}, " +
            "started_at = NOW() WHERE id = #{id}")
    void updateRunning(@Param("id") Long id, @Param("status") String status,
                       @Param("totalQuestions") int totalQuestions);

    @Update("UPDATE interview_sessions SET status = #{status}, ended_at = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("UPDATE interview_sessions SET overall_score = #{score} WHERE id = #{id}")
    void updateScore(@Param("id") Long id, @Param("score") BigDecimal score);

}
