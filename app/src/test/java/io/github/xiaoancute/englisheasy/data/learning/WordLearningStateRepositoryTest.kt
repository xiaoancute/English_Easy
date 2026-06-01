package io.github.xiaoancute.englisheasy.data.learning

import io.github.xiaoancute.englisheasy.data.local.WordLearningStateDao
import io.github.xiaoancute.englisheasy.data.local.WordLearningStateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class WordLearningStateRepositoryTest {

    @Test
    fun startLearningDoesNotOverwriteSkippedWords() = runBlocking {
        val dao = FakeWordLearningStateDao()
        val repository = WordLearningStateRepository(dao)

        repository.skipWord(" Break   The Ice ")
        repository.startLearning("break the ice")

        assertEquals(LearningState.SKIPPED.storageValue, dao.getState("break the ice"))
    }

    @Test
    fun startLearningDoesNotOverwriteMasteredWords() = runBlocking {
        val dao = FakeWordLearningStateDao()
        val repository = WordLearningStateRepository(dao)

        repository.markMastered(" Spring ")
        repository.startLearning("spring")

        assertEquals(LearningState.MASTERED.storageValue, dao.getState("spring"))
    }

    @Test
    fun restoreWordReturnsWordToUnlearnedByDeletingExplicitState() = runBlocking {
        val dao = FakeWordLearningStateDao()
        val repository = WordLearningStateRepository(dao)

        repository.skipWord("Take   A Break")
        repository.restoreWord(" take a break ")

        assertEquals(null, dao.getState("take a break"))
    }

    private class FakeWordLearningStateDao : WordLearningStateDao {
        private val stateByWord = MutableStateFlow<Map<String, WordLearningStateEntity>>(emptyMap())

        override suspend fun getState(word: String): String? {
            return stateByWord.value[normalizeWord(word)]?.state
        }

        override fun observeWordsInStates(states: List<String>): Flow<List<String>> {
            return stateByWord.map { values ->
                values.values
                    .filter { it.state in states }
                    .map { it.word }
            }
        }

        override fun observeWordsInState(state: String): Flow<List<String>> {
            return observeWordsInStates(listOf(state))
        }

        override suspend fun upsert(entity: WordLearningStateEntity) {
            stateByWord.value = stateByWord.value + (entity.word to entity)
        }

        override suspend fun delete(word: String) {
            stateByWord.value = stateByWord.value - normalizeWord(word)
        }

        private fun normalizeWord(word: String): String {
            return word.trim().lowercase().replace(Regex("""\s+"""), " ")
        }
    }
}
