package io.github.xiaoancute.englisheasy.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ConceptCardDao {
    @Query("SELECT * FROM concept_cards WHERE word = :word LIMIT 1")
    suspend fun get(word: String): ConceptCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ConceptCardEntity)

    @Query("SELECT * FROM concept_cards ORDER BY queriedAt DESC")
    fun getAllByTimeDesc(): Flow<List<ConceptCardEntity>>

    @Query("DELETE FROM concept_cards WHERE word = :word")
    suspend fun delete(word: String)

    @Query("DELETE FROM concept_cards")
    suspend fun deleteAll()
}
