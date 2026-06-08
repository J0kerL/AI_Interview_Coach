package com.interview.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建JD请求DTO
 *
 * @Author Diamond
 * @Create 2026/6/5
 */
@Data
public class CreateJdDTO {

    /**
     * 岗位名称（必填）
     */
    @NotBlank(message = "岗位名称不能为空")
    @Size(max = 255, message = "岗位名称不能超过255个字符")
    private String title;

    /**
     * 公司名称（选填）
     */
    @Size(max = 255, message = "公司名称不能超过255个字符")
    private String companyName;

    /**
     * JD 原始文本内容（必填）
     */
    @NotBlank(message = "JD 内容不能为空")
    private String content;

}
