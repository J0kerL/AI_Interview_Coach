package com.interview.controller.jd;

import com.interview.common.Result;
import com.interview.dto.CreateJdDTO;
import com.interview.service.JdService;
import com.interview.vo.JdAnalysisVO;
import com.interview.vo.JdVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * JD 管理控制器
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@RestController
@RequestMapping("/jd")
public class JdController {

    @Resource
    private JdService jdService;

    /**
     * 创建 JD
     */
    @PostMapping
    public Result<JdVO> createJd(@Valid @RequestBody CreateJdDTO dto) {
        JdVO jdVO = jdService.createJd(dto);
        return Result.success(jdVO);
    }

    /**
     * 获取当前用户的 JD 列表
     */
    @GetMapping("/list")
    public Result<List<JdVO>> getJdList() {
        List<JdVO> list = jdService.getJdList();
        return Result.success(list);
    }

    /**
     * 获取 JD 详情
     */
    @GetMapping("/{id}")
    public Result<JdVO> getJdDetail(@PathVariable Long id) {
        JdVO jdVO = jdService.getJdDetail(id);
        return Result.success(jdVO);
    }

    /**
     * 删除 JD
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteJd(@PathVariable Long id) {
        jdService.deleteJd(id);
        return Result.success();
    }

    /**
     * 批量删除 JD
     */
    @DeleteMapping("/delete/batch")
    public Result<Void> deleteJdByIds(@RequestBody List<Long> ids) {
        jdService.deleteJdByIds(ids);
        return Result.success();
    }

    /**
     * AI 解析 JD
     */
    @PostMapping("/{id}/parse")
    public Result<Void> parseJd(@PathVariable Long id) {
        jdService.parseJd(id);
        return Result.success();
    }

    /**
     * 获取 JD 解析结果
     */
    @GetMapping("/{id}/analysis")
    public Result<JdAnalysisVO> getJdAnalysis(@PathVariable Long id) {
        JdAnalysisVO analysisVO = jdService.getJdAnalysis(id);
        return Result.success(analysisVO);
    }

}
