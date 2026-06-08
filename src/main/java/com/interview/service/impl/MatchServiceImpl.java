package com.interview.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.exception.BusinessException;
import com.interview.dto.MatchAnalyzeDTO;
import com.interview.entity.JdAnalysis;
import com.interview.entity.JobDescriptions;
import com.interview.entity.ResumeJobMatches;
import com.interview.entity.ResumeProfiles;
import com.interview.entity.Resumes;
import com.interview.mapper.JdAnalysisMapper;
import com.interview.mapper.JdMapper;
import com.interview.mapper.MatchAnalysisMapper;
import com.interview.mapper.ResumeMapper;
import com.interview.mapper.ResumeProfileMapper;
import com.interview.model.response.MatchAnalysisResult;
import com.interview.service.MatchService;
import com.interview.service.llm.LlmGatewayService;
import com.interview.service.llm.PromptTemplateManager;
import com.interview.vo.MatchAnalysisVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
@Slf4j
@Service
public class MatchServiceImpl implements MatchService {

    @Resource
    private ResumeMapper resumeMapper;

    @Resource
    private ResumeProfileMapper resumeProfileMapper;

    @Resource
    private JdMapper jdMapper;

    @Resource
    private JdAnalysisMapper jdAnalysisMapper;

    @Resource
    private MatchAnalysisMapper matchAnalysisMapper;

    @Resource
    private LlmGatewayService llmGatewayService;

    @Resource
    private PromptTemplateManager promptTemplateManager;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 执行简历-JD 匹配分析
     */
    @Override
    public MatchAnalysisVO analyze(MatchAnalyzeDTO dto) {

        long userId = StpUtil.getLoginIdAsLong();
        Long resumeId = dto.getResumeId();
        Long jdId = dto.getJdId();

        // 1. 校验简历存在 + 归属权
        Resumes resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该简历");
        }

        // 2. 校验简历已解析
        ResumeProfiles profile = resumeProfileMapper.selectByResumeId(resumeId);
        if (profile == null) {
            throw new BusinessException("请先解析简历后再进行匹配分析");
        }

        // 3. 校验 JD 存在 + 归属权
        JobDescriptions jd = jdMapper.selectById(jdId);
        if (jd == null) {
            throw new BusinessException("JD 不存在");
        }
        if (!jd.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作该 JD");
        }

        // 4. 校验 JD 已解析
        JdAnalysis jdAnalysis = jdAnalysisMapper.selectByJdId(jdId);
        if (jdAnalysis == null) {
            throw new BusinessException("请先解析 JD 后再进行匹配分析");
        }

        // 5. 检查是否已有匹配结果（避免重复分析）
        ResumeJobMatches existing = matchAnalysisMapper.selectByResumeAndJd(resumeId, jdId);
        if (existing != null) {
            throw new BusinessException("该简历与 JD 已完成匹配分析，请勿重复操作");
        }

