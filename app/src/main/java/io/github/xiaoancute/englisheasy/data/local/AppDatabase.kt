package io.github.xiaoancute.englisheasy.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ConceptCardEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun conceptCardDao(): ConceptCardDao
}
