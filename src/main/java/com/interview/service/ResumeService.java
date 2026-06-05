package com.interview.service;

import com.interview.vo.ResumeVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
public interface ResumeService {

    /**
     * 上传简历
     */
    ResumeVO uploadResume(MultipartFile file);

    /**
     * 获取简历列表
     */
    List<ResumeVO> getResumeList();

    /**
     * 根据id删除简历
     */
    void deleteById(Long id);

    /**
     * 根据ids批量删除简历
     */
    void deleteByIds(List<Long> ids);
}
