package io.github.xiaoancute.englisheasy.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.xiaoancute.englisheasy.data.local.AppDatabase
import io.github.xiaoancute.englisheasy.data.local.ConceptCardDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "english_easy.db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideConceptCardDao(db: AppDatabase): ConceptCardDao = db.conceptCardDao()
}
