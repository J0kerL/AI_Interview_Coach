package com.interview.service;

import com.interview.vo.ResumeVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
public interface ResumeService {

    /**
     * 上传简历
     */
    ResumeVO uploadResume(MultipartFile file);

}
