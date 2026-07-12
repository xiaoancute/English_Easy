# 英易与语言学习理论对照审查

日期：2026-06-01

## 相关文档

- [英语学习材料调研](research/english-learning-materials-review.md)：GitHub、社区文章和本地参考材料的后续调查。

## 结论

英易目前的核心方向 **没有违背语言学习规律**，但它不能被定义成完整的英语学习系统。

更准确地说，它属于词汇学习中的一小块：

> language-focused learning / form-focused vocabulary learning：把学习者注意力集中到词义边界、用法差异、常见误解和可迁移场景上。

它适合解决：

- 学生把英文词死记成中文释义列表。
- 多义词用法互相割裂。
- 固定短语被逐词翻译。
- 学了词但不知道如何迁移到新句子。

它不适合单独解决：

- 大量真实输入不足。
- 听说读写综合能力不足。
- 自动化输出能力不足。
- 语音、语法、篇章能力不足。

所以项目现在最危险的不是“理念错”，而是产品表达如果暗示“靠概念卡就能学会英语”，那就会违背语言学习规律。

---

## 参考主线

### 1. Nation 的 four strands

Paul Nation 的语言课程框架通常把有效语言学习分为四类投入：

1. meaning-focused input：理解性输入，例如阅读、听力。
2. meaning-focused output：表达性输出，例如说、写。
3. language-focused learning：有意识地学习语言形式、词汇、发音、语法。
4. fluency development：在已知材料上提升速度和流利度。

英易明显落在第 3 类：language-focused learning。

这说明英易可以成立，但只能占学习系统的一部分。它不能替代真实阅读、听力、写作和口语。

项目计划书里已经写了“语感需要长期输入输出，本项目只做打地基这一步”，这个判断是对的。

### 2. 词汇知识不是只知道中文意思

词汇能力通常包含：

- form：拼写、发音、词形。
- meaning：核心意义、概念边界、搭配关系、联想。
- use：语法位置、搭配、语域、场景、限制。

英易重点做 meaning，并尝试通过典型场景触到 use。

这不违背词汇学习，反而比“英文 = 中文释义”更接近深层词汇知识。

但风险是：如果卡片只讲“漂亮的概念画面”，没有足够例句、搭配和用户产出，它仍然可能停在理解层，不会自动变成可用能力。

### 3. 多义词、原型范畴、概念隐喻方向是合理的

项目把多义词看成“核心概念 + 场景投射”，这与认知语言学中的原型范畴、径向范畴、概念隐喻方向基本一致。

这对中文学习者尤其有意义，因为传统词典把一个英文词拆成多个中文释义，容易让学习者误以为这些用法互不相关。

但也要避免过度统一：

- 同形异义词不能硬凑统一概念。
- 高频语法化动词不能为了理念强行一个核心解释所有用法。
- 普通自由组合不能胡编深层含义。

当前 prompt 已经有 `HOMONYM / SEMANTIC_CLUSTER / FREE_COMBINATION` 的分支机制，这一点是正确防线。

### 4. 间隔重复和提取练习是有效的，但不是核心差异化

间隔重复、主动回忆、测试效应都支持词汇保持。

英易现在已经有轻量复习/学习状态，这是合理的；但如果把大量精力投入完整 SRS、复杂提醒、成就系统，容易偏离核心。

更合理的是：

- 当前阶段只做轻量复习闭环。
- 以后优先做 Anki/FSRS 导出或兼容，而不是重造完整 Anki。
- 复习内容不要只是“我记得/不记得这个中文意思”，而应该复习“核心画面 + 新句迁移”。

### 5. 输出和迁移必须进入产品闭环

语言学习不是看懂解释就结束。看懂只是 recognition，能用才接近 productive knowledge。

项目现在 paper MVP 的“可迁移造句”指标是正确的。

App 里如果要继续补学习系统，最符合学习规律的不是新手引导，也不是更多按钮，而是：

- 看完卡片后，让用户写一句自己的句子。
- 保存“我的理解/我的例句”。
- 复习时让用户先回忆核心画面，再看卡片。
- 对固定短语，要求用户判断一个场景能不能用它。

这类功能比签到、成就、复杂统计更贴近学习本质。

---

## 对英易现状的判定

### 没有违背的地方

| 项目做法 | 判断 | 原因 |
|---|---|---|
| 不把英文等同中文释义 | 正确 | 有助于建立词汇概念边界 |
| 用核心画面统一多义词 | 基本正确 | 符合认知语言学方向，但需要避免强行统一 |
| 固定短语按整体解释 | 正确 | 习语和固定搭配不能逐词翻译 |
| 普通词组标成自由组合 | 正确 | 防止 AI 胡编深层含义 |
| 错误直觉栏目 | 有价值 | 能针对中文学习者常见负迁移 |
| 场景例句 + 画面解释 | 有价值 | 比孤立释义更接近真实使用 |
| 轻量复习状态 | 合理 | 有助于保持，但不应变成主战场 |

### 有风险的地方

