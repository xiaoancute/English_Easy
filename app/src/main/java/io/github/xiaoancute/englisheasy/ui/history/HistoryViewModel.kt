package io.github.xiaoancute.englisheasy.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xiaoancute.englisheasy.data.local.ConceptCardDao
import io.github.xiaoancute.englisheasy.data.local.ConceptCardEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dao: ConceptCardDao,
) : ViewModel() {

    val history: StateFlow<List<ConceptCardEntity>> = dao.getAllByTimeDesc()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun delete(word: String) {
        viewModelScope.launch {
            dao.delete(word)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }
}
