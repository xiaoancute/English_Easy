package io.github.xiaoancute.englisheasy.data.learning

import io.github.xiaoancute.englisheasy.data.local.WordLearningStateDao
import io.github.xiaoancute.englisheasy.data.local.WordLearningStateEntity
import io.github.xiaoancute.englisheasy.data.util.WordNormalizer
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
            .map { words -> words.map(WordNormalizer::normalize).distinct() }
    }

    fun observeReviewWords(): Flow<List<String>> {
        return dao.observeWordsInState(LearningState.LEARNING.storageValue)
            .map { words -> words.map(WordNormalizer::normalize).distinct() }
    }

    fun observeSkippedWords(): Flow<List<String>> {
        return dao.observeWordsInState(LearningState.SKIPPED.storageValue)
            .map { words -> words.map(WordNormalizer::normalize).distinct() }
    }

    fun observeBlockedWords(): Flow<List<String>> {
        return dao.observeWordsInStates(LearningState.storageValues(LearningState.blockingStates))
            .map { words -> words.map(WordNormalizer::normalize).distinct() }
    }

    suspend fun startLearning(word: String) {
        val normalized = WordNormalizer.normalize(word)
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
        dao.delete(WordNormalizer.normalize(word))
    }

    private suspend fun setState(word: String, state: LearningState) {
        dao.upsert(WordLearningStateEntity.from(word, state))
    }
}
