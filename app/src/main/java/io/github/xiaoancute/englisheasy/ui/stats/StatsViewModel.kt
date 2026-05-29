package io.github.xiaoancute.englisheasy.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.xiaoancute.englisheasy.data.local.ConceptCardDao
import io.github.xiaoancute.englisheasy.data.model.ConceptCard
import io.github.xiaoancute.englisheasy.data.model.EntryType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json
import java.util.Calendar
import javax.inject.Inject

data class StatsUiState(
    val totalCount: Int = 0,
    val favoriteCount: Int = 0,
    val todayCount: Int = 0,
    val notedCount: Int = 0,
    val wordCount: Int = 0,
    val fixedPhraseCount: Int = 0,
    val freeCombinationCount: Int = 0,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    dao: ConceptCardDao,
    private val json: Json,
) : ViewModel() {

    val state: StateFlow<StatsUiState> = dao.getAllByTimeDesc()
        .map { cards ->
            val todayStart = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val entryTypes = cards.mapNotNull { entity ->
                runCatching {
                    json.decodeFromString<ConceptCard>(entity.cardJson).entryType
                }.getOrNull()
            }

            StatsUiState(
                totalCount = cards.size,
                favoriteCount = cards.count { it.isFavorite },
                todayCount = cards.count { it.queriedAt >= todayStart },
                notedCount = cards.count { it.userNote.isNotBlank() },
                wordCount = entryTypes.count { it == EntryType.WORD },
                fixedPhraseCount = entryTypes.count { it == EntryType.FIXED_PHRASE },
                freeCombinationCount = entryTypes.count { it == EntryType.FREE_COMBINATION },
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = StatsUiState(),
        )
}
