package io.github.xiaoancute.englisheasy.data.util

/**
 * 词条归一化：trim、小写、压缩空白。
 * 查词缓存主键、学习状态、词库匹配都必须用同一规则。
 */
object WordNormalizer {
    fun normalize(value: String): String {
        return value.trim().lowercase().replace(Regex("""\s+"""), " ")
    }
}
