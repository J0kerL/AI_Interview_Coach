package com.interview.controller.resume;

import com.interview.common.Result;
import com.interview.service.ResumeService;
import com.interview.vo.ResumeVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

}
