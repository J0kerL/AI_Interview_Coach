package com.interview.controller.resume;

import com.interview.common.Result;
import com.interview.service.ResumeService;
import com.interview.vo.ResumeVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author Diamond
 * @Create 2026/6/4
 */
@RestController
@RequestMapping("/resume")
public class ResumeController {

    @Resource
    private ResumeService resumeService;

    /**
     * 上传简历
     */
    @PostMapping("/upload")
    public Result<ResumeVO> uploadResume(@RequestParam("file") MultipartFile file) {
        ResumeVO resumeVO = resumeService.uploadResume(file);
        return Result.success(resumeVO);
    }

    /**
     * 获取简历列表
     */
    @GetMapping("/list")
    public Result<List<ResumeVO>> getResumeList() {
        List<ResumeVO> list = resumeService.getResumeList();
        return Result.success(list);
    }

    /**
     * 根据id删除简历
     */
    @DeleteMapping("delete/{id}")
    public Result<Void> deleteById(@PathVariable Long id) {
        resumeService.deleteById(id);
        return Result.success();
    }

}
