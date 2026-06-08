# AI 面试教练 - 后端服务

> 基于 Spring Boot 3.5 + Spring AI + Sa-Token 构建的 AI 面试教练后端服务，支持简历解析、JD 分析、AI 面试（文字/语音双模式）、面试报告生成等完整功能。

## 技术栈

| 组件 | 技术 | 版本 |
|---|---|---|
| 框架 | Spring Boot | 3.5.14 |
| AI 集成 | Spring AI (OpenAI 兼容) | 1.0.0 |
| 大模型 | 小米 MiMo-V2.5-Pro | - |
| 语音识别 | MiMo-V2.5-ASR | - |
| 语音合成 | MiMo-V2.5-TTS | - |
| 认证 | Sa-Token | 1.39.0 |
| ORM | MyBatis | 3.0.5 |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis | - |
| 对象存储 | 阿里云 OSS | 3.17.4 |
| PDF 解析 | Apache PDFBox | 3.0.3 |
| 参数校验 | Jakarta Validation | - |
| Java | JDK | 17+ |

## 项目结构

```
src/main/java/com/interview/
├── common/                    # 通用组件
│   ├── Result.java            # 统一响应封装
│   └── exception/             # 全局异常处理
├── config/                    # 配置类
│   ├── OssConfig.java         # 阿里云 OSS 配置
│   └── SaTokenConfig.java     # Sa-Token 认证配置
├── controller/                # 控制器层
│   ├── ai/                    # AI 测试接口
│   ├── interview/             # AI 面试接口
│   ├── jd/                    # JD 管理接口
│   ├── match/                 # 匹配分析接口
│   ├── resume/                # 简历管理接口
│   └── user/                  # 用户/认证接口
├── dto/                       # 请求 DTO
├── entity/                    # 数据库实体
├── mapper/                    # MyBatis Mapper
├── model/response/            # LLM 结构化输出模型
├── service/                   # 服务层
│   ├── impl/                  # 服务实现
│   └── llm/                   # LLM 网关 + Prompt 管理
└── vo/                        # 响应 VO
```

## 环境要求

- JDK 17+
- MySQL 8.0+
- Redis
- 阿里云 OSS Bucket
- 小米 MiMo API Key

## 快速开始

### 1. 初始化数据库

```bash
# 创建数据库
CREATE DATABASE interview_coach DEFAULT CHARACTER SET utf8mb4;

# 导入建表 SQL
mysql -u root -p interview_coach < sql/interview_coach.sql
```

### 2. 修改配置

编辑 `src/main/resources/application-dev.yaml`，填入你的环境信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/interview_coach
    username: your_username
    password: your_password
  data:
    redis:
      host: localhost
      port: 6379
      password: your_redis_password
  ai:
    openai:
      api-key: your_mimo_api_key
      base-url: https://token-plan-cn.xiaomimimo.com   # 不要带 /v1

aliyun:
  oss:
    endpoint: oss-cn-beijing.aliyuncs.com
    access-key-id: your_access_key_id
    access-key-secret: your_access_key_secret
    bucket-name: your_bucket_name
    url-prefix: https://your_bucket_name.oss-cn-beijing.aliyuncs.com
```

> ⚠️ `base-url` 不能包含 `/v1`，Spring AI 会自动追加。

### 3. 编译运行

```bash
mvn clean compile
mvn spring-boot:run
```

服务启动后访问 `http://localhost:8080/api`。

## API 接口文档

所有接口基础路径：`/api`

### 认证模块

| 方法 | 路径 | 说明 | 需登录 |
|---|---|---|---|
| POST | `/auth/register` | 用户注册 | ❌ |
| POST | `/auth/login` | 用户登录 | ❌ |
| POST | `/auth/logout` | 用户登出 | ✅ |
| GET | `/auth/captcha` | 获取验证码 | ❌ |
| POST | `/user/forgot-password` | 忘记密码 | ❌ |

**登录请求示例：**
```json
POST /api/auth/login
{
  "account": "user@example.com",
  "password": "123456",
  "captchaId": "xxx",
  "captchaCode": "abcd"
}
```

**请求头携带 Token：**
```
Authorization: Bearer <token>
```

