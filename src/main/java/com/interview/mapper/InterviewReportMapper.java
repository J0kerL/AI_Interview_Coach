package com.interview.mapper;

import com.interview.entity.InterviewReports;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface InterviewReportMapper {

    void insert(@Param("r") InterviewReports report);

    @Select("SELECT * FROM interview_reports WHERE session_id = #{sessionId}")
    InterviewReports selectBySessionId(@Param("sessionId") Long sessionId);

}
