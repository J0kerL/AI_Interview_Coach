package com.interview.service.llm;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt 模板管理器
 * 加载 resources/prompts/ 目录下的 .st 模板文件，支持变量替换
 *
 * @Author Diamond
 * @Create 2026/6/4
 */
@Slf4j
@Component
public class PromptTemplateManager {

    /**
     * 模板缓存：模板名 → 原始模板文本
     */
    private final Map<String, String> templateCache = new ConcurrentHashMap<>();

    /**
     * 模板目录
     */
    private static final String TEMPLATE_DIR = "prompts/";

    /**
     * 预加载所有模板
     */
    @PostConstruct
    public void init() {
        loadTemplate("resume-parse");
        loadTemplate("jd-parse");
        loadTemplate("match-analysis");
        loadTemplate("question-generate");
        loadTemplate("followup-generate");
        loadTemplate("report-generate");
        loadTemplate("interview-start");
        loadTemplate("next-question-generate");
        loadTemplate("interview-closing");
        log.info("Prompt 模板加载完成，共 {} 个", templateCache.size());
    }

    /**
     * 获取模板并填充变量，生成最终 Prompt
     *
     * @param templateName 模板名称（不含后缀，如 resume-parse）
     * @param variables    模板变量
     * @return 填充后的完整 Prompt 文本
     */
    public String buildPrompt(String templateName, Map<String, Object> variables) {
        String templateText = getTemplateText(templateName);
        PromptTemplate template = new PromptTemplate(templateText);
        return template.render(variables);
    }

    /**
     * 获取原始模板文本（无变量替换）
     *
     * @param templateName 模板名称
     * @return 原始模板文本
     */
    public String getTemplateText(String templateName) {
        String text = templateCache.get(templateName);
        if (text == null) {
            throw new IllegalArgumentException("未找到 Prompt 模板: " + templateName);
        }
        return text;
    }

    /**
     * 加载单个模板文件
     */
    private void loadTemplate(String name) {
        String path = TEMPLATE_DIR + name + ".st";
        try {
            ClassPathResource resource = new ClassPathResource(path);
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            templateCache.put(name, content);
            log.debug("加载 Prompt 模板: {}", path);
        } catch (IOException e) {
            log.error("加载 Prompt 模板失败: {}", path, e);
        }
    }

}
