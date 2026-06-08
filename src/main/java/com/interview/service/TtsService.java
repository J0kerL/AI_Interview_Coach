package com.interview.service;

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
import java.util.Map;
import java.util.UUID;

/**
 * 语音合成服务（TTS）
 * 调用小米 MiMo TTS API（兼容 OpenAI audio/speech 协议）
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Slf4j
@Service
public class TtsService {

    private final RestClient restClient;

    @Value("${spring.ai.openai.api-key}")
    private String apiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    /**
     * TTS 模型名称
     */
    private static final String TTS_MODEL = "mimo-v2.5-tts";

    /**
     * 默认音色（中文男声-苏打）
     */
    private static final String DEFAULT_VOICE = "soda";

    /**
     * 音频格式
     */
    private static final String AUDIO_FORMAT = "mp3";

    private final FileService fileService;

    public TtsService(FileService fileService) {
        this.restClient = RestClient.create();
        this.fileService = fileService;
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
        String url = baseUrl + "/v1/audio/speech";

        Map<String, Object> requestBody = Map.of(
                "model", TTS_MODEL,
                "input", text,
                "voice", voice,
                "response_format", AUDIO_FORMAT
        );

        ResponseEntity<byte[]> response = restClient.post()
                .uri(url)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toEntity(byte[].class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            byte[] audioBytes = response.getBody();
            log.debug("TTS API 返回音频大小: {} bytes", audioBytes.length);
            return audioBytes;
        }

        throw new BusinessException("TTS 服务返回异常");
    }

}
