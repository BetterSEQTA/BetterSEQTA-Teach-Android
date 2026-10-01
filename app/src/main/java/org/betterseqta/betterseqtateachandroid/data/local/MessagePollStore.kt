package org.betterseqta.betterseqtateachandroid.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.messagePollDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "message_poll_prefs",
)

@Singleton
class MessagePollStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val lastSeenKey = stringSetPreferencesKey("lastSeenMessageIDs")

    suspend fun loadLastSeenIds(): Set<String> = withContext(Dispatchers.IO) {
        context.messagePollDataStore.data.first()[lastSeenKey] ?: emptySet()
    }

    suspend fun saveLastSeenIds(ids: List<String>) {
        withContext(Dispatchers.IO) {
            context.messagePollDataStore.edit { prefs ->
                prefs[lastSeenKey] = ids.toSet()
            }
        }
    }
}
