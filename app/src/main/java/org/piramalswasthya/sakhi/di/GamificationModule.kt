package org.piramalswasthya.sakhi.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.piramalswasthya.sakhi.database.room.InAppDb
import org.piramalswasthya.sakhi.database.room.dao.GamificationDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GamificationModule {

    @Provides
    @Singleton
    fun provideGamificationDao(db: InAppDb): GamificationDao =
        db.gamificationDao
}
