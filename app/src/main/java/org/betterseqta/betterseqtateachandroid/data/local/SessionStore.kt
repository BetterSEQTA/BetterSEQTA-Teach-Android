package org.betterseqta.betterseqtateachandroid.data.local

import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession

interface SessionStore {
    suspend fun saveSession(session: TeachSession)

    suspend fun loadSession(): TeachSession?

    suspend fun clearSession()
}
