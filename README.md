# 英易 English Easy

[![Build](https://github.com/xiaoancute/English_Easy/actions/workflows/build.yml/badge.svg)](https://github.com/xiaoancute/English_Easy/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

英易是一个帮中文学习者理解英文单词的 Android 应用。它不直接给翻译，而是用 AI 还原一个词在母语者脑海里的样子——它的核心画面，以及各种含义是怎么从这个画面延伸出来的。

举个例子，词典会告诉你 spring 是"春天 / 弹簧 / 泉水"，但不会解释为什么同一个词有这么多意思。英易给出的答案是：它们都来自"突然向上、向外释放的力量"这个画面。

每查一个词，应用会生成一张概念卡，包含四部分：

- 核心概念：用一个画面描述这个词的原型
- 语义分支：如果一个词确实有几个独立含义，展示它们共同的来源
- 典型场景：用真实例句展示这个概念在不同语境里的样子
- 错误直觉：指出中文学习者常踩的理解误区

## 功能

- 输入单词或短语，AI 生成概念卡片（支持单核心、同形异义词、语义簇分化、固定短语、普通词组）
- 句子拆解、表达救援：把整句或「想说的中文」交给 AI 做结构化帮助
- 语境查词：可附带原句；不会覆盖已有的通用概念卡缓存
- 本地缓存：Room 按词条 + prompt 版本缓存，查过的词秒开，prompt 升级后自动失效
- 历史 / 收藏：按时间倒序，可写笔记与自己的例句，并做例句反馈
- 学习页：词库任务 + 轻量间隔复习（不是完整 Anki/FSRS）
- 把概念卡导出成文本，方便保存或发给别人
- BYOK（Bring Your Own Key）：API Key 只存在设备本地，支持任意 OpenAI 兼容端点
- Material Design 3 + Compose，支持动态色 / 深色模式

## 当前状态

项目处于 RC 收口阶段。核心流程可用：查词、句子/表达模式、卡片、历史、收藏、笔记、例句反馈、轻量复习、复制与重新生成。Prompt 为 v3。GitHub Actions 会构建 debug / release APK；正式签名包需要在仓库配置 signing secrets。

暂时不做的：登录、推荐算法、完整 SRS 系统、阅读器、社区、离线词典、自建后端。

## 技术栈

| 层级 | 技术 |
|------|------|
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Hilt DI + Kotlin Coroutines |
| 网络 | Retrofit + OkHttp + Kotlinx Serialization |
| 存储 | Room（缓存）+ DataStore（设置） |
| AI | OpenAI 兼容 API（OpenAI / DeepSeek / Moonshot / 智谱 / Groq / Ollama 等） |
| 构建 | Gradle 8.7 + KSP + GitHub Actions |

## 快速开始

### 1. 下载 APK

优先从 [GitHub Releases](https://github.com/xiaoancute/English_Easy/releases) 下载正式发布的版本。如果还没有 release，可以到 [GitHub Actions](https://github.com/xiaoancute/English_Easy/actions) 下载最新构建的产物：

- `app-debug`：调试包，适合快速试用
- `app-release`：release 构建；没配置签名 secrets 时是 unsigned APK，只适合测试

也可以自己克隆构建：

```bash
git clone https://github.com/xiaoancute/English_Easy.git
cd English_Easy
./gradlew assembleDebug
# APK 在 app/build/outputs/apk/debug/app-debug.apk
```

### 2. 配置 API Key

第一次打开应用，进入底部的"设置"标签，填入：

| 字段 | 说明 | 示例 |
|------|------|------|
| Base URL | OpenAI 兼容端点 | `https://api.openai.com/v1/` |
| 模型名 | 使用的模型 | `gpt-5-mini` |
| API Key | 你的密钥 | `sk-proj-...` |

### 3. 查询

回到主页，输入任意英文单词或短语（比如 `spring` 或 `break the ice`），等 AI 生成卡片。

## 隐私与密钥

英易用 BYOK 模式，不内置共享密钥，也不需要自建后端：

- API Key 只保存在设备本地
- 设置数据通过 Android 本地安全能力加密
- 查词请求直接发往你配置的 Base URL
- 历史、收藏、笔记都存在本机的 Room 数据库里

注意别把自己的 API Key 提交到仓库、Issue、截图或日志里。

## 使用示例

查询 `spring`：

核心概念
> 一股突然向上 / 向外释放的力量，像压缩的弹簧突然弹开

典型场景
- `Spring is here!`（春天来了）：万物"弹"出地面的季节
- `a spring in the mattress`（床垫弹簧）：储存并释放弹性能量的装置
- `a mountain spring`（山泉）：水从地下涌出

错误直觉
- ✗ 以为"spring 有三个完全不同的意思"
- ✓ 它们都来自"突然向上的力量"这个核心画面

## 项目结构

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

## 缓存策略

查询时先查本地 Room 数据库（word 为主键，带 promptVersion 字段）：

- 命中且 promptVersion 等于 `CURRENT_PROMPT_VERSION`，直接返回
- 未命中或版本过期，调用 LLM 并写入缓存
- 升级 prompt 时改 `CURRENT_PROMPT_VERSION` 常量，旧缓存自动失效

## 发布

Release 构建通过 GitHub Actions 完成，详细步骤见 [docs/release.md](docs/release.md)。大致流程：

1. 配置 release signing secrets
2. 配置 GitHub repository variable：`VERSION_CODE`
3. 推送 tag：

```bash
git tag v0.1.0
git push origin v0.1.0
```

CI 会跑单元测试、debug 构建和 release 构建，并在 `v*` tag 上创建 GitHub Release。

## 贡献

欢迎提 Issue 和 PR。

开发环境：

- Android Studio Ladybug 2024.2.1+
- JDK 17
- Android SDK 26+（最低支持 Android 8.0）

本地构建：

```bash
./gradlew assembleDebug
./gradlew test
```

## 开源协议

MIT License，见 [LICENSE](LICENSE)。

## 致谢

设计思路受 B 站 up 主 [苹果香蕉--](https://space.bilibili.com/440513906/) 的视频启发：[学习语言容易陷入的误区 | 如何高效学习英语？真正的语言学习应该是怎样的？](https://www.bilibili.com/video/BV1s6oLB8ESh/)
