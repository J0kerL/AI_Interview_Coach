package com.interview.mapper;

import com.interview.entity.JobDescriptions;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface JdMapper {

    void insert(@Param("jd") JobDescriptions jd);

    @Select("SELECT * FROM job_descriptions WHERE id = #{id}")
    JobDescriptions selectById(Long id);

    @Select("SELECT * FROM job_descriptions WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<JobDescriptions> selectByUserId(@Param("userId") long userId);

    @Select("SELECT COUNT(*) FROM job_descriptions WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") long userId);

    @Delete("DELETE FROM job_descriptions WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") long userId);

    int deleteByIdsAndUserId(@Param("ids") List<Long> ids, @Param("userId") long userId);

}
