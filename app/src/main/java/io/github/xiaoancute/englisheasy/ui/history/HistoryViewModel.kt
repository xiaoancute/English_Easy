package io.github.xiaoancute.englisheasy.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xiaoancute.englisheasy.data.local.ConceptCardDao
import io.github.xiaoancute.englisheasy.data.local.ConceptCardEntity
import io.github.xiaoancute.englisheasy.data.model.toShareText
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dao: ConceptCardDao,
    private val json: Json,
) : ViewModel() {

    val history: StateFlow<List<ConceptCardEntity>> = dao.getAllByTimeDesc()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val favorites: StateFlow<List<ConceptCardEntity>> = dao.getFavoritesByTimeDesc()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun setFavorite(word: String, isFavorite: Boolean) {
        viewModelScope.launch {
            val normalized = word.trim().lowercase().replace(Regex("""\s+"""), " ")
            if (normalized.isNotEmpty()) {
                dao.setFavorite(normalized, isFavorite)
            }
        }
    }

    fun delete(word: String) {
        viewModelScope.launch {
            val normalized = word.trim().lowercase().replace(Regex("""\s+"""), " ")
            if (normalized.isNotEmpty()) {
                dao.delete(normalized)
            }
        }
    }

    fun restore(entity: ConceptCardEntity) {
        viewModelScope.launch { dao.insert(entity) }
    }

    fun clearAll() {
        viewModelScope.launch {
            dao.deleteAllNonFavorites()
        }
    }

    fun exportText(entity: ConceptCardEntity): String? {
        return runCatching {
            entity.toCard(json).toShareText(
                userNote = entity.userNote,
                sourceSentence = entity.sourceSentence,
                userExample = entity.userExample,
            )
        }.getOrNull()
    }
}
