package org.betterseqta.betterseqtateachandroid.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.data.remote.TeachMessagesClient
import org.betterseqta.betterseqtateachandroid.domain.model.MessageStateChange
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLabel
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessageDetail
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessageFile
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.util.MessageStateNotifier

data class MessageDetailUiState(
    val detail: TeachMessageDetail? = null,
    val labels: List<TeachLabel> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val didTrash: Boolean = false,
)

@HiltViewModel
class MessageDetailViewModel @Inject constructor(
    private val messagesClient: TeachMessagesClient,
    private val messageStateNotifier: MessageStateNotifier,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageDetailUiState())
    val uiState: StateFlow<MessageDetailUiState> = _uiState.asStateFlow()

    private var loadedKey: String? = null

    fun loadIfNeeded(session: TeachSession?, messageId: Int) {
        if (session == null) {
            _uiState.value = MessageDetailUiState(errorMessage = "You're not logged in.")
            return
        }
        val key = "${session.jsessionId}-$messageId"
        if (loadedKey == key) return
        viewModelScope.launch {
            load(session, messageId)
            loadedKey = key
        }
    }

    fun toggleStar(session: TeachSession?) {
        val sessionValue = session ?: return
        val detail = _uiState.value.detail ?: return
        val newStarred = !detail.starred
        _uiState.update {
            it.copy(detail = detail.copy(starred = newStarred))
        }
        messageStateNotifier.post(MessageStateChange.Starred(detail.id, newStarred))
        viewModelScope.launch {
            runCatching { messagesClient.toggleStar(sessionValue, listOf(detail.id), newStarred) }
        }
    }

    fun trash(session: TeachSession?) {
        val sessionValue = session ?: return
        val detail = _uiState.value.detail ?: return
        messageStateNotifier.post(MessageStateChange.Trashed(detail.id))
        viewModelScope.launch {
            runCatching { messagesClient.moveMessage(sessionValue, listOf(detail.id), "trash") }
            _uiState.update { it.copy(didTrash = true) }
        }
    }

    fun toggleRead(session: TeachSession?) {
        val sessionValue = session ?: return
        val detail = _uiState.value.detail ?: return
        val newRead = !detail.read
        _uiState.update { it.copy(detail = detail.copy(read = newRead)) }
        messageStateNotifier.post(MessageStateChange.Read(detail.id, newRead))
        viewModelScope.launch {
            runCatching { messagesClient.markRead(sessionValue, listOf(detail.id), newRead) }
        }
    }

    suspend fun downloadAttachment(file: TeachMessageFile, session: TeachSession?): File {
        val sessionValue = session ?: throw IllegalStateException("Not logged in")
        return messagesClient.downloadAttachment(sessionValue, file)
    }

    private suspend fun load(session: TeachSession, messageId: Int) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        runCatching {
            val detail = messagesClient.fetchMessageDetail(session, messageId)
            val labels = runCatching { messagesClient.fetchLabels(session) }.getOrDefault(emptyList())
            detail to labels
        }.onSuccess { result ->
            val detail = result.first
            val labels = result.second
            messageStateNotifier.post(MessageStateChange.Read(messageId, true))
            _uiState.update {
                it.copy(detail = detail, labels = labels, isLoading = false)
            }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.message,
                    detail = null,
                )
            }
        }
    }
}
