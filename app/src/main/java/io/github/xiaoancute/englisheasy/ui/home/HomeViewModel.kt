package io.github.xiaoancute.englisheasy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xiaoancute.englisheasy.data.llm.ConceptRepository
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
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
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: ConceptRepository,
    settings: SettingsRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
    private var favoriteJob: Job? = null
    private var noteJob: Job? = null

    /** 是否已配置可用的 Provider；初始乐观为 true，避免已配置用户看到引导闪烁。 */
    val isConfigured: StateFlow<Boolean> = settings.configFlow
        .map { it.isUsable }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)

    fun lookup(word: String, forceRefresh: Boolean = false) {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return
        _state.value = HomeUiState.Loading
        favoriteJob?.cancel()
        noteJob?.cancel()
        viewModelScope.launch {
            repo.lookup(trimmed, forceRefresh = forceRefresh).fold(
                onSuccess = { card ->
                    _state.value = HomeUiState.Success(card = card, isFavorite = false, userNote = "")
                    observeFavorite(card.word)
                    observeNote(card.word)
                },
                onFailure = { _state.value = HomeUiState.Error(it.message ?: "未知错误") },
            )
        }
    }

    fun refreshCurrent() {
        val current = _state.value as? HomeUiState.Success ?: return
        lookup(current.card.word, forceRefresh = true)
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

    fun reset() {
        favoriteJob?.cancel()
        noteJob?.cancel()
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
}
