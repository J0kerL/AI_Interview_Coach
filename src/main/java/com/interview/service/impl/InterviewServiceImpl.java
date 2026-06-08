package com.interview.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.exception.BusinessException;
import com.interview.dto.StartInterviewDTO;
import com.interview.dto.SubmitAnswerDTO;
import com.interview.entity.*;
import com.interview.mapper.*;
import com.interview.model.response.FollowUpResult;
import com.interview.model.response.InterviewQuestionResult;
import com.interview.model.response.InterviewReportResult;
import com.interview.service.AsrService;
import com.interview.service.FileService;
import com.interview.service.InterviewService;
import com.interview.service.TtsService;
import com.interview.service.llm.LlmCallOptions;
import com.interview.service.llm.LlmGatewayService;
import com.interview.service.llm.PromptTemplateManager;
import com.interview.vo.InterviewDetailVO;
import com.interview.vo.InterviewReportVO;
import com.interview.vo.InterviewSessionVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
@Slf4j
@Service
public class InterviewServiceImpl implements InterviewService {

    @Resource
    private InterviewSessionMapper sessionMapper;

    @Resource
    private InterviewQuestionMapper questionMapper;

    @Resource
    private InterviewAnswerMapper answerMapper;

    @Resource
    private InterviewReportMapper reportMapper;

    @Resource
    private ResumeMapper resumeMapper;

    @Resource
    private ResumeProfileMapper resumeProfileMapper;

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

    @Resource
    private AsrService asrService;

    @Resource
    private FileService fileService;

    @Resource
    private TtsService ttsService;

    private static final String AUDIO_FOLDER = "interview-audio";

    // ==================== 开始面试 ====================

    @Override
    public InterviewDetailVO startInterview(StartInterviewDTO dto) {

        long userId = StpUtil.getLoginIdAsLong();
        String sessionType = dto.getSessionType();

        // 1. 校验面试类型 + 关联数据
        validateSessionType(dto);

        // 2. 构建上下文信息
        String contextInfo = buildContextInfo(dto);

        // 3. 创建面试会话（pending 状态）
        InterviewSessions session = InterviewSessions.builder()
                .userId(userId)
                .resumeId(dto.getResumeId())
                .jdId(dto.getJdId())
                .sessionType(sessionType)
                .status("pending")
                .mode(dto.getMode())
                .build();
        sessionMapper.insert(session);

        try {
            // 4. 调用 LLM 生成首轮题目
            String prompt = promptTemplateManager.buildPrompt("question-generate", Map.of(
                    "interviewType", sessionType,
                    "contextInfo", contextInfo,
                    "questionCount", dto.getQuestionCount()
            ));
            InterviewQuestionResult result = llmGatewayService.call(prompt, InterviewQuestionResult.class,
                    LlmCallOptions.creative());

            // 5. 语音模式：为每道题生成 TTS 音频
            boolean isVoiceMode = "voice".equals(dto.getMode());
            List<InterviewQuestions> questions = new ArrayList<>();
            for (int i = 0; i < result.getQuestions().size(); i++) {
                InterviewQuestionResult.Question q = result.getQuestions().get(i);
                String audioUrl = null;
                if (isVoiceMode) {
                    try {
                        audioUrl = ttsService.synthesize(q.getQuestionText());
                    } catch (Exception ex) {
                        log.warn("TTS 生成失败，跳过音频: {}", ex.getMessage());
                    }
                }
                questions.add(InterviewQuestions.builder()
                        .sessionId(session.getId())
                        .questionType(q.getQuestionType())
                        .sequenceNo(i + 1)
                        .questionText(q.getQuestionText())
                        .aiReason(q.getAiReason())
                        .questionAudioUrl(audioUrl)
                        .build());
            }
            questionMapper.batchInsert(questions);

            // 6. 更新会话状态为 running
            sessionMapper.updateRunning(session.getId(), "running", questions.size());

            log.info("面试开始: sessionId={}, 题目数={}", session.getId(), questions.size());

        } catch (Exception e) {
            log.error("生成面试题目失败: sessionId={}", session.getId(), e);
            sessionMapper.updateStatus(session.getId(), "terminated");
            throw new BusinessException("生成面试题目失败：" + e.getMessage());
        }

        // 7. 返回面试详情
        return getInterviewDetail(session.getId());
    }

    // ==================== 提交回答 + AI 追问 ====================

