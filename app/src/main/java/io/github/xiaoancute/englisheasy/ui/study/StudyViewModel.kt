package io.github.xiaoancute.englisheasy.ui.study

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xiaoancute.englisheasy.data.learning.LearningDashboard
import io.github.xiaoancute.englisheasy.data.learning.LearningPlanner
import io.github.xiaoancute.englisheasy.data.learning.TodayStudyTask
import io.github.xiaoancute.englisheasy.data.learning.WeakWordPolicy
import io.github.xiaoancute.englisheasy.data.learning.WordLearningStateRepository
import io.github.xiaoancute.englisheasy.data.local.ConceptCardDao
import io.github.xiaoancute.englisheasy.data.local.ConceptCardEntity
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.review.ReviewGrade
import io.github.xiaoancute.englisheasy.data.review.ReviewScheduler
import io.github.xiaoancute.englisheasy.data.util.WordNormalizer
import io.github.xiaoancute.englisheasy.data.vocabulary.VocabularyPack
import io.github.xiaoancute.englisheasy.data.vocabulary.VocabularyRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

data class StudyCard(
    val entity: ConceptCardEntity,
    val card: ConceptCard?,
)

@OptIn(ExperimentalCoroutinesApi::class)
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

    /** 周期性刷新“现在”，避免到期词卡在会话中永远不出现。 */
    private val nowFlow = flow {
        while (currentCoroutineContext().isActive) {
            emit(System.currentTimeMillis())
            delay(DUE_REFRESH_INTERVAL_MS)
        }
    }

    val dueCards: StateFlow<List<StudyCard>> = nowFlow
        .flatMapLatest { now ->
            combine(
                dao.getDueReviews(now),
                reviewWords,
            ) { entities, review ->
                val reviewSet = review.map(WordNormalizer::normalize).toSet()
                entities
                    .filter { entity -> WordNormalizer.normalize(entity.word) in reviewSet }
                    .map { entity ->
                        StudyCard(
                            entity = entity,
                            card = runCatching { entity.toCard(json) }.getOrNull(),
                        )
                    }
            }
        }
        .stateIn(
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
        val blockedSet = blocked.map(WordNormalizer::normalize).toSet()
        packWords
            .map(WordNormalizer::normalize)
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
        val skippedSet = skipped.map(WordNormalizer::normalize).toSet()
        packWords
            .map(WordNormalizer::normalize)
            .distinct()
            .filter { it in skippedSet }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val weakWords: StateFlow<List<String>> = combine(
        selectedPackWords,
        dao.observeWeakWords(WeakWordPolicy.STRENGTH_THRESHOLD),
        reviewWords,
    ) { packWords, weak, review ->
        val packSet = packWords.map(WordNormalizer::normalize).toSet()
        val reviewSet = review.map(WordNormalizer::normalize).toSet()
        weak
            .map(WordNormalizer::normalize)
            .distinct()
            .filter { word -> word in reviewSet }
            .filter { word -> packSet.isEmpty() || word in packSet }
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

    private fun packLabel(pack: VocabularyPack): String {
        return "${pack.stage.label}词库"
    }

    private companion object {
        const val TODAY_WORD_LIMIT = 10
        const val MASTERED_STRENGTH = 5
        const val DUE_REFRESH_INTERVAL_MS = 30_000L
    }
}
