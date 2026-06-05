package com.interview.mapper;

import com.interview.entity.Resumes;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
public interface ResumeMapper {

    @Insert("INSERT INTO resumes (user_id, resume_name, file_url) VALUES (#{resume.userId}, #{resume.resumeName}, #{resume.fileUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(@Param("resume") Resumes resume);

    @Select("SELECT * FROM resumes WHERE id = #{id} AND deleted_at IS NULL")
    Resumes selectById(Long id);

    @Select("SELECT COUNT(*) FROM resumes WHERE user_id = #{userId} AND deleted_at IS NULL")
    int countByUserId(@Param("userId") long userId);

    @Select("SELECT * FROM resumes WHERE user_id = #{userId} AND deleted_at IS NULL ORDER BY created_at DESC")
    List<Resumes> selectListByUserId(long userId);

    @Update("UPDATE resumes SET deleted_at = NOW() WHERE id = #{id}")
    void deleteById(Long id);

    List<Resumes> selectByIds(@Param("ids") List<Long> ids);

    void deleteByIds(@Param("ids") List<Long> ids);
}
