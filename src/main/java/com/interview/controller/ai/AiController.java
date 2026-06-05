package com.interview.controller.ai;

import com.interview.common.Result;
import com.interview.service.llm.LlmGatewayService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 测试控制器
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LlmGatewayService llmGatewayService;

    /**
     * 测试大模型连通性
     */
    @GetMapping("/test")
    public Result<String> testLlm() {
        String response = llmGatewayService.callForText("请回复：你好，我是AI面试教练，连通性测试成功！");
        return Result.success(response);
    }

}
