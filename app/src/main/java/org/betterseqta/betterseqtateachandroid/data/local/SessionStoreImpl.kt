package org.betterseqta.betterseqtateachandroid.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import javax.inject.Inject
import javax.inject.Singleton

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "teach_session_prefs",
)

/**
 * Persists [TeachSession] in [EncryptedSharedPreferences] when the keystore is available.
 * Falls back to plain DataStore preferences if encrypted prefs cannot be created (e.g. corrupt
 * legacy plain XML at the same name); successful loads from the fallback are migrated upward.
 */
@Singleton
class SessionStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : SessionStore {

    private val baseUrlKey = stringPreferencesKey("baseUrl")
    private val jsessionIdKey = stringPreferencesKey("jsessionId")

    private val securePrefs: SharedPreferences? = runCatching { createEncryptedPrefs(context) }.getOrNull()

    override suspend fun saveSession(session: TeachSession) {
        withContext(Dispatchers.IO) {
            val prefs = securePrefs
            if (prefs != null) {
                prefs.edit()
                    .putString(KEY_BASE_URL, session.baseUrl)
                    .putString(KEY_JSESSION_ID, session.jsessionId)
                    .apply()
                clearPlainDataStore()
            } else {
                context.sessionDataStore.edit { data ->
                    data[baseUrlKey] = session.baseUrl
                    data[jsessionIdKey] = session.jsessionId
                }
            }
        }
    }

    override suspend fun loadSession(): TeachSession? = withContext(Dispatchers.IO) {
        loadFromEncryptedPrefs()?.let { return@withContext it }
        val fromPlain = readPlainDataStore()
        if (fromPlain != null && securePrefs != null) {
            securePrefs.edit()
                .putString(KEY_BASE_URL, fromPlain.baseUrl)
                .putString(KEY_JSESSION_ID, fromPlain.jsessionId)
                .apply()
            clearPlainDataStore()
        }
        fromPlain
    }

    override suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            securePrefs?.edit()
                ?.remove(KEY_BASE_URL)
                ?.remove(KEY_JSESSION_ID)
                ?.apply()
            clearPlainDataStore()
        }
    }

    private fun loadFromEncryptedPrefs(): TeachSession? {
        val prefs = securePrefs ?: return null
        val baseUrl = prefs.getString(KEY_BASE_URL, null)?.takeIf { it.isNotBlank() } ?: return null
        val jsessionId = prefs.getString(KEY_JSESSION_ID, null)?.takeIf { it.isNotEmpty() } ?: return null
        return TeachSession(baseUrl = baseUrl, jsessionId = jsessionId)
    }

    private suspend fun readPlainDataStore(): TeachSession? {
        val prefs = context.sessionDataStore.data.first()
        val baseUrl = prefs[baseUrlKey]
        val jsessionId = prefs[jsessionIdKey]
        return if (baseUrl.isNullOrBlank() || jsessionId.isNullOrEmpty()) {
            null
        } else {
            TeachSession(baseUrl = baseUrl, jsessionId = jsessionId)
        }
    }

    private suspend fun clearPlainDataStore() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(baseUrlKey)
            prefs.remove(jsessionIdKey)
        }
    }

    private companion object {
        private const val ENCRYPTED_PREFS_NAME = "teach_session_secure"
        private const val KEY_BASE_URL = "baseUrl"
        private const val KEY_JSESSION_ID = "jsessionId"

        private fun createEncryptedPrefs(context: Context): SharedPreferences {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            return EncryptedSharedPreferences.create(
                context,
                ENCRYPTED_PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }
    }
}
