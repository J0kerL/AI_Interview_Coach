package com.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.Base64;

/**
 * 语音识别服务（ASR）
 * 调用小米 MiMo ASR API（通过 chat/completions 端点）
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Slf4j
@Service
public class AsrService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    /**
     * ASR 模型名称
     */
    private static final String ASR_MODEL = "mimo-v2.5-asr";

    /**
     * 支持的音频格式 MIME 类型映射
     */
    private static final Map<String, String> MIME_TYPE_MAP = Map.of(
            "wav", "audio/wav",
            "mp3", "audio/mpeg",
            "webm", "audio/webm",
            "m4a", "audio/mp4"
    );

    public AsrService(ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    /**
     * 将音频文件转为文字
     *
     * @param audioFile 音频文件（支持 mp3/wav/m4a/webm 等）
     * @return 识别出的文字内容
     */
    public String transcribe(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new BusinessException("音频文件不能为空");
        }

        log.debug("开始 ASR 语音识别: 文件名={}, 大小={}bytes",
                audioFile.getOriginalFilename(), audioFile.getSize());

        try {
            // 1. 将音频文件转为 Base64 编码
            byte[] audioBytes = audioFile.getBytes();
            String base64Audio = Base64.getEncoder().encodeToString(audioBytes);

            // 2. 获取 MIME 类型
            String filename = audioFile.getOriginalFilename();
            String extension = getFileExtension(filename);
            String mimeType = MIME_TYPE_MAP.getOrDefault(extension, "audio/webm");
            String dataUrl = "data:" + mimeType + ";base64," + base64Audio;

            // 3. 构建请求体（MiMo ASR 使用 chat completions 格式）
            Map<String, Object> audioData = Map.of(
                    "type", "input_audio",
                    "input_audio", Map.of("data", dataUrl)
            );

            Map<String, Object> userMessage = Map.of(
                    "role", "user",
                    "content", List.of(audioData)
            );

            Map<String, Object> requestBody = Map.of(
                    "model", ASR_MODEL,
                    "messages", List.of(userMessage),
                    "asr_options", Map.of("language", "zh")
            );

            // 4. 调用 ASR API：{base-url}/v1/chat/completions
            String url = baseUrl + "/v1/chat/completions";
            String responseJson = restClient.post()
                    .uri(url)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .toEntity(String.class)
                    .getBody();

            // 5. 解析响应
            if (responseJson != null) {
                JsonNode json = objectMapper.readTree(responseJson);
                String text = json.path("choices").path(0).path("message").path("content").asText("");

                if (text.isBlank()) {
                    throw new BusinessException("语音识别结果为空，请重新录音");
                }

                log.debug("ASR 识别完成: {}字符", text.length());
                return text;
            }

            throw new BusinessException("语音识别服务返回异常");

        } catch (BusinessException e) {
            throw e;
        } catch (IOException e) {
            log.error("读取音频文件失败", e);
            throw new BusinessException("音频文件读取失败");
        } catch (Exception e) {
            log.error("ASR 语音识别失败", e);
            throw new BusinessException("语音识别失败：" + e.getMessage());
        }
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "webm";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
