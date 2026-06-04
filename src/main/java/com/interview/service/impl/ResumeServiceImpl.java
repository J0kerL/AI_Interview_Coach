package com.interview.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.interview.common.exception.BusinessException;
import com.interview.entity.Resumes;
import com.interview.mapper.ResumeMapper;
import com.interview.service.FileService;
import com.interview.service.ResumeService;
import com.interview.vo.ResumeVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
@Slf4j
@Service
public class ResumeServiceImpl implements ResumeService {

    private static final String FOLDER_NAME = "resume";

    /**
     * 每个用户最多上传简历数量
     */
    private static final int MAX_RESUME_COUNT = 5;

    @Resource
    private FileService fileService;

    @Resource
    private ResumeMapper resumeMapper;

    /**
     * 上传简历
     */
    @Override
    public ResumeVO uploadResume(MultipartFile file) {

        long userId = StpUtil.getLoginIdAsLong();

        // 1. 校验简历数量上限
        int count = resumeMapper.countByUserId(userId);
        if (count >= MAX_RESUME_COUNT) {
            throw new BusinessException("简历数量已达上限（最多" + MAX_RESUME_COUNT + "份）");
        }

        // 2. 上传 PDF 到 OSS
        String fileUrl = fileService.uploadPdf(file, FOLDER_NAME);

        // 3. 插入数据库（失败则回滚 OSS 文件）
        Resumes resume = Resumes.builder()
                .userId(userId)
                .resumeName(file.getOriginalFilename())
                .fileUrl(fileUrl)
                .build();
        try {
            resumeMapper.insert(resume);
        } catch (Exception e) {
            log.error("简历入库失败，回滚 OSS 文件: {}", fileUrl, e);
            fileService.deleteFile(fileUrl);
            throw new BusinessException("简历上传失败，请重试");
        }

        // 4. 查询完整记录并返回
        Resumes savedResume = resumeMapper.selectById(resume.getId());
        ResumeVO resumeVO = new ResumeVO();
        BeanUtils.copyProperties(savedResume, resumeVO);
        return resumeVO;
    }

}
