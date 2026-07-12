package io.github.xiaoancute.englisheasy.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.xiaoancute.englisheasy.data.learning.LearningState
import io.github.xiaoancute.englisheasy.data.util.WordNormalizer

@Entity(tableName = "word_learning_states")
data class WordLearningStateEntity(
    @PrimaryKey
    val word: String,
    val state: String,
    val updatedAt: Long,
) {
    companion object {
        fun from(
            word: String,
            state: LearningState,
            updatedAt: Long = System.currentTimeMillis(),
        ): WordLearningStateEntity {
            return WordLearningStateEntity(
                word = WordNormalizer.normalize(word),
                state = state.storageValue,
                updatedAt = updatedAt,
            )
        }
    }
}
