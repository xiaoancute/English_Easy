package io.github.xiaoancute.englisheasy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xiaoancute.englisheasy.data.llm.ExampleFeedback
import io.github.xiaoancute.englisheasy.data.llm.ConceptRepository
import io.github.xiaoancute.englisheasy.data.learning.WordLearningStateRepository
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.model.ExpressionRescueCard
import io.github.xiaoancute.englisheasy.data.model.SentenceCard
import io.github.xiaoancute.englisheasy.data.settings.SettingsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Idle : HomeUiState
    data object Loading : HomeUiState
    data class Success(
        val card: ConceptCard,
        val isFavorite: Boolean,
        val userNote: String,
        val userExample: String,
        val contextSentence: String = "",
        val exampleFeedbackState: ExampleFeedbackUiState = ExampleFeedbackUiState.Idle,
    ) : HomeUiState
    data class SentenceSuccess(
        val card: SentenceCard,
    ) : HomeUiState
    data class ExpressionSuccess(
        val card: ExpressionRescueCard,
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

sealed interface ExampleFeedbackUiState {
    data object Idle : ExampleFeedbackUiState
    data object Loading : ExampleFeedbackUiState
    data class Success(val feedback: ExampleFeedback) : ExampleFeedbackUiState
    data class Error(val message: String) : ExampleFeedbackUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: ConceptRepository,
    settings: SettingsRepository,
    private val wordLearningStateRepository: WordLearningStateRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    private var requestJob: Job? = null
    private var favoriteJob: Job? = null
    private var noteJob: Job? = null
    private var exampleJob: Job? = null
    private var sourceSentenceJob: Job? = null
    private var noteSaveJob: Job? = null
    private var exampleSaveJob: Job? = null

    /** 是否已配置可用的 Provider；初始乐观为 true，避免已配置用户看到引导闪烁。 */
    val isConfigured: StateFlow<Boolean> = settings.configFlow
        .map { it.isUsable }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun lookup(
        word: String,
        contextSentence: String = "",
        forceRefresh: Boolean = false,
        markLearningOnSuccess: Boolean = false,
    ) {
        val trimmed = word.trim()
        val trimmedContext = contextSentence.trim()
        if (trimmed.isEmpty()) return
        _state.value = HomeUiState.Loading
        cancelObservationJobs()
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            repo.lookup(
                word = trimmed,
                contextSentence = trimmedContext,
                forceRefresh = forceRefresh,
            ).fold(
                onSuccess = { card ->
                    _state.value = HomeUiState.Success(
                        card = card,
                        isFavorite = false,
                        userNote = "",
                        userExample = "",
                        contextSentence = trimmedContext,
                    )
                    if (markLearningOnSuccess) {
                        wordLearningStateRepository.startLearning(card.word)
                    }
                    observeFavorite(card.word)
                    observeNote(card.word)
                    observeExample(card.word)
                    observeSourceSentence(card.word)
                },
                onFailure = { _state.value = HomeUiState.Error(it.message ?: "未知错误") },
            )
        }
    }

    fun refreshCurrent() {
        when (val current = _state.value) {
            is HomeUiState.Success -> lookup(
                word = current.card.word,
                contextSentence = current.contextSentence,
                forceRefresh = true,
            )

            is HomeUiState.SentenceSuccess -> analyzeSentence(current.card.sentence)
            is HomeUiState.ExpressionSuccess -> rescueExpression(current.card.intent)
            HomeUiState.Idle, HomeUiState.Loading, is HomeUiState.Error -> Unit
        }
    }

    fun analyzeSentence(sentence: String) {
        val trimmed = sentence.trim()
        if (trimmed.isEmpty()) return
        _state.value = HomeUiState.Loading
        cancelObservationJobs()
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            repo.analyzeSentence(trimmed).fold(
                onSuccess = { card ->
                    _state.value = HomeUiState.SentenceSuccess(card)
                },
                onFailure = { _state.value = HomeUiState.Error(it.message ?: "未知错误") },
            )
        }
    }

    fun rescueExpression(intent: String) {
        val trimmed = intent.trim()
        if (trimmed.isEmpty()) return
        _state.value = HomeUiState.Loading
        cancelObservationJobs()
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            repo.rescueExpression(trimmed).fold(
                onSuccess = { card ->
                    _state.value = HomeUiState.ExpressionSuccess(card)
                },
                onFailure = { _state.value = HomeUiState.Error(it.message ?: "未知错误") },
            )
        }
    }

    fun setFavorite(isFavorite: Boolean) {
        val current = _state.value as? HomeUiState.Success ?: return
        _state.value = current.copy(isFavorite = isFavorite)
        viewModelScope.launch {
            repo.setFavorite(current.card.word, isFavorite)
        }
    }

    fun setNote(note: String) {
        val current = _state.value as? HomeUiState.Success ?: return
        _state.value = current.copy(userNote = note)
        noteSaveJob?.cancel()
        noteSaveJob = viewModelScope.launch {
            delay(NOTE_SAVE_DEBOUNCE_MS)
            val latest = _state.value as? HomeUiState.Success ?: return@launch
            if (!latest.card.word.equals(current.card.word, ignoreCase = true)) return@launch
            repo.setNote(latest.card.word, latest.userNote)
        }
    }

    fun setExample(example: String) {
        val current = _state.value as? HomeUiState.Success ?: return
        _state.value = current.copy(
            userExample = example,
            exampleFeedbackState = ExampleFeedbackUiState.Idle,
        )
        exampleSaveJob?.cancel()
        exampleSaveJob = viewModelScope.launch {
            delay(NOTE_SAVE_DEBOUNCE_MS)
            val latest = _state.value as? HomeUiState.Success ?: return@launch
            if (!latest.card.word.equals(current.card.word, ignoreCase = true)) return@launch
            repo.setExample(latest.card.word, latest.userExample)
        }
    }

    fun reviewExample() {
        val current = _state.value as? HomeUiState.Success ?: return
        val example = current.userExample.trim()
        if (example.isBlank()) {
            _state.value = current.copy(
                exampleFeedbackState = ExampleFeedbackUiState.Error("请先写一句自己的英文例句"),
            )
            return
        }

        _state.value = current.copy(exampleFeedbackState = ExampleFeedbackUiState.Loading)
        requestJob?.cancel()
        requestJob = viewModelScope.launch {
            // 立刻落库，避免 debounce 未完成时检查用到旧例句
            repo.setExample(current.card.word, example)
            repo.reviewExample(
                word = current.card.word,
                userExample = example,
                contextSentence = current.contextSentence,
            ).fold(
                onSuccess = { feedback ->
                    val latest = _state.value as? HomeUiState.Success
                    if (latest != null) {
                        _state.value = latest.copy(
                            exampleFeedbackState = ExampleFeedbackUiState.Success(feedback),
                        )
                    }
                },
                onFailure = {
                    val latest = _state.value as? HomeUiState.Success
                    if (latest != null) {
                        _state.value = latest.copy(
                            exampleFeedbackState = ExampleFeedbackUiState.Error(
                                it.message ?: "例句检查失败",
                            ),
                        )
                    }
                },
            )
        }
    }

    fun reset() {
        requestJob?.cancel()
        cancelObservationJobs()
        noteSaveJob?.cancel()
        exampleSaveJob?.cancel()
        _state.value = HomeUiState.Idle
    }

    private fun cancelObservationJobs() {
        favoriteJob?.cancel()
        noteJob?.cancel()
        exampleJob?.cancel()
        sourceSentenceJob?.cancel()
        noteSaveJob?.cancel()
        exampleSaveJob?.cancel()
    }

    private fun observeFavorite(word: String) {
        favoriteJob?.cancel()
        favoriteJob = viewModelScope.launch {
            repo.observeFavorite(word).collect { isFavorite ->
                val current = _state.value as? HomeUiState.Success
                if (current != null && current.card.word.equals(word, ignoreCase = true)) {
                    _state.value = current.copy(isFavorite = isFavorite)
                }
            }
        }
    }

    private fun observeNote(word: String) {
        noteJob?.cancel()
        noteJob = viewModelScope.launch {
            // 只同步首帧；若用户已开始输入则不覆盖
            val userNote = repo.observeNote(word).firstOrNull().orEmpty()
            val current = _state.value as? HomeUiState.Success
            if (
                current != null &&
                current.card.word.equals(word, ignoreCase = true) &&
                current.userNote.isEmpty()
            ) {
                _state.value = current.copy(userNote = userNote)
            }
        }
    }

    private fun observeExample(word: String) {
        exampleJob?.cancel()
        exampleJob = viewModelScope.launch {
            val userExample = repo.observeExample(word).firstOrNull().orEmpty()
            val current = _state.value as? HomeUiState.Success
            if (
                current != null &&
                current.card.word.equals(word, ignoreCase = true) &&
                current.userExample.isEmpty()
            ) {
                _state.value = current.copy(userExample = userExample)
            }
        }
    }

    private fun observeSourceSentence(word: String) {
        sourceSentenceJob?.cancel()
        sourceSentenceJob = viewModelScope.launch {
            val sourceSentence = repo.observeSourceSentence(word).firstOrNull().orEmpty()
            val current = _state.value as? HomeUiState.Success
            if (
                current != null &&
                current.card.word.equals(word, ignoreCase = true) &&
                current.contextSentence.isEmpty() &&
                sourceSentence.isNotEmpty()
            ) {
                _state.value = current.copy(contextSentence = sourceSentence)
            }
        }
    }

    private companion object {
        const val NOTE_SAVE_DEBOUNCE_MS = 400L
    }
}