    @Override
    public InterviewDetailVO.QAPair submitAnswer(Long sessionId, SubmitAnswerDTO dto) {

        long userId = StpUtil.getLoginIdAsLong();
        InterviewSessions session = validateSessionOwnership(sessionId, userId);

        if (!"running".equals(session.getStatus())) {
            throw new BusinessException("面试未在进行中");
        }

        // 1. 校验问题属于当前会话
        InterviewQuestions question = questionMapper.selectById(dto.getQuestionId());
        if (question == null || !question.getSessionId().equals(sessionId)) {
            throw new BusinessException("问题不存在或不属于当前面试");
        }

        // 2. 检查是否已回答
        InterviewAnswers existing = answerMapper.selectByQuestionId(dto.getQuestionId());
        if (existing != null) {
            throw new BusinessException("该问题已回答，请勿重复提交");
        }

        // 3. 保存回答
        InterviewAnswers answer = InterviewAnswers.builder()
                .questionId(dto.getQuestionId())
                .answerText(dto.getAnswerText())
                .build();
        answerMapper.insert(answer);

        // 4. AI 判断是否追问
        try {
            String chatHistory = buildChatHistory(sessionId);
            String resumeSummary = buildResumeSummaryForSession(session);

            String prompt = promptTemplateManager.buildPrompt("followup-generate", Map.of(
                    "resumeSummary", resumeSummary,
                    "chatHistory", chatHistory,
                    "lastQuestion", question.getQuestionText(),
                    "lastAnswer", dto.getAnswerText()
            ));
            FollowUpResult followUp = llmGatewayService.call(prompt, FollowUpResult.class,
                    LlmCallOptions.creative());

            if (Boolean.TRUE.equals(followUp.getNeedFollowUp()) && StringUtils.hasText(followUp.getFollowUpQuestion())) {
                // 生成追问
                List<InterviewQuestions> questions = questionMapper.selectBySessionId(sessionId);
                int nextSeqNo = questions.size() + 1;

                InterviewQuestions followUpQuestion = InterviewQuestions.builder()
                        .sessionId(sessionId)
                        .parentQuestionId(question.getId())
                        .questionType("followup")
                        .sequenceNo(nextSeqNo)
                        .questionText(followUp.getFollowUpQuestion())
                        .aiReason(followUp.getReason())
                        .build();

                // 语音模式：为追问生成 TTS
                if ("voice".equals(session.getMode())) {
                    try {
                        String audioUrl = ttsService.synthesize(followUp.getFollowUpQuestion());
                        followUpQuestion.setQuestionAudioUrl(audioUrl);
                    } catch (Exception ex) {
                        log.warn("追问 TTS 生成失败: {}", ex.getMessage());
                    }
                }

                questionMapper.batchInsert(List.of(followUpQuestion));

                // 更新题目总数
                sessionMapper.updateRunning(sessionId, "running", nextSeqNo);

                // 返回追问
                InterviewDetailVO.QAPair qaPair = new InterviewDetailVO.QAPair();
                qaPair.setQuestionId(followUpQuestion.getId());
                qaPair.setParentQuestionId(question.getId());
                qaPair.setQuestionType("followup");
                qaPair.setSequenceNo(nextSeqNo);
                qaPair.setQuestionText(followUp.getFollowUpQuestion());
                qaPair.setQuestionAudioUrl(followUpQuestion.getQuestionAudioUrl());
                return qaPair;
            }

        } catch (Exception e) {
            log.warn("AI 追问判断失败，跳过追问: sessionId={}", sessionId, e);
        }

        // 不追问，返回 null
        return null;
    }

    // ==================== 语音回答 ====================

    @Override
    public InterviewDetailVO.QAPair submitVoiceAnswer(Long sessionId, Long questionId, MultipartFile audioFile) {

        // 1. 上传音频到 OSS
        String audioUrl = fileService.uploadAudio(audioFile, AUDIO_FOLDER);

        // 2. 调用 ASR 语音转文字
        String transcript = asrService.transcribe(audioFile);

        // 3. 复用文本回答流程
        SubmitAnswerDTO dto = new SubmitAnswerDTO();
        dto.setQuestionId(questionId);
        dto.setAnswerText(transcript);

        InterviewDetailVO.QAPair result = submitAnswer(sessionId, dto);

        // 4. 补充录音 URL 到回答记录
        if (audioUrl != null) {
            InterviewAnswers answer = answerMapper.selectByQuestionId(questionId);
            if (answer != null) {
                answer.setAudioUrl(audioUrl);
                answerMapper.updateAudioUrl(answer.getId(), audioUrl);
            }
        }

        return result;
    }

    // ==================== 结束面试 ====================

    @Override
    public void endInterview(Long sessionId) {

        long userId = StpUtil.getLoginIdAsLong();
        validateSessionOwnership(sessionId, userId);

        InterviewSessions session = sessionMapper.selectById(sessionId);
        if (!"running".equals(session.getStatus())) {
            throw new BusinessException("面试未在进行中");
        }

        sessionMapper.updateStatus(sessionId, "completed");
        log.info("面试结束: sessionId={}", sessionId);
    }

