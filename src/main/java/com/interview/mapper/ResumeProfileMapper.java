package com.interview.mapper;

import com.interview.entity.ResumeProfiles;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface ResumeProfileMapper {

    void insert(@Param("p") ResumeProfiles profile);

    @Select("SELECT * FROM resume_profiles WHERE resume_id = #{resumeId}")
    ResumeProfiles selectByResumeId(@Param("resumeId") Long resumeId);

}
