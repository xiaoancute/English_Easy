package io.github.xiaoancute.englisheasy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConceptCardEntity::class, WordLearningStateEntity::class],
    version = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conceptCardDao(): ConceptCardDao
    abstract fun wordLearningStateDao(): WordLearningStateDao
}
