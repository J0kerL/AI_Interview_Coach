package com.interview.service.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * LLM 调用参数配置
 * 预设不同场景的 temperature 和 maxTokens
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
@Builder
@AllArgsConstructor
public class LlmCallOptions {

    /**
     * 创造性参数（0-1）
     * 越低越精确、越一致；越高越多样、越有创意
     */
    private Double temperature;

    /**
     * 最大输出 token 数
     */
    private Integer maxTokens;

    // ==================== 预设场景 ====================

    /**
     * 信息提取场景（简历解析、JD 解析）
     * 低 temperature 保证准确提取，不发挥
     */
    public static LlmCallOptions extraction() {
        return LlmCallOptions.builder()
                .temperature(0.3)
                .maxTokens(2048)
                .build();
    }

    /**
     * 分析评估场景（匹配分析、面试评分）
     * 中等 temperature，允许一定推理分析
     */
    public static LlmCallOptions analysis() {
        return LlmCallOptions.builder()
                .temperature(0.5)
                .maxTokens(4096)
                .build();
    }

    /**
     * 创意生成场景（面试出题、AI 追问）
     * 高 temperature 保证多样性和灵活性
     */
    public static LlmCallOptions creative() {
        return LlmCallOptions.builder()
                .temperature(0.8)
                .maxTokens(2048)
                .build();
    }

}
