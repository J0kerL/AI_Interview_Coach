package com.interview.mapper;

import com.interview.entity.JdAnalysis;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface JdAnalysisMapper {

    void insert(@Param("a") JdAnalysis analysis);

    @Select("SELECT * FROM jd_analysis WHERE jd_id = #{jdId}")
    JdAnalysis selectByJdId(@Param("jdId") Long jdId);

}
