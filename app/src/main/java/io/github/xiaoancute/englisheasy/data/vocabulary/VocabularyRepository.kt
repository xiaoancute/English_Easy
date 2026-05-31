package io.github.xiaoancute.englisheasy.data.vocabulary

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val entries: List<VocabularyEntry> by lazy {
        context.assets.open("vocabulary/student_vocabulary_v1.json").bufferedReader().use { reader ->
            VocabularyCatalog.decode(reader.readText())
        }
    }

    fun getPacks(learnedWords: Set<String>): List<VocabularyPack> {
        return VocabularyCatalog.groupByStage(entries, learnedWords)
    }
}
