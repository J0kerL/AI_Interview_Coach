/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : interview_coach

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 08/06/2026 13:44:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for interview_answers
-- ----------------------------
DROP TABLE IF EXISTS `interview_answers`;
CREATE TABLE `interview_answers`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '回答ID',
  `question_id` bigint UNSIGNED NOT NULL COMMENT '问题ID',
  `answer_text` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '回答内容',
  `audio_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '录音地址',
  `duration_seconds` int NULL DEFAULT NULL COMMENT '回答时长(秒)',
  `transcript` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '语音转文本结果',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_question`(`question_id` ASC) USING BTREE,
  CONSTRAINT `fk_answer_question` FOREIGN KEY (`question_id`) REFERENCES `interview_questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '面试回答表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interview_questions
-- ----------------------------
DROP TABLE IF EXISTS `interview_questions`;
CREATE TABLE `interview_questions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '问题ID',
  `session_id` bigint UNSIGNED NOT NULL COMMENT '面试会话ID',
  `parent_question_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '父问题ID',
  `question_type` enum('intro','technical','behavior','project','followup') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '题目类型',
  `sequence_no` int NOT NULL COMMENT '题目序号',
  `question_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '问题内容',
  `ai_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI出题原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `question_audio_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '问题语音URL',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_session`(`session_id` ASC) USING BTREE,
  INDEX `idx_session_seq`(`session_id` ASC, `sequence_no` ASC) USING BTREE,
  CONSTRAINT `fk_question_session` FOREIGN KEY (`session_id`) REFERENCES `interview_sessions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '面试问题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interview_reports
-- ----------------------------
DROP TABLE IF EXISTS `interview_reports`;
CREATE TABLE `interview_reports`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '报告ID',
  `session_id` bigint UNSIGNED NOT NULL COMMENT '面试会话ID',
  `overall_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '综合评分',
  `technical_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '技术评分',
  `communication_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '沟通评分',
  `project_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '项目深度评分',
  `strengths` json NULL COMMENT '优势分析',
  `weaknesses` json NULL COMMENT '待提升项',
  `recommendations` json NULL COMMENT '学习建议',
  `report_content` json NULL COMMENT '完整报告',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session`(`session_id` ASC) USING BTREE,
  CONSTRAINT `fk_report_session` FOREIGN KEY (`session_id`) REFERENCES `interview_sessions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '面试报告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interview_sessions
-- ----------------------------
DROP TABLE IF EXISTS `interview_sessions`;
CREATE TABLE `interview_sessions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '面试会话ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `resume_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联简历ID',
  `jd_id` bigint UNSIGNED NULL DEFAULT NULL COMMENT '关联JD ID',
  `session_type` enum('resume','job','mixed') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '面试类型',
  `status` enum('pending','running','completed','terminated') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '面试状态',
  `mode` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'text' COMMENT '面试模式：text/voice',
  `total_questions` int NULL DEFAULT 0 COMMENT '题目总数',
  `overall_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '最终评分',
  `started_at` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `ended_at` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_status`(`user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_session_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '面试会话表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for jd_analysis
-- ----------------------------
DROP TABLE IF EXISTS `jd_analysis`;
CREATE TABLE `jd_analysis`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'JD分析ID',
  `jd_id` bigint UNSIGNED NOT NULL COMMENT 'JD ID',
  `required_skills` json NULL COMMENT '必备技能',
  `preferred_skills` json NULL COMMENT '加分技能',
  `responsibilities` json NULL COMMENT '岗位职责',
  `keywords` json NULL COMMENT '关键词',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_jd`(`jd_id` ASC) USING BTREE,
  CONSTRAINT `fk_jd_analysis` FOREIGN KEY (`jd_id`) REFERENCES `job_descriptions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'JD分析结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for job_descriptions
-- ----------------------------
DROP TABLE IF EXISTS `job_descriptions`;
CREATE TABLE `job_descriptions`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '岗位JD ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位名称',
  `company_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公司名称',
  `source_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'JD来源链接',
  `content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'JD原始内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  CONSTRAINT `fk_jd_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '岗位JD表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for resume_job_matches
-- ----------------------------
DROP TABLE IF EXISTS `resume_job_matches`;
CREATE TABLE `resume_job_matches`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '匹配记录ID',
  `resume_id` bigint UNSIGNED NOT NULL COMMENT '简历ID',
  `jd_id` bigint UNSIGNED NOT NULL COMMENT 'JD ID',
  `overall_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '总体匹配分',
  `skill_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '技能匹配分',
  `experience_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '经验匹配分',
  `strengths` json NULL COMMENT '匹配优势',
  `gaps` json NULL COMMENT '能力差距',
  `analysis` json NULL COMMENT '完整分析结果',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_resume_jd`(`resume_id` ASC, `jd_id` ASC) USING BTREE,
  INDEX `idx_jd`(`jd_id` ASC) USING BTREE,
  CONSTRAINT `fk_match_jd` FOREIGN KEY (`jd_id`) REFERENCES `job_descriptions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_match_resume` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '简历与岗位匹配分析表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for resume_profiles
-- ----------------------------
DROP TABLE IF EXISTS `resume_profiles`;
CREATE TABLE `resume_profiles`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '简历画像ID',
  `resume_id` bigint UNSIGNED NOT NULL COMMENT '简历ID',
  `candidate_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '候选人姓名',
  `experience_years` decimal(4, 1) NULL DEFAULT NULL COMMENT '工作年限',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'AI生成个人总结',
  `skills` json NULL COMMENT '技能标签',
  `work_experiences` json NULL COMMENT '工作经历',
  `project_experiences` json NULL COMMENT '项目经历',
  `education` json NULL COMMENT '教育经历',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_resume`(`resume_id` ASC) USING BTREE,
  CONSTRAINT `fk_profile_resume` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '简历解析结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for resumes
-- ----------------------------
DROP TABLE IF EXISTS `resumes`;
CREATE TABLE `resumes`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '简历ID',
  `user_id` bigint UNSIGNED NOT NULL COMMENT '用户ID',
  `resume_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简历名称',
  `file_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简历文件地址',
  `parse_status` tinyint NULL DEFAULT 0 COMMENT '解析状态：0待解析 1解析中 2成功 3失败',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted_at` datetime NULL DEFAULT NULL COMMENT '逻辑删除时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_created`(`user_id` ASC, `created_at` ASC) USING BTREE,
  CONSTRAINT `fk_resume_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户简历表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `nickname` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '普通用户' COMMENT '昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'https://ai-interview-c0ach.oss-cn-beijing.aliyuncs.com/defaultAvatar.png' COMMENT '头像地址',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常 0禁用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
