package org.betterseqta.betterseqtateachandroid.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.any
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.betterseqta.betterseqtateachandroid.data.local.SessionStore
import org.betterseqta.betterseqtateachandroid.data.remote.HeartbeatClient
import org.betterseqta.betterseqtateachandroid.data.remote.HeartbeatResult
import org.betterseqta.betterseqtateachandroid.data.remote.TeachUserClient
import org.betterseqta.betterseqtateachandroid.domain.model.LoginStatus
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SessionRepositoryHeartbeatTest {

    private val testDispatcher = StandardTestDispatcher()
    private val sessionStore: SessionStore = mockk(relaxed = true)
    private val heartbeatClient: HeartbeatClient = mockk()
    private val teachUserClient: TeachUserClient = mockk(relaxed = true)

    private lateinit var repository: SessionRepositoryImpl

    @Before
    fun setUp() {
        coEvery { sessionStore.loadSession() } returns null
        repository = SessionRepositoryImpl(
            sessionStore = sessionStore,
            heartbeatClient = heartbeatClient,
            teachUserClient = teachUserClient,
            applicationScope = CoroutineScope(testDispatcher),
        )
        testDispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun sendHeartbeat_unauthorized_clearsSessionAndEmitsLogout() = runTest(testDispatcher) {
        val session = TeachSession(
            baseUrl = "https://school.example.edu.au",
            jsessionId = "cookie-value",
        )
        repository.completeLogin(session)
        coEvery { heartbeatClient.sendHeartbeat(any()) } returns HeartbeatResult.Unauthorized

        repository.logoutEvents.test {
            repository.sendHeartbeat()

            assertEquals(LoginStatus.LoggedOut, repository.state.value.loginStatus)
            assertNull(repository.state.value.session)
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        coVerify { sessionStore.clearSession() }
    }
}