    // ==================== 获取面试详情 ====================

    @Override
    public InterviewDetailVO getInterviewDetail(Long sessionId) {

        long userId = StpUtil.getLoginIdAsLong();
        InterviewSessions session = validateSessionOwnership(sessionId, userId);

        InterviewDetailVO vo = new InterviewDetailVO();
        BeanUtils.copyProperties(session, vo);

        // 构建 QA 列表
        List<InterviewQuestions> questions = questionMapper.selectBySessionId(sessionId);
        List<InterviewAnswers> answers = answerMapper.selectBySessionId(sessionId);

        Map<Long, String> answerMap = answers.stream()
                .collect(Collectors.toMap(InterviewAnswers::getQuestionId, InterviewAnswers::getAnswerText));

        List<InterviewDetailVO.QAPair> qaPairs = questions.stream().map(q -> {
            InterviewDetailVO.QAPair pair = new InterviewDetailVO.QAPair();
            pair.setQuestionId(q.getId());
            pair.setParentQuestionId(q.getParentQuestionId());
            pair.setQuestionType(q.getQuestionType());
            pair.setSequenceNo(q.getSequenceNo());
            pair.setQuestionText(q.getQuestionText());
            pair.setQuestionAudioUrl(q.getQuestionAudioUrl());
            pair.setAnswerText(answerMap.get(q.getId()));
            return pair;
        }).toList();

        vo.setQaPairs(qaPairs);
        return vo;
    }

    // ==================== 面试列表 ====================

    @Override
    public List<InterviewSessionVO> getInterviewList() {

        long userId = StpUtil.getLoginIdAsLong();
        List<InterviewSessions> list = sessionMapper.selectByUserId(userId);
        return list.stream().map(s -> {
            InterviewSessionVO vo = new InterviewSessionVO();
            BeanUtils.copyProperties(s, vo);
            return vo;
        }).toList();
    }

    // ==================== 生成面试报告 ====================

    @Override
    public InterviewReportVO generateReport(Long sessionId) {

        long userId = StpUtil.getLoginIdAsLong();
        InterviewSessions session = validateSessionOwnership(sessionId, userId);

        if (!"completed".equals(session.getStatus())) {
            throw new BusinessException("面试尚未结束，无法生成报告");
        }

        // 检查是否已有报告
        InterviewReports existing = reportMapper.selectBySessionId(sessionId);
        if (existing != null) {
            throw new BusinessException("报告已生成，请勿重复操作");
        }

        try {
            // 构建上下文
            String chatHistory = buildChatHistory(sessionId);
            String resumeSummary = buildResumeSummaryForSession(session);
            String jdSummary = buildJdSummaryForSession(session);

            String prompt = promptTemplateManager.buildPrompt("report-generate", Map.of(
                    "resumeSummary", resumeSummary,
                    "jdSummary", StringUtils.hasText(jdSummary) ? jdSummary : "（未关联 JD）",
                    "chatHistory", chatHistory
            ));
            InterviewReportResult result = llmGatewayService.call(prompt, InterviewReportResult.class,
                    LlmCallOptions.analysis());

            // 写入 interview_reports
            InterviewReports report = InterviewReports.builder()
                    .sessionId(sessionId)
                    .overallScore(result.getOverallScore())
                    .technicalScore(result.getTechnicalScore())
                    .communicationScore(result.getCommunicationScore())
                    .projectScore(result.getProjectScore())
                    .strengths(toJson(result.getStrengths()))
                    .weaknesses(toJson(result.getWeaknesses()))
                    .recommendations(toJson(result.getRecommendations()))
                    .build();
            reportMapper.insert(report);

            // 更新会话的综合评分
            sessionMapper.updateScore(sessionId, result.getOverallScore());

            log.info("面试报告生成成功: sessionId={}", sessionId);

            InterviewReportVO vo = new InterviewReportVO();
            BeanUtils.copyProperties(report, vo);
            return vo;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("面试报告生成失败: sessionId={}", sessionId, e);
            throw new BusinessException("报告生成失败：" + e.getMessage());
        }
    }

    // ==================== 获取报告 ====================

    @Override
    public InterviewReportVO getReport(Long sessionId) {

        long userId = StpUtil.getLoginIdAsLong();
        validateSessionOwnership(sessionId, userId);

        InterviewReports report = reportMapper.selectBySessionId(sessionId);
        if (report == null) {
            throw new BusinessException("报告尚未生成");
        }

        InterviewReportVO vo = new InterviewReportVO();
        BeanUtils.copyProperties(report, vo);
        return vo;
    }

