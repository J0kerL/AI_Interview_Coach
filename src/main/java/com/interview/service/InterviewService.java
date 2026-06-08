package com.interview.service;

import com.interview.dto.StartInterviewDTO;
import com.interview.dto.SubmitAnswerDTO;
import com.interview.vo.InterviewDetailVO;
import com.interview.vo.InterviewReportVO;
import com.interview.vo.InterviewSessionVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface InterviewService {

    /**
     * 开始面试（创建会话 + AI 生成首轮题目）
     */
    InterviewDetailVO startInterview(StartInterviewDTO dto);

    /**
     * 提交回答（保存答案 + AI 判断是否追问）
     */
    InterviewDetailVO.QAPair submitAnswer(Long sessionId, SubmitAnswerDTO dto);

    /**
     * 提交语音回答（音频上传 → ASR转文字 → 保存答案 → AI 追问）
     */
    InterviewDetailVO.QAPair submitVoiceAnswer(Long sessionId, Long questionId, MultipartFile audioFile);

    /**
     * 结束面试
     */
    void endInterview(Long sessionId);

    /**
     * 获取面试详情（含完整问答）
     */
    InterviewDetailVO getInterviewDetail(Long sessionId);

    /**
     * 获取当前用户的面试列表
     */
    List<InterviewSessionVO> getInterviewList();

    /**
     * 生成面试报告
     */
    InterviewReportVO generateReport(Long sessionId);

    /**
     * 获取面试报告
     */
    InterviewReportVO getReport(Long sessionId);

}
