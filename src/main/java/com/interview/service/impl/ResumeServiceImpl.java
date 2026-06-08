package com.interview.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.exception.BusinessException;
import com.interview.entity.ResumeProfiles;
import com.interview.entity.Resumes;
import com.interview.mapper.ResumeMapper;
import com.interview.mapper.ResumeProfileMapper;
import com.interview.model.response.ResumeParseResult;
import com.interview.service.FileService;
import com.interview.service.PdfParseService;
import com.interview.service.ResumeService;
import com.interview.service.llm.LlmCallOptions;
import com.interview.service.llm.LlmGatewayService;
import com.interview.service.llm.PromptTemplateManager;
import com.interview.vo.ResumeProfileVO;
import com.interview.vo.ResumeVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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

    /**
     * 简历解析状态常量
     */
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_PARSING = 1;
    private static final int STATUS_SUCCESS = 2;
    private static final int STATUS_FAILED = 3;

    @Resource
    private FileService fileService;

    @Resource
    private ResumeMapper resumeMapper;

    @Resource
    private ResumeProfileMapper resumeProfileMapper;

    @Resource
    private PdfParseService pdfParseService;

    @Resource
    private LlmGatewayService llmGatewayService;

    @Resource
    private PromptTemplateManager promptTemplateManager;

    @Resource
    private ObjectMapper objectMapper;

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

    /**
     * 获取简历列表
     */
    @Override
    public List<ResumeVO> getResumeList() {

        // 1. 获取当前用户ID
        long userId = StpUtil.getLoginIdAsLong();

        // 2. 查询简历列表
        List<Resumes> list = resumeMapper.selectListByUserId(userId);

        // 3. 循环转为VO列表
        return list.stream().map(resume -> {
            ResumeVO resumeVO = new ResumeVO();
            BeanUtils.copyProperties(resume, resumeVO);
            return resumeVO;
        }).toList();
    }

    /**
     * 根据id删除简历
     */
    @Override
    public void deleteById(Long id) {

        Resumes resume = resumeMapper.selectById(id);
        // 1. 判断简历是否存在
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }

        // 2. 判断简历是否属于当前用户
        if (!resume.getUserId().equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("无权限删除");
        }

        // 3. 逻辑删除
        resumeMapper.deleteById(id);

        // 4. 清理 OSS 文件
        if (resume.getFileUrl() != null) {
            try {
                fileService.deleteFile(resume.getFileUrl());
            } catch (Exception e) {
                log.warn("OSS 文件删除失败: {}", resume.getFileUrl(), e);
            }
        }
    }

    /**
     * 根据ids批量删除简历
     */
    @Override
    public void deleteByIds(List<Long> ids) {

        // 1. 参数校验
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("删除列表不能为空");
        }

        long userId = StpUtil.getLoginIdAsLong();

        // 2. 批量查询简历（一条SQL）
        List<Resumes> resumes = resumeMapper.selectByIds(ids);
        if (resumes.isEmpty()) {
            throw new BusinessException("未找到可删除的简历");
        }

        // 3. 校验所有简历是否属于当前用户
        for (Resumes resume : resumes) {
            if (!resume.getUserId().equals(userId)) {
                throw new BusinessException("无权限删除简历 id=" + resume.getId());
            }
        }

        // 4. 批量逻辑删除（一条SQL）
        List<Long> validIds = resumes.stream().map(Resumes::getId).toList();
        resumeMapper.deleteByIds(validIds);

        // 5. 清理 OSS 文件
        for (Resumes resume : resumes) {
            if (resume.getFileUrl() != null) {
                try {
                    fileService.deleteFile(resume.getFileUrl());
                } catch (Exception e) {
                    log.warn("OSS 文件删除失败: {}", resume.getFileUrl(), e);
                }
            }
        }
    }

    /**
     * 解析简历（调用 LLM）
     */
    @Override
    public void parseResume(Long resumeId) {

        long userId = StpUtil.getLoginIdAsLong();

        // 1. 查询简历 + 校验归属权
        Resumes resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        if (resume.getParseStatus() != null && resume.getParseStatus() == STATUS_PARSING) {
            throw new BusinessException("简历正在解析中，请勿重复操作");
        }

        // 2. 更新状态为"解析中"
        resumeMapper.updateParseStatus(resumeId, STATUS_PARSING);

        try {
            // 3. 从 OSS 下载 PDF 并提取文本
            String pdfText = pdfParseService.extractTextFromUrl(resume.getFileUrl());

            // 4. 构建 Prompt 并调用 LLM
            String prompt = promptTemplateManager.buildPrompt("resume-parse",
                    Map.of("resumeText", pdfText));
            ResumeParseResult result = llmGatewayService.call(prompt, ResumeParseResult.class,
                    LlmCallOptions.extraction());

            // 5. 将 LLM 结果转为 JSON 字符串，写入 resume_profiles
            ResumeProfiles profile = ResumeProfiles.builder()
                    .resumeId(resumeId)
                    .candidateName(result.getCandidateName())
                    .experienceYears(result.getExperienceYears() != null
                            ? BigDecimal.valueOf(result.getExperienceYears()) : null)
                    .summary(result.getSummary())
                    .skills(toJson(result.getSkills()))
                    .workExperiences(toJson(result.getWorkExperiences()))
                    .projectExperiences(toJson(result.getProjectExperiences()))
                    .education(toJson(result.getEducation()))
                    .build();
            resumeProfileMapper.insert(profile);

            // 6. 更新状态为"解析成功"
            resumeMapper.updateParseStatus(resumeId, STATUS_SUCCESS);
            log.info("简历解析成功: id={}", resumeId);

        } catch (Exception e) {
            // 7. 任意步骤失败 → 标记为"解析失败"
            log.error("简历解析失败: id={}", resumeId, e);
            resumeMapper.updateParseStatus(resumeId, STATUS_FAILED);
            throw new BusinessException("简历解析失败：" + e.getMessage());
        }
    }

    /**
     * 获取简历解析结果
     */
    @Override
    public ResumeProfileVO getProfile(Long resumeId) {

        long userId = StpUtil.getLoginIdAsLong();

        // 1. 校验简历归属
        Resumes resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看");
        }

        // 2. 查询解析结果
        ResumeProfiles profile = resumeProfileMapper.selectByResumeId(resumeId);
        if (profile == null) {
            throw new BusinessException("该简历尚未解析");
        }

        // 3. 转为 VO
        ResumeProfileVO vo = new ResumeProfileVO();
        BeanUtils.copyProperties(profile, vo);
        return vo;
    }

    /**
     * 对象转 JSON 字符串
     */
    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.warn("JSON 序列化失败", e);
            return null;
        }
    }

}
