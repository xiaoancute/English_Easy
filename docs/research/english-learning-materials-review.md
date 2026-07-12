# 英语学习材料调研

日期：2026-06-09

## 目的

这份文档整理了一轮 GitHub 仓库、社区文章和本地参考材料调研。目标不是把外部内容打包进 English Easy，而是判断 `v0.1.0` 之后产品应该继续强化什么、避免什么。

当前产品边界保持不变：

> English Easy 是面向中文学习者的英语概念理解加速器，不是完整英语学习系统。

## 来源清单

| 来源 | 类型 | 信号 | 复用态度 |
|---|---|---|---|
| [`byoungd/English-level-up-tips`](https://github.com/byoungd/English-level-up-tips) | GitHub 指南 | 中文学习者视角很强，覆盖词汇、听力、阅读、口语、写作和 AI 学习。 | 只做方法论参考。该指南声明 CC BY-NC 风格条款，不直接导入正文、图片或词表。 |
| [`Gianguyen1234/open-english-vn`](https://github.com/Gianguyen1234/open-english-vn) | GitHub 资源目录 | 展示了一个可维护的资源目录结构：等级、技能、主题、贡献模板、移动端目录。 | MIT 项目结构可参考；具体资源仍只做外链，不打包进 app。 |
| [`jcrodriguezu/english_learning_resources`](https://github.com/jcrodriguezu/english_learning_resources) | GitHub/社区资源表 | 来自开发者 Slack 社区的小型资源汇总，常见分类包括听力、交流、发音、应用、书籍。 | 只做资源分类参考，不作为内置内容来源。 |
| [`参考.md`](../参考.md) | 用户提供的本地视频文稿 | 与 English Easy 高度一致：反对长期依赖中文中转，强调英文直接映射概念、画面和场景。 | 用户提供参考。可以总结思想；不在项目文档中发布全文，除非后续明确来源权限。 |
| [Antimoon: A guide to learning English really well](https://www.antimoon.com/how/howtolearn.htm) | 社区方法文章 | 强调动机、词典、正确输入、发音、大量句子和 SRS。 | 方法论参考，不导入内容。 |
| [Antimoon: Dictionary](https://www.antimoon.com/how/dict.htm) | 社区方法文章 | 推荐英英词典、发音和例句；认为例句对输出能力比定义更关键。 | 支持 English Easy 强化例句和语境，而不是增加中文释义。 |
| [Antimoon: Get input. Lots of it.](https://www.antimoon.com/how/input.htm) | 社区方法文章 | 把 input 定义为读到/听到的英文句子，并强调输入能支持后续造句。 | 支持把概念卡连接到真实句子和输出。 |
| [Antimoon: Learn English without mistakes](https://www.antimoon.com/how/mistakes.htm) | 社区方法文章 | 警告粗糙输出会固化错误，建议慢一点、简单一点、查证后再输出。 | 支持“我的输出”保持小而准，不做泛作文工具。 |
| [Antimoon: Use spaced-repetition software](https://www.antimoon.com/how/srs.htm) | 社区方法文章 | 推荐用 SRS 复习单词、短语、发音和句型。 | 支持后续 Anki/Markdown 导出，暂不重造完整 SRS。 |
| [Refold: Sentence Mining](https://refold.la/roadmap/library/sentence-mining) | 社区路线图 | 强调从基本理解的真实句子中挖词、查词、存句子卡、复习。 | 直接支持“来源句子/上下文句子”功能。 |
| [Refold: Phase 2 Comprehension](https://refold.la/roadmap/phase-2) | 社区路线图 | 通过常见词、带音频阅读、真实内容中的句子挖掘来建立理解。 | 支持 English Easy 做真实输入的辅助工具，而不是替代输入。 |
| [Refold: The Pillars of Language Learning](https://prod.refold.la/roadmap/library/the-pillars-of-language-learning) | 社区路线图 | 区分 priming、interactive immersion、freeflow immersion 和后期输出。 | English Easy 更像 priming + interactive lookup，应把用户送回输入/输出。 |

补充说明：调研中发现了一些 Reddit 讨论线索，但 Reddit 通过 robots.txt 阻止自动抓取，所以没有把这些讨论列入证据表。后续可以人工阅读后再补充。

## 收敛主题

### 1. 单词不能停留在孤立状态

GitHub 指南、Antimoon、Refold 和本地 `参考.md` 都指向同一个问题：孤立背单词很弱。真正有价值的学习单位通常是句子里的词、场景里的短语，或者一个能迁移的概念画面。

对 English Easy 的影响：

- 概念卡方向正确，因为它在对抗“英文 = 中文释义列表”。
- App 应继续推动用户从“核心概念”走向“典型场景”，再走向“我的输出”。
- 词库只应该是查词任务来源，不应该成为产品主角。

### 2. 输入仍然是主燃料

Antimoon 和 Refold 都把阅读/听力输入视为语言能力增长的主要燃料。Refold 还区分了两种输入状态：一种是暂停、查词、挖句子的交互式输入；另一种是不打断流动的自由输入。

对 English Easy 的影响：

- 不要把 English Easy 包装成完整替代阅读、听力、口语和写作的工具。
- 后续功能应该帮助用户更快回到真实输入中。
- 下一步好功能不是做大型课程页，而是给单词挂上来源句子或具体语境。

### 3. 输出重要，但应该小而准

`参考.md`、Antimoon、Refold 和当前 app 方向都指向一个窄输出闭环：理解一个词之后，写一句自己真的可能会用的话。

对 English Easy 的影响：

- “我的输出”应该继续保持短。
- 后续可以让 AI 帮用户改一句话，但不要扩成泛用 AI 写作老师。
- 复习时应该让用户回忆核心画面或写一个小句子，而不是翻译整段中文。

### 4. SRS 有用，但不是核心差异化

Antimoon 支持 SRS，Refold 也把句子卡作为学习流程的一部分。结论是：复习有用，但卡片设计比系统复杂度更重要。

对 English Easy 的影响：

- 保留当前轻量复习即可。
- 在“概念 -> 语境 -> 输出”闭环稳之前，不要做复杂 Anki 克隆。
- 更适合的复习形式是：
  - 回忆核心画面；
  - 判断某个场景能不能用这个词；
  - 在已知句子里填一个词；
  - 改写自己的例句。

### 5. AI 有用，但生成内容必须有护栏

`English-level-up-tips` 的 AI 章节支持 AI 辅助学习，但也说明了一个产品风险：AI 很容易生成太多建议、太少练习。生成例句只有在迫使用户做真实用法判断时才有价值。

对 English Easy 的影响：

- AI 应该生成受约束的小练习，而不是长篇建议。
- 生成内容要有明确目的：
  - 对比两个近义词；
  - 强迫用户判断某个场景能不能用；
  - 测一个中文学习者常见误解；
  - 要求用户写或修改一句话。
- 避免做泛泛的 “teach me this word” 聊天页。

## 对 English Easy 的路线建议

### 保留

- 概念卡定位。
- 中文学习者焦点。
- 核心画面 / 典型场景 / 错误直觉结构。
- 本地历史、收藏、笔记和个人输出。
- 轻量 Study 页和词库任务来源。

### 下一步优先加

1. **例句改进闭环**
   - 用户写一句话。
   - AI 给一个改进版和一个理由。
   - 用户可以保存改进后的句子。

2. **上下文句子字段**
   - 用户粘贴遇到这个词的原句。
   - 概念卡解释这个词在该句中的具体含义。
   - 这能接近 sentence mining，但不需要把 English Easy 做成阅读器。

3. **超过“记得/忘了”的复习提示**
   - 显示单词，问核心画面。
   - 显示场景，问这个词是否合适。
   - 显示用户自己的句子，要求改进。

4. **导出**
   - Markdown 导出继续保留。
   - 后续做 Anki 兼容导出，比内建复杂 SRS 更合理。

### 避免

- 未经清晰授权就导入第三方文章、图片或词表。
- 把 app 做成大而全资源目录。
- 在输出闭环有用之前增加打卡、徽章、大数据看板或泛课程计划。
- 让 AI 输出长篇解释，而不是帮用户改好一句话。

## 文档整理建议

目前文档主要由 release notes 和设计计划组成。后续产品调研建议固定为：

```text
docs/
  research/
    README.md
    english-learning-materials-review.md
  language-learning-alignment-review.md
  vocabulary-sources.md
  third-party-notices.md
  release.md
  release-notes/
```

约定：

- `docs/research/`：外部调研和产品影响。
- `docs/language-learning-alignment-review.md`：稳定产品理论和边界。
- `docs/vocabulary-sources.md`、`docs/third-party-notices.md`：只放来源和许可追踪。
- `docs/release-notes/`：只放面向用户的发布历史。

## 结论

外部材料支持当前 `v0.1.0` 方向。English Easy 不应该变成完整英语学习平台。它最强的路线是：

> 概念查询 -> 上下文句子 -> 个人输出 -> 轻量复习/导出。
