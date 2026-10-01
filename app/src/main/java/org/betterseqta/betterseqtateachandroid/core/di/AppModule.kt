package org.betterseqta.betterseqtateachandroid.core.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.betterseqta.betterseqtateachandroid.data.local.AppPreferences
import org.betterseqta.betterseqtateachandroid.data.local.AppPreferencesImpl
import org.betterseqta.betterseqtateachandroid.data.local.SessionStore
import org.betterseqta.betterseqtateachandroid.data.local.SessionStoreImpl
import org.betterseqta.betterseqtateachandroid.data.repository.SessionRepositoryImpl
import org.betterseqta.betterseqtateachandroid.domain.repository.SessionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository

    @Binds
    @Singleton
    abstract fun bindSessionStore(impl: SessionStoreImpl): SessionStore

    @Binds
    @Singleton
    abstract fun bindAppPreferences(impl: AppPreferencesImpl): AppPreferences

    companion object {
        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(): CoroutineScope =
            CoroutineScope(SupervisorJob())
    }
}
