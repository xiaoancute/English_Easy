package io.github.xiaoancute.englisheasy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordLearningStateDao {
    @Query("SELECT state FROM word_learning_states WHERE word = :word LIMIT 1")
    suspend fun getState(word: String): String?

    @Query("SELECT word FROM word_learning_states WHERE state IN (:states)")
    fun observeWordsInStates(states: List<String>): Flow<List<String>>

    @Query("SELECT word FROM word_learning_states WHERE state = :state ORDER BY updatedAt DESC")
    fun observeWordsInState(state: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WordLearningStateEntity)

    @Query("DELETE FROM word_learning_states WHERE word = :word")
    suspend fun delete(word: String)
}
