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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private var favoriteJob: Job? = null
    private var noteJob: Job? = null
    private var exampleJob: Job? = null
    private var sourceSentenceJob: Job? = null

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
        favoriteJob?.cancel()
        noteJob?.cancel()
        exampleJob?.cancel()
        sourceSentenceJob?.cancel()
        viewModelScope.launch {
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
        favoriteJob?.cancel()
        noteJob?.cancel()
        exampleJob?.cancel()
        sourceSentenceJob?.cancel()
        viewModelScope.launch {
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
        favoriteJob?.cancel()
        noteJob?.cancel()
        exampleJob?.cancel()
        sourceSentenceJob?.cancel()
        viewModelScope.launch {
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
        viewModelScope.launch {
            repo.setNote(current.card.word, note)
        }
    }

    fun setExample(example: String) {
        val current = _state.value as? HomeUiState.Success ?: return
        _state.value = current.copy(
            userExample = example,
            exampleFeedbackState = ExampleFeedbackUiState.Idle,
        )
        viewModelScope.launch {
            repo.setExample(current.card.word, example)
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
        viewModelScope.launch {
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
        favoriteJob?.cancel()
        noteJob?.cancel()
        exampleJob?.cancel()
        sourceSentenceJob?.cancel()
        _state.value = HomeUiState.Idle
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
            repo.observeNote(word).collect { userNote ->
                val current = _state.value as? HomeUiState.Success
                if (current != null && current.card.word.equals(word, ignoreCase = true)) {
                    _state.value = current.copy(userNote = userNote)
                }
            }
        }
    }

    private fun observeExample(word: String) {
        exampleJob?.cancel()
        exampleJob = viewModelScope.launch {
            repo.observeExample(word).collect { userExample ->
                val current = _state.value as? HomeUiState.Success
                if (current != null && current.card.word.equals(word, ignoreCase = true)) {
                    _state.value = current.copy(userExample = userExample)
                }
            }
        }
    }

    private fun observeSourceSentence(word: String) {
        sourceSentenceJob?.cancel()
        sourceSentenceJob = viewModelScope.launch {
            repo.observeSourceSentence(word).collect { sourceSentence ->
                val current = _state.value as? HomeUiState.Success
                if (current != null && current.card.word.equals(word, ignoreCase = true)) {
                    _state.value = current.copy(contextSentence = sourceSentence)
                }
            }
        }
    }
}
