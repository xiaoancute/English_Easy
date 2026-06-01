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

    @Query("SELECT * FROM concept_cards WHERE isFavorite = 1 ORDER BY queriedAt DESC")
    fun getFavoritesByTimeDesc(): Flow<List<ConceptCardEntity>>

    @Query("SELECT word FROM concept_cards")
    fun observeLearnedWords(): Flow<List<String>>

    @Query("SELECT * FROM concept_cards WHERE reviewDueAt <= :now ORDER BY reviewDueAt ASC, queriedAt DESC")
    fun getDueReviews(now: Long): Flow<List<ConceptCardEntity>>

    @Query("SELECT isFavorite FROM concept_cards WHERE word = :word LIMIT 1")
    fun observeFavorite(word: String): Flow<Boolean?>

    @Query("SELECT userNote FROM concept_cards WHERE word = :word LIMIT 1")
    fun observeNote(word: String): Flow<String?>

    @Query("SELECT userExample FROM concept_cards WHERE word = :word LIMIT 1")
    fun observeExample(word: String): Flow<String?>

    @Query("UPDATE concept_cards SET isFavorite = :isFavorite WHERE word = :word")
    suspend fun setFavorite(word: String, isFavorite: Boolean)

    @Query("UPDATE concept_cards SET userNote = :note WHERE word = :word")
    suspend fun setNote(word: String, note: String)

    @Query("UPDATE concept_cards SET userExample = :example WHERE word = :word")
    suspend fun setExample(word: String, example: String)

    @Query(
        """
        UPDATE concept_cards
        SET reviewDueAt = :reviewDueAt,
            reviewStrength = :reviewStrength,
            reviewCount = :reviewCount,
            lastReviewedAt = :lastReviewedAt
        WHERE word = :word
        """
    )
    suspend fun updateReview(
        word: String,
        reviewDueAt: Long,
        reviewStrength: Int,
        reviewCount: Int,
        lastReviewedAt: Long,
    )

    @Query("DELETE FROM concept_cards WHERE word = :word")
    suspend fun delete(word: String)

    @Query("DELETE FROM concept_cards")
    suspend fun deleteAll()

    @Query("DELETE FROM concept_cards WHERE isFavorite = 0")
    suspend fun deleteAllNonFavorites()
}
