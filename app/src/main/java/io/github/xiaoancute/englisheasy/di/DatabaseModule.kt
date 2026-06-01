package io.github.xiaoancute.englisheasy.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.xiaoancute.englisheasy.data.local.AppDatabase
import io.github.xiaoancute.englisheasy.data.local.ConceptCardDao
import io.github.xiaoancute.englisheasy.data.local.WordLearningStateDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE concept_cards ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0"
            )
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE concept_cards ADD COLUMN userNote TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE concept_cards ADD COLUMN reviewDueAt INTEGER NOT NULL DEFAULT 0"
            )
            db.execSQL(
                "ALTER TABLE concept_cards ADD COLUMN reviewStrength INTEGER NOT NULL DEFAULT 0"
            )
            db.execSQL(
                "ALTER TABLE concept_cards ADD COLUMN reviewCount INTEGER NOT NULL DEFAULT 0"
            )
            db.execSQL(
                "ALTER TABLE concept_cards ADD COLUMN lastReviewedAt INTEGER NOT NULL DEFAULT 0"
            )
            db.execSQL(
                "UPDATE concept_cards SET reviewDueAt = queriedAt WHERE reviewDueAt = 0"
            )
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS word_learning_states (
                    word TEXT NOT NULL,
                    state TEXT NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    PRIMARY KEY(word)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO word_learning_states (word, state, updatedAt)
                SELECT word, 'LEARNING', queriedAt FROM concept_cards
                """.trimIndent()
            )
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE concept_cards ADD COLUMN userExample TEXT NOT NULL DEFAULT ''"
            )
        }
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "english_easy.db"
        ).addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
        ).build()
    }

    @Provides
    @Singleton
    fun provideConceptCardDao(db: AppDatabase): ConceptCardDao = db.conceptCardDao()

    @Provides
    @Singleton
    fun provideWordLearningStateDao(db: AppDatabase): WordLearningStateDao = db.wordLearningStateDao()
}
