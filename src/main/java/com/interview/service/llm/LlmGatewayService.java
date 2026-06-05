package com.interview.service.llm;

import com.interview.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * LLM 统一调用网关
 * 所有大模型调用均通过此服务完成，屏蔽底层模型差异
 *
 * @Author Diamond
 * @Create 2026/6/4
 */
@Slf4j
@Service
public class LlmGatewayService {

    private final ChatClient chatClient;

    public LlmGatewayService(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * 非流式调用：发送 Prompt，返回结构化 Java 对象
     *
     * @param prompt       完整的 Prompt 文本
     * @param responseType 期望的返回类型
     * @return 结构化对象
     */
    public <T> T call(String prompt, Class<T> responseType) {
        log.debug("LLM 调用 [{}]: {}", responseType.getSimpleName(), truncate(prompt));
        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(responseType);
        } catch (Exception e) {
            log.error("LLM 调用失败: {}", e.getMessage(), e);
            throw new BusinessException("AI 服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 非流式调用：发送 Prompt，返回原始文本
     *
     * @param prompt 完整的 Prompt 文本
     * @return LLM 原始响应文本
     */
    public String callForText(String prompt) {
        log.debug("LLM 文本调用: {}", truncate(prompt));
        try {
            return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("LLM 调用失败: {}", e.getMessage(), e);
            throw new BusinessException("AI 服务暂时不可用，请稍后重试");
        }
    }

    /**
     * 流式调用：适用于面试追问等需要实时输出的场景
     *
     * @param prompt 完整的 Prompt 文本
     * @return 流式响应
     */
    public Flux<String> stream(String prompt) {
        log.debug("LLM 流式调用: {}", truncate(prompt));
        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content();
    }

    /**
     * 截断日志，避免打印过长的 Prompt
     */
    private String truncate(String text) {
        if (text == null) {
            return "null";
        }
        return text.length() > 200 ? text.substring(0, 200) + "...(" + text.length() + " chars)" : text;
    }

}
