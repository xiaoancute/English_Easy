package io.github.xiaoancute.englisheasy.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xiaoancute.englisheasy.data.learning.LearningPlanner
import io.github.xiaoancute.englisheasy.data.local.ConceptCardDao
import io.github.xiaoancute.englisheasy.data.local.ConceptCardEntity
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.review.ReviewGrade
import io.github.xiaoancute.englisheasy.data.review.ReviewScheduler
import io.github.xiaoancute.englisheasy.data.vocabulary.VocabularyPack
import io.github.xiaoancute.englisheasy.data.vocabulary.VocabularyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class StudyCard(
    val entity: ConceptCardEntity,
    val card: ConceptCard?,
)

@HiltViewModel
class StudyViewModel @Inject constructor(
    private val dao: ConceptCardDao,
    private val json: Json,
    vocabularyRepository: VocabularyRepository,
) : ViewModel() {

    private val selectedPackWords = MutableStateFlow(emptyList<String>())

    val dueCards: StateFlow<List<StudyCard>> = dao.getDueReviews(System.currentTimeMillis())
        .map { entities ->
            entities.map { entity ->
                StudyCard(
                    entity = entity,
                    card = runCatching { entity.toCard(json) }.getOrNull(),
                )
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val vocabularyPacks: StateFlow<List<VocabularyPack>> = dao.observeLearnedWords()
        .map { words -> vocabularyRepository.getPacks(words.toSet()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val selectedWords: StateFlow<List<String>> = combine(
        selectedPackWords,
        dao.observeLearnedWords(),
    ) { packWords, learnedWords ->
        val learned = learnedWords.map(::normalizeWord).toSet()
        packWords
            .map(::normalizeWord)
            .distinct()
            .filterNot { it in learned }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val todayWords: StateFlow<List<String>> = combine(
        selectedPackWords,
        dao.observeLearnedWords(),
    ) { packWords, learnedWords ->
        LearningPlanner.todayWords(
            words = packWords,
            learnedWords = learnedWords.toSet(),
            limit = TODAY_WORD_LIMIT,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    fun selectPack(pack: VocabularyPack) {
        selectedPackWords.value = pack.entries.map { it.word }
    }

    fun review(entity: ConceptCardEntity, grade: ReviewGrade) {
        viewModelScope.launch {
            val next = ReviewScheduler.schedule(
                currentStrength = entity.reviewStrength,
                currentReviewCount = entity.reviewCount,
                grade = grade,
            )
            dao.updateReview(
                word = entity.word,
                reviewDueAt = next.reviewDueAt,
                reviewStrength = next.reviewStrength,
                reviewCount = next.reviewCount,
                lastReviewedAt = next.lastReviewedAt,
            )
        }
    }

    private fun normalizeWord(word: String): String {
        return word
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
    }

    private companion object {
        const val TODAY_WORD_LIMIT = 10
    }
}
