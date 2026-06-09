package com.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Base64;

/**
 * 语音合成服务（TTS）
 * 调用小米 MiMo TTS API（通过 chat/completions 端点）
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Slf4j
@Service
public class TtsService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    /**
     * TTS 模型名称
     */
    private static final String TTS_MODEL = "mimo-v2.5-tts";

    /**
     * 默认音色（中文女声-冰糖）
     */
    private static final String DEFAULT_VOICE = "冰糖";

    /**
     * 音频格式
     */
    private static final String AUDIO_FORMAT = "wav";

    private final FileService fileService;

    public TtsService(FileService fileService, ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.fileService = fileService;
        this.objectMapper = objectMapper;
    }

    /**
     * 将文字转为语音，上传到 OSS 并返回访问 URL
     *
     * @param text 要合成的文字
     * @return OSS 音频访问 URL
     */
    public String synthesize(String text) {
        return synthesize(text, DEFAULT_VOICE);
    }

    /**
     * 将文字转为语音（指定音色），上传到 OSS 并返回访问 URL
     *
     * @param text  要合成的文字
     * @param voice 音色名称
     * @return OSS 音频访问 URL
     */
    public String synthesize(String text, String voice) {
        log.debug("开始 TTS 语音合成: 文本长度={}, 音色={}", text.length(), voice);

        try {
            // 调用 MiMo TTS API
            byte[] audioBytes = callTtsApi(text, voice);

            // 上传到 OSS
            String fileName = "tts-" + UUID.randomUUID().toString().replace("-", "") + "." + AUDIO_FORMAT;
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String objectKey = "interview-audio/" + datePath + "/" + fileName;

            try (InputStream inputStream = new ByteArrayInputStream(audioBytes)) {
                // 直接用 FileService 的底层 OSS 客户端上传
                String audioUrl = fileService.uploadAudioBytes(inputStream, objectKey, audioBytes.length);
                log.debug("TTS 合成完成: url={}", audioUrl);
                return audioUrl;
            }

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("TTS 语音合成失败", e);
            throw new BusinessException("语音合成失败：" + e.getMessage());
        }
    }

    /**
     * 调用 MiMo TTS API，返回音频字节数组
     */
    private byte[] callTtsApi(String text, String voice) {
        String url = baseUrl + "/v1/chat/completions";

        // 构建请求体（MiMo TTS 使用 chat completions 格式）
        Map<String, Object> userMessage = Map.of(
                "role", "user",
                "content", "用自然流畅的语调朗读以下内容"
        );

        Map<String, Object> assistantMessage = Map.of(
                "role", "assistant",
                "content", text
        );

        Map<String, Object> audioConfig = new HashMap<>();
        audioConfig.put("format", AUDIO_FORMAT);
        audioConfig.put("voice", voice);

        Map<String, Object> requestBody = Map.of(
                "model", TTS_MODEL,
                "messages", List.of(userMessage, assistantMessage),
                "audio", audioConfig
        );

        try {
            String responseJson = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toEntity(String.class)
                    .getBody();

            // 解析响应，获取音频数据
            if (responseJson != null) {
                JsonNode json = objectMapper.readTree(responseJson);
                String audioData = json.path("choices").path(0).path("message").path("audio").path("data").asText();

                if (!audioData.isBlank()) {
                    byte[] audioBytes = Base64.getDecoder().decode(audioData);
                    log.debug("TTS API 返回音频大小: {} bytes", audioBytes.length);
                    return audioBytes;
                }
            }

            throw new BusinessException("TTS 服务返回异常");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("TTS API 调用失败", e);
            throw new BusinessException("TTS 服务调用失败：" + e.getMessage());
        }
    }

}