        try {
            // 6. 构建上下文：简历画像摘要 + JD 分析摘要 + JD 原文
            String resumeSummary = buildResumeSummary(profile);
            String jdSummary = buildJdSummary(jdAnalysis);
            String jdContent = StringUtils.hasText(jd.getContent()) ? jd.getContent() : "（无原始文本）";

            // 7. 构建 Prompt 并调用 LLM
            String prompt = promptTemplateManager.buildPrompt("match-analysis", Map.of(
                    "resumeProfile", resumeSummary,
                    "jdAnalysis", jdSummary,
                    "jdContent", jdContent
            ));
            MatchAnalysisResult result = llmGatewayService.call(prompt, MatchAnalysisResult.class);

            // 8. 写入 resume_job_matches 表
            ResumeJobMatches match = ResumeJobMatches.builder()
                    .resumeId(resumeId)
                    .jdId(jdId)
                    .overallScore(result.getOverallScore())
                    .skillScore(result.getSkillScore())
                    .experienceScore(result.getExperienceScore())
                    .strengths(toJson(result.getStrengths()))
                    .gaps(toJson(result.getGaps()))
                    .analysis(result.getAnalysis())
                    .build();
            matchAnalysisMapper.insert(match);

            log.info("匹配分析完成: resumeId={}, jdId={}", resumeId, jdId);

            // 9. 查询完整记录并返回
            ResumeJobMatches saved = matchAnalysisMapper.selectByResumeAndJd(resumeId, jdId);
            MatchAnalysisVO vo = new MatchAnalysisVO();
            BeanUtils.copyProperties(saved, vo);
            return vo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("匹配分析失败: resumeId={}, jdId={}", resumeId, jdId, e);
            throw new BusinessException("匹配分析失败：" + e.getMessage());
        }
    }

    /**
     * 获取某份简历的所有匹配分析结果
     */
    @Override
    public List<MatchAnalysisVO> getMatchList(Long resumeId) {

        long userId = StpUtil.getLoginIdAsLong();

        // 校验简历归属
        Resumes resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看");
        }

        List<ResumeJobMatches> list = matchAnalysisMapper.selectByResumeId(resumeId);
        return list.stream().map(m -> {
            MatchAnalysisVO vo = new MatchAnalysisVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).toList();
    }

    /**
     * 获取指定简历和JD的匹配分析结果
     */
    @Override
    public MatchAnalysisVO getMatchResult(Long resumeId, Long jdId) {

        long userId = StpUtil.getLoginIdAsLong();

        // 校验简历归属
        Resumes resume = resumeMapper.selectById(resumeId);
        if (resume == null) {
            throw new BusinessException("简历不存在");
        }
        if (!resume.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看");
        }

        ResumeJobMatches match = matchAnalysisMapper.selectByResumeAndJd(resumeId, jdId);
        if (match == null) {
            throw new BusinessException("该简历与 JD 尚未进行匹配分析");
        }

        MatchAnalysisVO vo = new MatchAnalysisVO();
        BeanUtils.copyProperties(match, vo);
        return vo;
    }

    /**
     * 构建简历画像摘要文本
     */
    private String buildResumeSummary(ResumeProfiles profile) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(profile.getCandidateName())) {
            sb.append("姓名：").append(profile.getCandidateName()).append("\n");
        }
        if (profile.getExperienceYears() != null) {
            sb.append("工作年限：").append(profile.getExperienceYears()).append("年\n");
        }
        if (StringUtils.hasText(profile.getSummary())) {
            sb.append("个人总结：").append(profile.getSummary()).append("\n");
        }
        if (StringUtils.hasText(profile.getSkills())) {
            sb.append("技能标签：").append(profile.getSkills()).append("\n");
        }
        if (StringUtils.hasText(profile.getWorkExperiences())) {
            sb.append("工作经历：").append(profile.getWorkExperiences()).append("\n");
        }
        if (StringUtils.hasText(profile.getProjectExperiences())) {
            sb.append("项目经历：").append(profile.getProjectExperiences()).append("\n");
        }
        if (StringUtils.hasText(profile.getEducation())) {
            sb.append("教育经历：").append(profile.getEducation());
        }
        return sb.toString();
    }

    /**
     * 构建 JD 分析摘要文本
     */
    private String buildJdSummary(JdAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(analysis.getRequiredSkills())) {
            sb.append("必备技能：").append(analysis.getRequiredSkills()).append("\n");
        }
        if (StringUtils.hasText(analysis.getPreferredSkills())) {
            sb.append("加分技能：").append(analysis.getPreferredSkills()).append("\n");
        }
        if (StringUtils.hasText(analysis.getResponsibilities())) {
            sb.append("岗位职责：").append(analysis.getResponsibilities()).append("\n");
        }
        if (StringUtils.hasText(analysis.getKeywords())) {
            sb.append("关键词：").append(analysis.getKeywords());
        }
        return sb.toString();
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
