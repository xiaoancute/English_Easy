package io.github.xiaoancute.englisheasy.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xiaoancute.englisheasy.data.llm.ConceptRepository
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface HomeUiState {
    data object Idle : HomeUiState
    data object Loading : HomeUiState
    data class Success(val card: ConceptCard) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: ConceptRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<HomeUiState>(HomeUiState.Idle)
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    fun lookup(word: String) {
        val trimmed = word.trim()
        if (trimmed.isEmpty()) return
        _state.value = HomeUiState.Loading
        viewModelScope.launch {
            repo.lookup(trimmed).fold(
                onSuccess = { _state.value = HomeUiState.Success(it) },
                onFailure = { _state.value = HomeUiState.Error(it.message ?: "未知错误") },
            )
        }
    }

    fun reset() {
        _state.value = HomeUiState.Idle
    }
}
