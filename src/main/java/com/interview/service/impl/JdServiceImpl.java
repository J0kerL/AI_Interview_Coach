package com.interview.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.exception.BusinessException;
import com.interview.dto.CreateJdDTO;
import com.interview.entity.JdAnalysis;
import com.interview.entity.JobDescriptions;
import com.interview.mapper.JdAnalysisMapper;
import com.interview.mapper.JdMapper;
import com.interview.model.response.JdParseResult;
import com.interview.service.JdService;
import com.interview.service.llm.LlmCallOptions;
import com.interview.service.llm.LlmGatewayService;
import com.interview.service.llm.PromptTemplateManager;
import com.interview.vo.JdAnalysisVO;
import com.interview.vo.JdVO;
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
public class JdServiceImpl implements JdService {

    /**
     * 每个用户最多创建 JD 数量
     */
    private static final int MAX_JD_COUNT = 20;

    @Resource
    private JdMapper jdMapper;

    @Resource
    private JdAnalysisMapper jdAnalysisMapper;

    @Resource
    private LlmGatewayService llmGatewayService;

    @Resource
    private PromptTemplateManager promptTemplateManager;

    @Resource
    private ObjectMapper objectMapper;

    /**
     * 创建 JD
     */
    @Override
    public JdVO createJd(CreateJdDTO dto) {

        long userId = StpUtil.getLoginIdAsLong();

        // 1. content 和 sourceUrl 至少填一个
        if (!StringUtils.hasText(dto.getContent()) && !StringUtils.hasText(dto.getSourceUrl())) {
            throw new BusinessException("JD 内容和来源链接至少填写一个");
        }

        // 2. 校验数量上限
        int count = jdMapper.countByUserId(userId);
        if (count >= MAX_JD_COUNT) {
            throw new BusinessException("JD 数量已达上限（最多" + MAX_JD_COUNT + "个）");
        }

        // 3. 插入数据库
        JobDescriptions jd = JobDescriptions.builder()
                .userId(userId)
                .title(dto.getTitle())
                .companyName(dto.getCompanyName())
                .sourceUrl(StringUtils.hasText(dto.getSourceUrl()) ? dto.getSourceUrl() : null)
                .content(StringUtils.hasText(dto.getContent()) ? dto.getContent() : null)
                .build();
        jdMapper.insert(jd);

        // 4. 查询完整记录并返回
        JobDescriptions savedJd = jdMapper.selectById(jd.getId());
        JdVO jdVO = new JdVO();
        BeanUtils.copyProperties(savedJd, jdVO);
        return jdVO;
    }

    /**
     * 获取当前用户的 JD 列表
     */
    @Override
    public List<JdVO> getJdList() {
        long userId = StpUtil.getLoginIdAsLong();
        List<JobDescriptions> list = jdMapper.selectByUserId(userId);
        return list.stream().map(jd -> {
            JdVO jdVO = new JdVO();
            BeanUtils.copyProperties(jd, jdVO);
            return jdVO;
        }).toList();
    }

    /**
     * 获取 JD 详情
     */
    @Override
    public JdVO getJdDetail(Long id) {
        long userId = StpUtil.getLoginIdAsLong();

        JobDescriptions jd = jdMapper.selectById(id);
        if (jd == null) {
            throw new BusinessException("JD 不存在");
        }
        if (!jd.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看");
        }

        JdVO jdVO = new JdVO();
        BeanUtils.copyProperties(jd, jdVO);
        return jdVO;
    }

    /**
     * 删除 JD
     */
    @Override
    public void deleteJd(Long id) {
        long userId = StpUtil.getLoginIdAsLong();

        int rows = jdMapper.deleteByIdAndUserId(id, userId);
        if (rows == 0) {
            throw new BusinessException("JD 不存在或无权限删除");
        }
    }

    /**
     * 批量删除 JD
     */
    @Override
    public void deleteJdByIds(List<Long> ids) {

        // 1. 参数校验
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("删除列表不能为空");
        }

        long userId = StpUtil.getLoginIdAsLong();

        // 2. 一条 SQL 批量删除（WHERE user_id = ? AND id IN (...)）
        int rows = jdMapper.deleteByIdsAndUserId(ids, userId);
        if (rows == 0) {
            throw new BusinessException("未找到可删除的 JD");
        }
    }

    /**
     * AI 解析 JD
     */
    @Override
    public void parseJd(Long jdId) {

        long userId = StpUtil.getLoginIdAsLong();

        // 1. 查询 JD + 校验归属权
        JobDescriptions jd = jdMapper.selectById(jdId);
        if (jd == null) {
            throw new BusinessException("JD 不存在");
        }
        if (!jd.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }

        // 2. 校验 JD 有文本内容可解析
        if (!StringUtils.hasText(jd.getContent())) {
            throw new BusinessException("该 JD 没有文本内容，无法解析");
        }

        // 3. 检查是否已有解析结果（避免重复解析）
        JdAnalysis existing = jdAnalysisMapper.selectByJdId(jdId);
        if (existing != null) {
            throw new BusinessException("该 JD 已解析，请勿重复操作");
        }

        try {
            // 4. 构建 Prompt 并调用 LLM
            String prompt = promptTemplateManager.buildPrompt("jd-parse",
                    Map.of("jdText", jd.getContent()));
            JdParseResult result = llmGatewayService.call(prompt, JdParseResult.class,
                    LlmCallOptions.extraction());

            // 5. 将结果写入 jd_analysis 表
            JdAnalysis analysis = JdAnalysis.builder()
                    .jdId(jdId)
                    .requiredSkills(toJson(result.getRequiredSkills()))
                    .preferredSkills(toJson(result.getPreferredSkills()))
                    .responsibilities(toJson(result.getResponsibilities()))
                    .keywords(toJson(result.getKeywords()))
                    .build();
            jdAnalysisMapper.insert(analysis);

            log.info("JD 解析成功: id={}", jdId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("JD 解析失败: id={}", jdId, e);
            throw new BusinessException("JD 解析失败：" + e.getMessage());
        }
    }

    /**
     * 获取 JD 解析结果
     */
    @Override
    public JdAnalysisVO getJdAnalysis(Long jdId) {

        long userId = StpUtil.getLoginIdAsLong();

        // 1. 校验 JD 归属
        JobDescriptions jd = jdMapper.selectById(jdId);
        if (jd == null) {
            throw new BusinessException("JD 不存在");
        }
        if (!jd.getUserId().equals(userId)) {
            throw new BusinessException("无权限查看");
        }

        // 2. 查询解析结果
        JdAnalysis analysis = jdAnalysisMapper.selectByJdId(jdId);
        if (analysis == null) {
            throw new BusinessException("该 JD 尚未解析");
        }

        // 3. 转为 VO
        JdAnalysisVO vo = new JdAnalysisVO();
        BeanUtils.copyProperties(analysis, vo);
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
