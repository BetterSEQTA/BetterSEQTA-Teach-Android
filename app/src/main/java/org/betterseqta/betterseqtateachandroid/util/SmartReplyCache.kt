package org.betterseqta.betterseqtateachandroid.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

private val Context.smartReplyDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "smart_reply_cache",
)

@Singleton
class SmartReplyCache @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun get(messageId: Int): List<String>? = withContext(Dispatchers.IO) {
        val key = stringPreferencesKey(KEY_PREFIX + messageId)
        val raw = context.smartReplyDataStore.prefsSnapshot()?.get(key) ?: return@withContext null
        runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), raw)
        }.getOrNull()
    }

    suspend fun set(messageId: Int, replies: List<String>) {
        withContext(Dispatchers.IO) {
            val key = stringPreferencesKey(KEY_PREFIX + messageId)
            context.smartReplyDataStore.edit { prefs ->
                prefs[key] = json.encodeToString(ListSerializer(String.serializer()), replies)
            }
        }
    }

    suspend fun clear(messageId: Int) {
        withContext(Dispatchers.IO) {
            val key = stringPreferencesKey(KEY_PREFIX + messageId)
            context.smartReplyDataStore.edit { prefs ->
                prefs.remove(key)
            }
        }
    }

    private suspend fun DataStore<Preferences>.prefsSnapshot(): Preferences? =
        runCatching { data.first() }.getOrNull()

    private companion object {
        const val KEY_PREFIX = "smart_reply_"
    }
}
