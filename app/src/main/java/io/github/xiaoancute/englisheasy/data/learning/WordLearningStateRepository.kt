package io.github.xiaoancute.englisheasy.data.learning

import io.github.xiaoancute.englisheasy.data.local.WordLearningStateDao
import io.github.xiaoancute.englisheasy.data.local.WordLearningStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordLearningStateRepository @Inject constructor(
    private val dao: WordLearningStateDao,
) {
    fun observeLearningWords(): Flow<List<String>> {
        return dao.observeWordsInStates(LearningState.storageValues(LearningState.progressStates))
            .map { words -> words.map(::normalizeWord).distinct() }
    }

    fun observeReviewWords(): Flow<List<String>> {
        return dao.observeWordsInState(LearningState.LEARNING.storageValue)
            .map { words -> words.map(::normalizeWord).distinct() }
    }

    fun observeSkippedWords(): Flow<List<String>> {
        return dao.observeWordsInState(LearningState.SKIPPED.storageValue)
            .map { words -> words.map(::normalizeWord).distinct() }
    }

    fun observeBlockedWords(): Flow<List<String>> {
        return dao.observeWordsInStates(LearningState.storageValues(LearningState.blockingStates))
            .map { words -> words.map(::normalizeWord).distinct() }
    }

    suspend fun startLearning(word: String) {
        val normalized = normalizeWord(word)
        val currentState = dao.getState(normalized)
        if (currentState == LearningState.MASTERED.storageValue ||
            currentState == LearningState.SKIPPED.storageValue
        ) {
            return
        }
        setState(word, LearningState.LEARNING)
    }

    suspend fun markMastered(word: String) {
        setState(word, LearningState.MASTERED)
    }

    suspend fun skipWord(word: String) {
        setState(word, LearningState.SKIPPED)
    }

    suspend fun restoreWord(word: String) {
        dao.delete(normalizeWord(word))
    }

    private suspend fun setState(word: String, state: LearningState) {
        dao.upsert(WordLearningStateEntity.from(word, state))
    }

    private fun normalizeWord(word: String): String {
        return word.trim().lowercase().replace(Regex("""\s+"""), " ")
    }
}
