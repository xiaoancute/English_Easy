# 英易 English Easy

> **概念还原器**：用 AI 把英文词和短语在母语者大脑里的样子，重新呈现给中文学习者

[![Build](https://github.com/xiaoancute/English_Easy/actions/workflows/build.yml/badge.svg)](https://github.com/xiaoancute/English_Easy/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## 💡 核心理念

传统词典告诉你"spring = 春天 / 弹簧 / 泉水"，但**为什么同一个词有这么多意思？它们之间有什么联系？**

英易不翻译单词，而是**还原概念**：
- 🧠 **核心概念**：用一个画面描述母语者脑中的"原型"（例如 spring 的核心是"突然向上的力量"）
- 🌳 **语义分支**：当一个词确实有多个独立含义时，展示它们的共同祖源
- 🎬 **典型场景**：用真实例句展示这个概念在不同语境下的样子
- ⚠️ **错误直觉**：指出中文学习者常见的理解偏差

**不是背单词工具，是概念理解工具。**

---

## ✨ 功能特性

- **🔍 智能查询**：输入单词或短语 → AI 生成概念卡片（支持单核心 / 同形异义词 / 语义簇分化 / 固定短语 / 普通词组）
- **💾 本地缓存**：Room 数据库 + prompt 版本管理，相同词秒开，prompt 升级自动重新生成
- **📚 历史记录**：按时间倒序展示查询过的词，点击即可重新查看
- **⭐ 收藏与笔记**：保存值得复看的概念卡，并给自己的理解补充备注
- **📋 复制与分享**：把概念卡导出为文本，方便保存或发给别人
- **⚙️ BYOK（Bring Your Own Key）**：所有 API Key 仅保存在设备本地，支持任意 OpenAI 兼容端点
- **🎨 Material Design 3**：Compose UI + 自适应图标 + 深色模式友好

---

## ✅ 当前状态

当前项目处于 **RC 前收口阶段**：

- Android App 核心流程已经可用：查询、卡片、历史、收藏、笔记、复制、重新生成
- Prompt 已升级到 v3，支持单词、固定短语、普通词组
- GitHub Actions 会构建 debug APK 和 release APK
- Release signing 已接入，但正式签名包需要仓库配置 signing secrets

暂时不做：登录、推荐算法、SRS 复习、阅读器、社区、离线词典。

---

## 🛠️ 技术栈

| 层级 | 技术选型 |
|------|---------|
| **UI** | Jetpack Compose + Material 3 |
| **架构** | MVVM + Hilt DI + Kotlin Coroutines |
| **网络** | Retrofit + OkHttp + Kotlinx Serialization |
| **存储** | Room (缓存) + DataStore (设置) |
| **AI** | OpenAI-compatible API（支持 OpenAI / DeepSeek / Moonshot / 智谱 / Groq / Ollama 等） |
| **构建** | Gradle 8.7 + KSP + GitHub Actions CI |

---

## 🚀 快速开始

### 1. 下载 APK

优先从 [GitHub Releases](https://github.com/xiaoancute/English_Easy/releases) 下载正式发布的 APK。

如果还没有正式 release，可以从 [GitHub Actions Artifacts](https://github.com/xiaoancute/English_Easy/actions) 下载最新构建：

- `app-debug`：调试包，适合快速试用
- `app-release`：release 构建；如果没有配置签名 secrets，则是 unsigned APK，只适合测试

或克隆仓库自行构建：
```bash
git clone https://github.com/xiaoancute/English_Easy.git
cd English_Easy
./gradlew assembleDebug
# APK 位于 app/build/outputs/apk/debug/app-debug.apk
```

### 2. 配置 API Key

首次打开应用，点击底部 **设置** tab，填入：

| 字段 | 说明 | 示例 |
|------|------|------|
| **Base URL** | OpenAI 兼容端点 | `https://api.openai.com/v1/` |
| **模型名** | 使用的模型 | `gpt-5-mini` |
| **API Key** | 你的密钥 | `sk-proj-...` |

常用配置示例：

| 服务商 | Base URL | 模型名 |
|--------|----------|--------|
| OpenAI | `https://api.openai.com/v1/` | `gpt-5-mini` |
| DeepSeek | `https://api.deepseek.com` | `deepseek-v4-flash` |
| Kimi / Moonshot | `https://api.moonshot.cn/v1` | `kimi-k2.6` |
| 智谱 GLM | `https://open.bigmodel.cn/api/paas/v4` | `glm-4.7-flash` |
| Groq | `https://api.groq.com/openai/v1` | `llama-3.3-70b-versatile` |
| 本地 Ollama | `http://localhost:11434/v1` | `llama3.2` |

模型名会随服务商更新变化；如果请求失败，先到对应平台控制台确认当前可用模型名。

### 3. 开始查询

返回主页，输入任意英文单词或短语（例如 `spring` / `break the ice`），等待 AI 生成概念卡片。

---

## 🔐 隐私与密钥

英易采用 BYOK 模式，App 不内置共享密钥，也不需要自建后端。

- API Key 只保存在设备本地
- 设置数据通过 Android 本地安全能力加密保存
- 查词请求直接发送到你配置的 Base URL
- 历史、收藏、笔记保存在本机 Room 数据库

请不要把自己的 API Key 提交到仓库、Issue、截图或日志里。

---

## 📖 使用示例

### 查询 "spring"

**核心概念**：
> 一股突然向上/向外释放的力量，像压缩的弹簧突然弹开

**典型场景**：
- `Spring is here!`（春天来了）→ 万物"弹"出地面的季节
- `a spring in the mattress`（床垫里的弹簧）→ 储存并释放弹性能量的装置
- `a mountain spring`（山泉）→ 水从地下"涌"出

**错误直觉**：
- ❌ "spring 有三个完全不同的意思"
- ✅ 它们都源自"突然向上的力量"这个核心画面

---

## 🗂️ 项目结构

```
app/src/main/java/io/github/xiaoancute/englisheasy/
├── MainActivity.kt                         入口 Activity
├── EnglishEasyApp.kt                       Hilt Application
├── data/
│   ├── model/ConceptCard.kt                概念卡片数据模型
│   ├── prompt/SystemPrompt.kt              AI 系统提示词（v3）
│   ├── settings/                           DataStore 配置存储
│   ├── llm/                                Retrofit API + Repository
│   └── local/                              Room 缓存层
├── di/                                     Hilt 依赖注入模块
└── ui/
    ├── AppRoot.kt                          四屏导航（Home / History / Favorites / Settings）
    ├── home/                               查询主页
    ├── history/                            历史记录
    ├── settings/                           设置页
    └── components/ConceptCardView.kt       卡片渲染组件
```

---

## 🔄 缓存策略

- **查询时**：先查本地 Room 数据库（word 主键 + promptVersion 字段）
- **缓存命中**：promptVersion 匹配 `CURRENT_PROMPT_VERSION` → 直接返回
- **缓存未命中或版本过期**：调用 LLM → 存入缓存
- **Prompt 升级**：修改 `CURRENT_PROMPT_VERSION` 常量，旧缓存自动失效

---

## 🚢 发布流程

Release 构建通过 GitHub Actions 完成。详细步骤见 [docs/release.md](docs/release.md)。

最短流程：

1. 配置 release signing secrets。
2. 配置 GitHub repository variable：`VERSION_CODE`。
3. 推送 tag，例如：

```bash
git tag v0.1.0
git push origin v0.1.0
```

CI 会运行单元测试、debug 构建、release 构建，并在 `v*` tag 上创建 GitHub Release。

---

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

**开发环境要求：**
- Android Studio Ladybug | 2024.2.1+
- JDK 17
- Android SDK 26+（最低支持 Android 8.0）

**本地构建：**
```bash
./gradlew assembleDebug
./gradlew test
```

---

## 📄 开源协议

本项目采用 [MIT License](LICENSE) 开源。

---

## 🙏 致谢
- **设计灵感**：受 [苹果香蕉--](https://space.bilibili.com/440513906/) B站up主的视频启发
- [学习语言容易陷入的误区 | 如何高效学习英语？真正的语言学习应该是怎样的？
](https://www.bilibili.com/video/BV1s6oLB8ESh/)

**让英文学习回归概念本质，而非死记硬背。**
