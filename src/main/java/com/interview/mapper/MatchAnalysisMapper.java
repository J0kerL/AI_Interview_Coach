package com.interview.mapper;

import com.interview.entity.ResumeJobMatches;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface MatchAnalysisMapper {

    void insert(@Param("m") ResumeJobMatches match);

    @Select("SELECT * FROM resume_job_matches WHERE resume_id = #{resumeId} AND jd_id = #{jdId}")
    ResumeJobMatches selectByResumeAndJd(@Param("resumeId") Long resumeId, @Param("jdId") Long jdId);

    @Select("SELECT * FROM resume_job_matches WHERE resume_id = #{resumeId} ORDER BY created_at DESC")
    List<ResumeJobMatches> selectByResumeId(@Param("resumeId") Long resumeId);

}
