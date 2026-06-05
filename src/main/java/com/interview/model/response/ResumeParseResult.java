package com.interview.model.response;

import lombok.Data;

import java.util.List;

/**
 * 简历解析结果（LLM 结构化输出）
 *
 * @Author Diamond
 * @Create 2026/6/4
 */
@Data
public class ResumeParseResult {

    /**
     * 候选人姓名
     */
    private String candidateName;

    /**
     * 工作年限
     */
    private Double experienceYears;

    /**
     * AI 生成的个人总结
     */
    private String summary;

    /**
     * 技能标签列表
     */
    private List<String> skills;

    /**
     * 工作经历列表
     */
    private List<WorkExperience> workExperiences;

    /**
     * 项目经历列表
     */
    private List<ProjectExperience> projectExperiences;

    /**
     * 教育经历
     */
    private Education education;

    @Data
    public static class WorkExperience {
        /** 公司名称 */
        private String company;
        /** 职位 */
        private String position;
        /** 工作描述 */
        private String description;
        /** 开始时间 */
        private String startDate;
        /** 结束时间 */
        private String endDate;
    }

    @Data
    public static class ProjectExperience {
        /** 项目名称 */
        private String projectName;
        /** 项目角色 */
        private String role;
        /** 项目描述 */
        private String description;
        /** 技术栈 */
        private List<String> techStack;
    }

    @Data
    public static class Education {
        /** 学校名称 */
        private String school;
        /** 专业 */
        private String major;
        /** 学历（本科/硕士/博士） */
        private String degree;
        /** 毕业时间 */
        private String graduationDate;
    }

}