    // ==================== 私有辅助方法 ====================

    private void validateSessionType(StartInterviewDTO dto) {
        String type = dto.getSessionType();
        switch (type) {
            case "resume":
                if (dto.getResumeId() == null) {
                    throw new BusinessException("简历面必须关联简历");
                }
                break;
            case "job":
                if (dto.getJdId() == null) {
                    throw new BusinessException("岗位面必须关联 JD");
                }
                break;
            case "mixed":
                if (dto.getResumeId() == null || dto.getJdId() == null) {
                    throw new BusinessException("综合面必须同时关联简历和 JD");
                }
                break;
            default:
                throw new BusinessException("不支持的面试类型：" + type);
        }
    }

    private InterviewSessions validateSessionOwnership(Long sessionId, long userId) {
        InterviewSessions session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("面试会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException("无权限操作");
        }
        return session;
    }

    private String buildContextInfo(StartInterviewDTO dto) {
        StringBuilder sb = new StringBuilder();

        if (dto.getResumeId() != null) {
            ResumeProfiles profile = resumeProfileMapper.selectByResumeId(dto.getResumeId());
            if (profile != null) {
                sb.append("候选人简历画像：\n");
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
                if (StringUtils.hasText(profile.getProjectExperiences())) {
                    sb.append("项目经历：").append(profile.getProjectExperiences()).append("\n");
                }
            }
        }

        if (dto.getJdId() != null) {
            JdAnalysis analysis = jdAnalysisMapper.selectByJdId(dto.getJdId());
            if (analysis != null) {
                sb.append("\n岗位要求分析：\n");
                if (StringUtils.hasText(analysis.getRequiredSkills())) {
                    sb.append("必备技能：").append(analysis.getRequiredSkills()).append("\n");
                }
                if (StringUtils.hasText(analysis.getPreferredSkills())) {
                    sb.append("加分技能：").append(analysis.getPreferredSkills()).append("\n");
                }
                if (StringUtils.hasText(analysis.getResponsibilities())) {
                    sb.append("岗位职责：").append(analysis.getResponsibilities()).append("\n");
                }
            }
        }

        return sb.toString();
    }

    private String buildChatHistory(Long sessionId) {
        List<InterviewQuestions> questions = questionMapper.selectBySessionId(sessionId);
        List<InterviewAnswers> answers = answerMapper.selectBySessionId(sessionId);
        Map<Long, String> answerMap = answers.stream()
                .collect(Collectors.toMap(InterviewAnswers::getQuestionId, InterviewAnswers::getAnswerText));

        StringBuilder sb = new StringBuilder();
        for (InterviewQuestions q : questions) {
            sb.append("面试官：").append(q.getQuestionText()).append("\n");
            String answer = answerMap.get(q.getId());
            sb.append("候选人：").append(answer != null ? answer : "（未回答）").append("\n\n");
        }
        return sb.toString();
    }

    private String buildResumeSummaryForSession(InterviewSessions session) {
        if (session.getResumeId() == null) {
            return "（未关联简历）";
        }
        ResumeProfiles profile = resumeProfileMapper.selectByResumeId(session.getResumeId());
        if (profile == null) {
            return "（简历未解析）";
        }
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(profile.getCandidateName())) {
            sb.append("姓名：").append(profile.getCandidateName()).append("\n");
        }
        if (profile.getExperienceYears() != null) {
            sb.append("工作年限：").append(profile.getExperienceYears()).append("年\n");
        }
        if (StringUtils.hasText(profile.getSkills())) {
            sb.append("技能标签：").append(profile.getSkills()).append("\n");
        }
        if (StringUtils.hasText(profile.getSummary())) {
            sb.append("个人总结：").append(profile.getSummary());
        }
        return sb.toString();
    }

    private String buildJdSummaryForSession(InterviewSessions session) {
        if (session.getJdId() == null) {
            return "";
        }
        JdAnalysis analysis = jdAnalysisMapper.selectByJdId(session.getJdId());
        if (analysis == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("岗位要求：\n");
        if (StringUtils.hasText(analysis.getRequiredSkills())) {
            sb.append("必备技能：").append(analysis.getRequiredSkills()).append("\n");
        }
        if (StringUtils.hasText(analysis.getPreferredSkills())) {
            sb.append("加分技能：").append(analysis.getPreferredSkills()).append("\n");
        }
        if (StringUtils.hasText(analysis.getResponsibilities())) {
            sb.append("岗位职责：").append(analysis.getResponsibilities());
        }
        return sb.toString();
    }

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
