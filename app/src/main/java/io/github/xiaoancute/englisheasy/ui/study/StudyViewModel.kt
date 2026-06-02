package io.github.xiaoancute.englisheasy.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xiaoancute.englisheasy.data.learning.LearningDashboard
import io.github.xiaoancute.englisheasy.data.learning.LearningPlanner
import io.github.xiaoancute.englisheasy.data.learning.TodayStudyTask
import io.github.xiaoancute.englisheasy.data.learning.WordLearningStateRepository
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
import kotlinx.coroutines.flow.asStateFlow
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
    private val wordLearningStateRepository: WordLearningStateRepository,
    vocabularyRepository: VocabularyRepository,
) : ViewModel() {

    private val selectedPackWords = MutableStateFlow(emptyList<String>())
    private val selectedPackLabelValue = MutableStateFlow<String?>(null)
    private val learningWords = wordLearningStateRepository.observeLearningWords()
    private val reviewWords = wordLearningStateRepository.observeReviewWords()
    private val blockedWords = wordLearningStateRepository.observeBlockedWords()

    val selectedPackLabel: StateFlow<String?> = selectedPackLabelValue.asStateFlow()

    val dueCards: StateFlow<List<StudyCard>> = combine(
        dao.getDueReviews(System.currentTimeMillis()),
        reviewWords,
    ) { entities, review ->
        val reviewSet = review.map(::normalizeWord).toSet()
        entities
            .filter { entity -> normalizeWord(entity.word) in reviewSet }
            .map { entity ->
                StudyCard(
                    entity = entity,
                    card = runCatching { entity.toCard(json) }.getOrNull(),
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val vocabularyPacks: StateFlow<List<VocabularyPack>> = learningWords
        .map { words -> vocabularyRepository.getPacks(words.toSet()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    private val selectedPack: StateFlow<VocabularyPack?> = combine(
        vocabularyPacks,
        selectedPackLabel,
    ) { packs, label ->
        packs.firstOrNull { pack -> packLabel(pack) == label }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    val selectedWords: StateFlow<List<String>> = combine(
        selectedPackWords,
        blockedWords,
    ) { packWords, blocked ->
        val blockedSet = blocked.map(::normalizeWord).toSet()
        packWords
            .map(::normalizeWord)
            .distinct()
            .filterNot { it in blockedSet }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val todayWords: StateFlow<List<String>> = combine(
        selectedPackWords,
        blockedWords,
    ) { packWords, blocked ->
        LearningPlanner.todayWords(
            words = packWords,
            blockedWords = blocked.toSet(),
            limit = TODAY_WORD_LIMIT,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val skippedWords: StateFlow<List<String>> = combine(
        selectedPackWords,
        wordLearningStateRepository.observeSkippedWords(),
    ) { packWords, skipped ->
        val skippedSet = skipped.map(::normalizeWord).toSet()
        packWords
            .map(::normalizeWord)
            .distinct()
            .filter { it in skippedSet }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val todayTask: StateFlow<TodayStudyTask> = combine(
        dueCards,
        todayWords,
        selectedPackLabel,
    ) { due, words, packLabel ->
        LearningPlanner.todayTask(
            dueReviewCount = due.size,
            todayWords = words,
            hasSelectedPack = packLabel != null,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodayStudyTask.ChoosePack,
    )

    val dashboard: StateFlow<LearningDashboard> = combine(
        selectedPack,
        selectedWords,
        skippedWords,
        dueCards,
        todayWords,
    ) { pack, available, skipped, due, today ->
        LearningDashboard.from(
            selectedPack = pack,
            availableWords = available,
            skippedWords = skipped,
            dueReviewCount = due.size,
            todayWordCount = today.size,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LearningDashboard.from(
            selectedPack = null,
            availableWords = emptyList(),
            skippedWords = emptyList(),
            dueReviewCount = 0,
            todayWordCount = 0,
        ),
    )

    init {
        viewModelScope.launch {
            vocabularyPacks.collect { packs ->
                val currentLabel = selectedPackLabelValue.value
                val selectedStillExists = currentLabel != null &&
                    packs.any { pack -> packLabel(pack) == currentLabel }

                when {
                    packs.isEmpty() -> {
                        selectedPackLabelValue.value = null
                        selectedPackWords.value = emptyList()
                    }

                    !selectedStillExists -> selectPack(packs.first())
                }
            }
        }
    }

    fun selectPack(pack: VocabularyPack) {
        selectedPackLabelValue.value = packLabel(pack)
        selectedPackWords.value = pack.entries.map { it.word }
    }

    fun startLearning(word: String) {
        viewModelScope.launch {
            wordLearningStateRepository.startLearning(word)
        }
    }

    fun skipWord(word: String) {
        viewModelScope.launch {
            wordLearningStateRepository.skipWord(word)
        }
    }

    fun restoreSkippedWord(word: String) {
        viewModelScope.launch {
            wordLearningStateRepository.restoreWord(word)
        }
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
            if (next.reviewStrength >= MASTERED_STRENGTH) {
                wordLearningStateRepository.markMastered(entity.word)
            } else {
                wordLearningStateRepository.startLearning(entity.word)
            }
        }
    }

    private fun normalizeWord(word: String): String {
        return word
            .trim()
            .lowercase()
            .replace(Regex("\\s+"), " ")
    }

    private fun packLabel(pack: VocabularyPack): String {
        return "${pack.stage.label}词库"
    }

    private companion object {
        const val TODAY_WORD_LIMIT = 10
        const val MASTERED_STRENGTH = 5
    }
}
