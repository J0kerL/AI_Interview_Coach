package com.interview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

/**
 * 语音识别服务（ASR）
 * 调用小米 MiMo ASR API（兼容 OpenAI audio/transcriptions 协议）
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
            // 构建 multipart 请求体
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamResource(audioFile));
            body.add("model", ASR_MODEL);
            body.add("language", "zh");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(apiKey);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 调用 ASR API：{base-url}/v1/audio/transcriptions
            String url = baseUrl + "/v1/audio/transcriptions";
            ResponseEntity<String> response = restClient.post()
                    .uri(url)
                    .headers(h -> h.addAll(headers))
                    .body(requestEntity.getBody())
                    .retrieve()
                    .toEntity(String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode json = objectMapper.readTree(response.getBody());
                String text = json.has("text") ? json.get("text").asText() : "";

                if (text.isBlank()) {
                    throw new BusinessException("语音识别结果为空，请重新录音");
                }

                log.debug("ASR 识别完成: {}字符", text.length());
                return text;
            }

            throw new BusinessException("语音识别服务返回异常");

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("ASR 语音识别失败", e);
            throw new BusinessException("语音识别失败：" + e.getMessage());
        }
    }

    /**
     * 包装 MultipartFile 为 Spring 的 Resource，支持 multipart 上传
     */
    private static class MultipartInputStreamResource extends InputStreamResource {
        private final MultipartFile file;

        public MultipartInputStreamResource(MultipartFile file) throws java.io.IOException {
            super(file.getInputStream());
            this.file = file;
        }

        @Override
        public String getFilename() {
            return file.getOriginalFilename() != null ? file.getOriginalFilename() : "audio.webm";
        }

        @Override
        public long contentLength() {
            return file.getSize();
        }
    }

}
