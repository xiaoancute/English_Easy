package io.github.xiaoancute.englisheasy.ui.compare

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

sealed interface CompareCardState {
    data object Idle : CompareCardState
    data object Loading : CompareCardState
    data class Success(val card: ConceptCard) : CompareCardState
    data class Error(val message: String) : CompareCardState
}

data class CompareUiState(
    val left: CompareCardState = CompareCardState.Idle,
    val right: CompareCardState = CompareCardState.Idle,
)

@HiltViewModel
class CompareViewModel @Inject constructor(
    private val repo: ConceptRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CompareUiState())
    val state: StateFlow<CompareUiState> = _state.asStateFlow()

    fun compare(leftInput: String, rightInput: String) {
        val leftWord = leftInput.trim()
        val rightWord = rightInput.trim()
        if (leftWord.isEmpty() || rightWord.isEmpty()) return

        _state.value = CompareUiState(
            left = CompareCardState.Loading,
            right = CompareCardState.Loading,
        )

        lookupSide(leftWord, isLeft = true)
        lookupSide(rightWord, isLeft = false)
    }

    private fun lookupSide(word: String, isLeft: Boolean) {
        viewModelScope.launch {
            val nextState = repo.lookup(word).fold(
                onSuccess = { CompareCardState.Success(it) },
                onFailure = { CompareCardState.Error(it.message ?: "未知错误") },
            )
            _state.value = if (isLeft) {
                _state.value.copy(left = nextState)
            } else {
                _state.value.copy(right = nextState)
            }
        }
    }
}
