package com.interview.controller.match;

import com.interview.common.Result;
import com.interview.dto.MatchAnalyzeDTO;
import com.interview.service.MatchService;
import com.interview.vo.MatchAnalysisVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 简历-JD 匹配分析控制器
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@RestController
@RequestMapping("/match")
public class MatchController {

    @Resource
    private MatchService matchService;

    /**
     * 执行匹配分析
     */
    @PostMapping("/analyze")
    public Result<MatchAnalysisVO> analyze(@Valid @RequestBody MatchAnalyzeDTO dto) {
        MatchAnalysisVO vo = matchService.analyze(dto);
        return Result.success(vo);
    }

    /**
     * 获取某份简历的所有匹配分析结果
     */
    @GetMapping("/list/{resumeId}")
    public Result<List<MatchAnalysisVO>> getMatchList(@PathVariable Long resumeId) {
        List<MatchAnalysisVO> list = matchService.getMatchList(resumeId);
        return Result.success(list);
    }

    /**
     * 获取指定简历和JD的匹配分析结果
     */
    @GetMapping("/{resumeId}/{jdId}")
    public Result<MatchAnalysisVO> getMatchResult(@PathVariable Long resumeId, @PathVariable Long jdId) {
        MatchAnalysisVO vo = matchService.getMatchResult(resumeId, jdId);
        return Result.success(vo);
    }

}