| 风险 | 说明 | 建议 |
|---|---|---|
| 输入不足 | 只查词不会形成语言能力 | 不要宣传成完整学习系统；后期可接阅读/例句输入 |
| 输出不足 | 看懂卡片不等于会用 | 加“我的例句/迁移造句”比加引导更重要 |
| 过度概念化 | 有些词没有漂亮统一核心 | 保留 HOMONYM / SEMANTIC_CLUSTER，不强行圆 |
| 解释替代记忆 | 用户可能觉得懂了但很快忘 | 复习时测核心画面和使用场景 |
| 中文脚手架停留过久 | 一直依赖中文画面也会变成新翻译 | 后续可逐步强化英文锚词、英文例句、弱化中文长解释 |
| 学生词库太像背单词 App | 如果只按小初高列表推进，会回到传统词表 | 词库应服务“概念卡生成 + 迁移使用”，不是让人刷列表 |

---

## 是否“违背语言学习”

### 不违背

英易的理念本身是合理的：

> 先帮学习者摆脱中文释义列表，建立英文词/短语的概念边界，再通过场景和误区帮助迁移。

这比单纯背中文释义更健康。

### 但不完整

它只是语言学习的一环，不是完整系统。

如果把它定位为“英语概念还原器 / 词汇理解增强器”，就合理。

如果把它包装成“靠这个就能学英语 / 背完词库就能提升英语”，就不合理。

---

## 对产品路线的建议

### 保留核心定位

建议继续使用这个定位：

> 面向中文学习者的英语概念还原器：把词典释义背后的统一概念画面讲出来。

不要改成：

- 全能英语学习 App
- 背单词神器
- AI 英语老师
- 一站式学习系统

### 学习系统应该补什么

优先级从高到低：

1. 我的例句：用户看完卡片后写一句自己的英文句子。
2. 迁移复习：复习时不是问中文意思，而是问“这个词的核心画面是什么 / 这个场景能不能用”。
3. 例句扩展：每个核心概念增加少量高质量例句，不要堆太多。
4. 搭配提示：尤其是动词、形容词、固定短语，补常见搭配。
5. Anki/Markdown 导出：让用户接入成熟复习工具。

不建议优先做：

- 复杂成就系统。
- 大型统计页。
- 社区。
- 阅读器。
- 离线大词典。
- 大而全课程。

### 对学生词库的建议

小初高/高考词库可以保留，但要换个使用方式：

- 不要做成“刷完这个列表”。
- 应该做成“从学生必遇词中挑选最容易被中文释义误导的词”。
- 优先处理高频、多义、搭配多、短语多、容易误用的词。
- 对非常直观的词，不必强行生成复杂概念卡。

也就是说，学生词库不是主角；主角仍然是“哪些词最需要概念还原”。

---

## 和当前 paper MVP 的关系

当前 `docs/paper_mvp.md` 的验证指标总体合理：

- JSON 合法率。
- entryType 正确率。
- 核心画面稳定率。
- 顿悟感。
- 可迁移造句。
- 硬性反例。

其中最符合语言学习的两个指标是：

1. 顿悟感：是否从翻译标签转向概念理解。
2. 可迁移造句：是否能把理解迁移到新输出。

如果以后只能保留两个核心人工指标，就保留这两个。

---

## 最终判断

英易没有违背语言学习，反而抓住了中文学习者词汇学习中的一个真实痛点。

但必须守住边界：

> 它是“概念理解加速器”，不是“完整语言习得机器”。

下一步最符合学习规律的功能不是继续堆 UI 或做新手引导，而是补一个很小的输出闭环：

> 每张概念卡允许用户写“我的例句”，复习时优先回看/修改这句例句。

---

## 参考资料

- Paul Nation, **The four strands of a language course**：提出平衡语言课程应包含 meaning-focused input、language-focused learning、meaning-focused output、fluency development 四类活动。  
  https://openaccess.wgtn.ac.nz/articles/journal_contribution/The_four_strands_of_a_language_course/12560387
- Council of Europe, **CEFR Companion Volume (2020)**：把语言能力放在 reception、production、interaction、mediation 等真实交际活动中理解。  
  https://www.coe.int/en/web/common-european-framework-reference-languages/cefr-companion-volume-and-its-language-versions
- I. S. P. Nation, **Learning Vocabulary in Another Language**：词汇知识不只是“意思”，还包括形式、意义、使用、搭配、接受性/产出性知识等维度。  
  https://books.google.com/books/about/Learning_Vocabulary_in_Another_Language.html?id=iNduEAAAQBAJ
- Regina Weinert, **The Role of Formulaic Language in Second Language Acquisition: A Review**：综述固定表达/语块在二语习得中的作用。  
  https://academic.oup.com/applij/article/16/2/180/211142
- Norbert Schmitt (ed.), **Formulaic Sequences: Acquisition, Processing and Use**：语块常被整体存取，和二语理解、产出、流利度相关。  
  https://journals.linguisticsociety.org/booknotices/?p=690
- Qi Xu, **Formulaic Sequences and the Implications for Second Language Learning**：提醒外语教学应重视预制语块，但也不能在输入不足环境中过度神化语块。  
  https://www.ccsenet.org/journal/index.php/elt/article/view/60348
- Tatsuya Nakata & Irina Elgort, **Effects of spacing on contextual vocabulary learning**：间隔分布对二语词汇学习有价值，但效果会随知识类型和语境变化。  
  https://journals.sagepub.com/doi/10.1177/0267658320927764
- Harry P. Bahrick et al., **Maintenance of Foreign Language Vocabulary and the Spacing Effect**：较早的外语词汇保持与间隔效应研究。  
  https://digitalcommons.fiu.edu/psychology_fac/50/
