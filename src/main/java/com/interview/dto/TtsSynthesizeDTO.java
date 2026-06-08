package com.interview.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * TTS 语音合成请求DTO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class TtsSynthesizeDTO {

    /**
     * 要合成的文本
     */
    @NotBlank(message = "文本内容不能为空")
    private String text;

    /**
     * 音色名称（可选，默认苏打-中文男声）
     */
    private String voice;

}
