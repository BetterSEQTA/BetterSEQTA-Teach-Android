package org.betterseqta.betterseqtateachandroid.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.betterseqta.betterseqtateachandroid.data.remote.TeachMessagesClient
import org.betterseqta.betterseqtateachandroid.domain.model.MessageStateChange
import org.betterseqta.betterseqtateachandroid.domain.model.TeachLabel
import org.betterseqta.betterseqtateachandroid.domain.model.TeachMessage
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.util.MessageStateNotifier

data class DireqtMessagesUiState(
    val labels: List<TeachLabel> = emptyList(),
    val selectedLabel: String = "inbox",
    val searchText: String = "",
    val messages: List<TeachMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class DireqtMessagesViewModel @Inject constructor(
    private val messagesClient: TeachMessagesClient,
    private val messageStateNotifier: MessageStateNotifier,
) : ViewModel() {

    private val _uiState = MutableStateFlow(DireqtMessagesUiState())
    val uiState: StateFlow<DireqtMessagesUiState> = _uiState.asStateFlow()

    private var loadedSessionId: String? = null
    private val starredIds = mutableSetOf<Int>()
    private var searchJob: Job? = null

    val displayedMessages: List<TeachMessage>
        get() {
            val list = _uiState.value.messages.map { message ->
                val mid = message.messageId
                if (mid != null && starredIds.contains(mid)) {
                    message.copy(starred = true)
                } else {
                    message
                }
            }
            return if (_uiState.value.selectedLabel == "starred") {
                list.filter { it.starred }
            } else {
                list
            }
        }

    init {
        viewModelScope.launch {
            messageStateNotifier.events.collect { change -> handleStateChange(change) }
        }
    }

    fun loadIfNeeded(session: TeachSession?) {
        if (session == null) {
            reset()
            return
        }
        if (loadedSessionId == session.jsessionId) return
        viewModelScope.launch {
            loadAll(session)
            loadedSessionId = session.jsessionId
        }
    }

    fun refresh(session: TeachSession?) {
        if (session == null) {
            reset()
            return
        }
        viewModelScope.launch {
            loadAll(session)
            loadedSessionId = session.jsessionId
        }
    }

    fun switchLabel(label: String, session: TeachSession?) {
        _uiState.update { it.copy(selectedLabel = label, searchText = "") }
        if (session == null) return
        val serverLabel = if (label == "starred") "inbox" else label
        viewModelScope.launch { loadMessages(session, serverLabel) }
    }

    fun onSearchChanged(session: TeachSession?) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            val current = _uiState.value
            val serverLabel = if (current.selectedLabel == "starred") "inbox" else current.selectedLabel
            session?.let { loadMessages(it, serverLabel) }
        }
    }

    fun updateSearchText(text: String) {
        _uiState.update { it.copy(searchText = text) }
    }

    fun toggleRead(message: TeachMessage, session: TeachSession?) {
        val msgId = message.messageId ?: return
        val sessionValue = session ?: return
        val newRead = !message.read
        patchMessage(message.id) { it.copy(read = newRead) }
        viewModelScope.launch {
            runCatching { messagesClient.markRead(sessionValue, listOf(msgId), newRead) }
            runCatching { messagesClient.fetchLabels(sessionValue) }
                .onSuccess { labels -> _uiState.update { it.copy(labels = labels) } }
        }
    }

    fun toggleStar(message: TeachMessage, session: TeachSession?) {
        val msgId = message.messageId ?: return
        val sessionValue = session ?: return
        if (starredIds.contains(msgId)) starredIds.remove(msgId) else starredIds.add(msgId)
        _uiState.update { it.copy(messages = it.messages.toList()) }
        viewModelScope.launch {
            runCatching {
                messagesClient.toggleStar(sessionValue, listOf(msgId), starredIds.contains(msgId))
            }
        }
    }

    fun trash(message: TeachMessage, session: TeachSession?) {
        val msgId = message.messageId ?: return
        val sessionValue = session ?: return
        _uiState.update { state ->
            state.copy(messages = state.messages.filterNot { it.id == message.id })
        }
        starredIds.remove(msgId)
        viewModelScope.launch {
            runCatching { messagesClient.moveMessage(sessionValue, listOf(msgId), "trash") }
            runCatching { messagesClient.fetchLabels(sessionValue) }
                .onSuccess { labels -> _uiState.update { it.copy(labels = labels) } }
        }
    }

    fun restore(message: TeachMessage, session: TeachSession?) {
        val msgId = message.messageId ?: return
        val sessionValue = session ?: return
        _uiState.update { state ->
            state.copy(messages = state.messages.filterNot { it.id == message.id })
        }
        viewModelScope.launch {
            runCatching { messagesClient.moveMessage(sessionValue, listOf(msgId), "inbox") }
            runCatching { messagesClient.fetchLabels(sessionValue) }
                .onSuccess { labels -> _uiState.update { it.copy(labels = labels) } }
        }
    }

    private suspend fun loadAll(session: TeachSession) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val serverLabel = if (_uiState.value.selectedLabel == "starred") "inbox" else _uiState.value.selectedLabel
        runCatching {
            val labels = messagesClient.fetchLabels(session)
            val messages = messagesClient.fetchMessages(
                session,
                label = serverLabel,
                searchValue = _uiState.value.searchText,
            )
            labels to messages
        }.onSuccess { (labels, messages) ->
            syncStarred(messages)
            _uiState.update { it.copy(labels = labels, messages = messages, isLoading = false) }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.message,
                    messages = emptyList(),
                )
            }
        }
    }

    private suspend fun loadMessages(session: TeachSession, serverLabel: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        runCatching {
            messagesClient.fetchMessages(
                session,
                label = serverLabel,
                searchValue = _uiState.value.searchText,
            )
        }.onSuccess { messages ->
            syncStarred(messages)
            _uiState.update { it.copy(messages = messages, isLoading = false) }
        }.onFailure { error ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = error.message,
                    messages = emptyList(),
                )
            }
        }
    }

    private fun syncStarred(messages: List<TeachMessage>) {
        messages.forEach { msg ->
            val mid = msg.messageId
            if (mid != null && msg.starred) starredIds.add(mid)
        }
    }

    private fun handleStateChange(change: MessageStateChange) {
        when (change) {
            is MessageStateChange.Read -> {
                patchMessageByMessageId(change.messageId) { it.copy(read = change.isRead) }
            }
            is MessageStateChange.Starred -> {
                if (change.isStarred) starredIds.add(change.messageId) else starredIds.remove(change.messageId)
                _uiState.update { it.copy(messages = it.messages.toList()) }
            }
            is MessageStateChange.Trashed -> {
                _uiState.update { state ->
                    state.copy(messages = state.messages.filterNot { it.messageId == change.messageId })
                }
                starredIds.remove(change.messageId)
            }
            is MessageStateChange.Moved -> {
                _uiState.update { state ->
                    state.copy(messages = state.messages.filterNot { it.messageId == change.messageId })
                }
            }
        }
    }

    private fun patchMessage(id: String, transform: (TeachMessage) -> TeachMessage) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { if (it.id == id) transform(it) else it },
            )
        }
    }

    private fun patchMessageByMessageId(messageId: Int, transform: (TeachMessage) -> TeachMessage) {
        _uiState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.messageId == messageId) transform(msg) else msg
                },
            )
        }
    }

    private fun reset() {
        loadedSessionId = null
        starredIds.clear()
        _uiState.value = DireqtMessagesUiState()
    }
}