### 用户模块

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/user/info` | 获取当前用户信息 |
| PUT | `/user/profile` | 修改用户信息 |
| PUT | `/user/password` | 修改密码 |
| PUT | `/user/avatar` | 修改头像（multipart） |

### 简历模块

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/resume/upload` | 上传简历（PDF） |
| GET | `/resume/list` | 获取简历列表 |
| DELETE | `/resume/delete/{id}` | 删除简历 |
| DELETE | `/resume/delete/batch` | 批量删除简历 |
| POST | `/resume/{id}/parse` | AI 解析简历 |
| GET | `/resume/{id}/profile` | 获取解析结果 |

### JD 模块

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/jd` | 创建 JD |
| GET | `/jd/list` | 获取 JD 列表 |
| GET | `/jd/{id}` | 获取 JD 详情 |
| DELETE | `/jd/{id}` | 删除 JD |
| DELETE | `/jd/delete/batch` | 批量删除 JD |
| POST | `/jd/{id}/parse` | AI 解析 JD |
| GET | `/jd/{id}/analysis` | 获取解析结果 |

### 匹配分析模块

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/match/analyze` | 执行简历-JD 匹配分析 |
| GET | `/match/{resumeId}/{jdId}` | 获取匹配结果 |
| GET | `/match/list/{resumeId}` | 获取简历的所有匹配结果 |

### AI 面试模块

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/interview/start` | 开始面试（AI 出题） |
| POST | `/interview/{id}/answer` | 提交文本回答 |
| POST | `/interview/{id}/answer/voice` | 提交语音回答（ASR） |
| POST | `/interview/{id}/end` | 结束面试 |
| GET | `/interview/{id}` | 获取面试详情 |
| GET | `/interview/list` | 获取面试列表 |
| POST | `/interview/report/{id}/generate` | 生成面试报告 |
| GET | `/interview/report/{id}` | 获取面试报告 |
| POST | `/interview/tts` | TTS 语音合成（独立接口） |

#### 开始面试

```json
POST /api/interview/start
{
  "sessionType": "resume",    // resume | job | mixed
  "resumeId": 1,              // 简历面/混合面必填
  "jdId": null,               // 岗位面/混合面必填
  "questionCount": 5,         // 生成题目数量，默认 5
  "mode": "text"              // text（纯文字）| voice（语音面试）
}
```

#### 提交文本回答

```json
POST /api/interview/1/answer
{
  "questionId": 10,
  "answerText": "我认为Spring的核心是IoC和AOP..."
}
```

#### 提交语音回答

```
POST /api/interview/1/answer/voice
Content-Type: multipart/form-data

questionId: 10
audio: [音频文件]
```

### AI 测试接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/ai/test` | 测试大模型连通性 |

## LLM 场景化参数

| 场景 | temperature | maxTokens | 说明 |
|---|---|---|---|
| 简历/JD 解析 | 0.3 | 2048 | 精确提取，不发挥 |
| 匹配分析/报告 | 0.5 | 4096 | 允许分析推理 |
| 面试出题/追问 | 0.8 | 2048 | 多样性、灵活性 |

## 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... }
}
```

错误响应：
```json
{
  "code": 500,
  "message": "简历不存在",
  "data": null
}
```

## 数据库表结构

| 表名 | 说明 |
|---|---|
| `users` | 用户表 |
| `resumes` | 用户简历表 |
| `resume_profiles` | 简历解析结果表 |
| `job_descriptions` | 岗位 JD 表 |
| `jd_analysis` | JD 解析结果表 |
| `resume_job_matches` | 简历-JD 匹配分析表 |
| `interview_sessions` | 面试会话表 |
| `interview_questions` | 面试问题表 |
| `interview_answers` | 面试回答表 |
| `interview_reports` | 面试报告表 |

## Prompt 模板

模板文件位于 `src/main/resources/prompts/`：

| 文件 | 用途 | 变量 |
|---|---|---|
| `resume-parse.st` | 简历解析 | `{resumeText}` |
| `jd-parse.st` | JD 解析 | `{jdText}` |
| `match-analysis.st` | 匹配分析 | `{resumeProfile}`, `{jdAnalysis}`, `{jdContent}` |
| `question-generate.st` | 面试出题 | `{interviewType}`, `{contextInfo}`, `{questionCount}` |
| `followup-generate.st` | AI 追问 | `{resumeSummary}`, `{chatHistory}`, `{lastQuestion}`, `{lastAnswer}` |
| `report-generate.st` | 报告生成 | `{resumeSummary}`, `{jdSummary}`, `{chatHistory}` |
