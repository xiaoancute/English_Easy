package io.github.xiaoancute.englisheasy.data.vocabulary

import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VocabularyCatalogTest {

    @Test
    fun decodesStudentVocabularySeed() {
        val json = """
            [
              {"word":"  take   a   break  ","stage":"PRIMARY","source":"sample"},
              {"word":"take a break","stage":"PRIMARY","source":"sample"},
              {"word":"available","stage":"SENIOR","source":"sample","tags":["academic"]}
            ]
        """.trimIndent()

        val entries = VocabularyCatalog.decode(json)

        assertEquals(2, entries.size)
        assertEquals(VocabularyStage.PRIMARY, entries[0].stage)
        assertEquals("take a break", entries[0].word)
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

    @Test
    fun bundledStudentVocabularyUsesExpandedLicensedSources() {
        val entries = VocabularyCatalog.decode(vocabularyAssetPath().toFile().readText())

        val primaryEntries = entries.filter { it.stage == VocabularyStage.PRIMARY }
        val gaokaoEntries = entries.filter { it.stage == VocabularyStage.GAOKAO }

        assertTrue(primaryEntries.size > 800)
        assertTrue(gaokaoEntries.size > 3_000)
        assertTrue(primaryEntries.any { it.word == "school" && it.source == "guanchunsheng/guanyiyi-english@main" })
        assertTrue(gaokaoEntries.any { it.word == "abandon" && it.source == "pluto0x0/word3500@master" })
        assertTrue(gaokaoEntries.any { it.word == "break the ice" && "phrase" in it.tags })
    }

    private fun vocabularyAssetPath(): Path {
        return listOf(
            Path.of("src/main/assets/vocabulary/student_vocabulary_v1.json"),
            Path.of("app/src/main/assets/vocabulary/student_vocabulary_v1.json"),
        ).first { Files.exists(it) }
    }
}
