package com.interview.service;

import com.interview.dto.MatchAnalyzeDTO;
import com.interview.vo.MatchAnalysisVO;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/5
 */
public interface MatchService {

    /**
     * 执行简历-JD 匹配分析
     */
    MatchAnalysisVO analyze(MatchAnalyzeDTO dto);

    /**
     * 获取某份简历的所有匹配分析结果
     */
    List<MatchAnalysisVO> getMatchList(Long resumeId);

    /**
     * 获取指定简历和JD的匹配分析结果
     */
    MatchAnalysisVO getMatchResult(Long resumeId, Long jdId);

}
