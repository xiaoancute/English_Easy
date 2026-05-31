package io.github.xiaoancute.englisheasy.data.vocabulary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VocabularyCatalogTest {

    @Test
    fun decodesStudentVocabularySeed() {
        val json = """
            [
              {"word":"apple","stage":"PRIMARY","source":"sample"},
              {"word":"available","stage":"SENIOR","source":"sample","tags":["academic"]}
            ]
        """.trimIndent()

        val entries = VocabularyCatalog.decode(json)

        assertEquals(2, entries.size)
        assertEquals(VocabularyStage.PRIMARY, entries[0].stage)
        assertEquals(VocabularyStage.SENIOR, entries[1].stage)
        assertEquals(listOf("academic"), entries[1].tags)
    }

    @Test
    fun groupsEntriesByStageInFixedStudentOrder() {
        val entries = listOf(
            VocabularyEntry("issue", VocabularyStage.GAOKAO, "sample"),
            VocabularyEntry("school", VocabularyStage.PRIMARY, "sample"),
            VocabularyEntry("develop", VocabularyStage.JUNIOR, "sample"),
            VocabularyEntry("available", VocabularyStage.SENIOR, "sample"),
        )

        val packs = VocabularyCatalog.groupByStage(entries, learnedWords = setOf("school"))

        assertEquals(
            listOf(
                VocabularyStage.PRIMARY,
                VocabularyStage.JUNIOR,
                VocabularyStage.SENIOR,
                VocabularyStage.GAOKAO,
            ),
            packs.map { it.stage },
        )
        assertEquals(1, packs.first().learnedCount)
        assertTrue(packs.all { it.totalCount == 1 })
    }
}
