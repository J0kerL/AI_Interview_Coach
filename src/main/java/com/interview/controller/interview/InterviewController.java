package com.interview.controller.interview;

import com.interview.common.Result;
import com.interview.dto.StartInterviewDTO;
import com.interview.dto.SubmitAnswerDTO;
import com.interview.dto.TtsSynthesizeDTO;
import com.interview.service.InterviewService;
import com.interview.service.TtsService;
import com.interview.vo.InterviewDetailVO;
import com.interview.vo.InterviewReportVO;
import com.interview.vo.InterviewSessionVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI 面试控制器
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@RestController
@RequestMapping("/interview")
public class InterviewController {

    @Resource
    private InterviewService interviewService;

    @Resource
    private TtsService ttsService;

    /**
     * 开始面试（创建会话 + AI 生成首轮题目）
     * mode=text: 纯文字面试
     * mode=voice: 语音面试（题目自动生成语音）
     */
    @PostMapping("/start")
    public Result<InterviewDetailVO> startInterview(@Valid @RequestBody StartInterviewDTO dto) {
        InterviewDetailVO detail = interviewService.startInterview(dto);
        return Result.success(detail);
    }

    /**
     * 提交文本回答（保存答案 + AI 判断是否追问）
     */
    @PostMapping("/{sessionId}/answer")
    public Result<InterviewDetailVO.QAPair> submitAnswer(@PathVariable Long sessionId,
                                                          @Valid @RequestBody SubmitAnswerDTO dto) {
        InterviewDetailVO.QAPair followUp = interviewService.submitAnswer(sessionId, dto);
        return Result.success(followUp);
    }

    /**
     * 提交语音回答（音频上传 → ASR转文字 → 保存答案 → AI 追问）
     */
    @PostMapping("/{sessionId}/answer/voice")
    public Result<InterviewDetailVO.QAPair> submitVoiceAnswer(
            @PathVariable Long sessionId,
            @RequestParam("questionId") Long questionId,
            @RequestParam("audio") MultipartFile audioFile) {
        InterviewDetailVO.QAPair followUp = interviewService.submitVoiceAnswer(sessionId, questionId, audioFile);
        return Result.success(followUp);
    }

    /**
     * 结束面试
     */
    @PostMapping("/{sessionId}/end")
    public Result<Void> endInterview(@PathVariable Long sessionId) {
        interviewService.endInterview(sessionId);
        return Result.success();
    }

    /**
     * 获取面试详情（含完整问答 + 语音URL）
     */
    @GetMapping("/{sessionId}")
    public Result<InterviewDetailVO> getInterviewDetail(@PathVariable Long sessionId) {
        InterviewDetailVO detail = interviewService.getInterviewDetail(sessionId);
        return Result.success(detail);
    }

    /**
     * 获取当前用户的面试列表
     */
    @GetMapping("/list")
    public Result<List<InterviewSessionVO>> getInterviewList() {
        List<InterviewSessionVO> list = interviewService.getInterviewList();
        return Result.success(list);
    }

    /**
     * 生成面试报告
     */
    @PostMapping("/report/{sessionId}/generate")
    public Result<InterviewReportVO> generateReport(@PathVariable Long sessionId) {
        InterviewReportVO report = interviewService.generateReport(sessionId);
        return Result.success(report);
    }

    /**
     * 获取面试报告
     */
    @GetMapping("/report/{sessionId}")
    public Result<InterviewReportVO> getReport(@PathVariable Long sessionId) {
        InterviewReportVO report = interviewService.getReport(sessionId);
        return Result.success(report);
    }

    /**
     * TTS 语音合成（独立接口，将文字转为语音 URL）
     */
    @PostMapping("/tts")
    public Result<String> synthesizeSpeech(@Valid @RequestBody TtsSynthesizeDTO dto) {
        String audioUrl = dto.getVoice() != null
                ? ttsService.synthesize(dto.getText(), dto.getVoice())
                : ttsService.synthesize(dto.getText());
        return Result.success(audioUrl);
    }

}
