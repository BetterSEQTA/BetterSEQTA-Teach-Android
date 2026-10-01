package org.betterseqta.betterseqtateachandroid.ui.messages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.betterseqta.betterseqtateachandroid.data.remote.TeachMessagesClient
import org.betterseqta.betterseqtateachandroid.domain.model.ComposeMode
import org.betterseqta.betterseqtateachandroid.domain.model.Recipient
import org.betterseqta.betterseqtateachandroid.domain.model.RecipientType
import org.betterseqta.betterseqtateachandroid.domain.model.TeachSession
import org.betterseqta.betterseqtateachandroid.util.wrappedInHtmlParagraphs

data class ComposeMessageUiState(
    val mode: ComposeMode = ComposeMode.New,
    val subject: String = "",
    val bodyHtml: String = "",
    val selectedRecipients: List<Recipient> = emptyList(),
    val recipientSearchText: String = "",
    val blind: Boolean = false,
    val allRecipients: List<Recipient> = emptyList(),
    val isLoadingRecipients: Boolean = false,
    val isSending: Boolean = false,
    val sendError: String? = null,
    val didSend: Boolean = false,
    val replyToMessageId: Int? = null,
)

@HiltViewModel
class ComposeMessageViewModel @Inject constructor(
    private val messagesClient: TeachMessagesClient,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ComposeMessageUiState())
    val uiState: StateFlow<ComposeMessageUiState> = _uiState.asStateFlow()

    fun initialize(mode: ComposeMode, messageId: Int?) {
        _uiState.update {
            it.copy(
                mode = mode,
                replyToMessageId = when (mode) {
                    ComposeMode.Reply, ComposeMode.ReplyAll -> messageId
                    else -> null
                },
            )
        }
    }

    fun updateSubject(value: String) {
        _uiState.update { it.copy(subject = value) }
    }

    fun updateBody(value: String) {
        _uiState.update { it.copy(bodyHtml = value) }
    }

    fun updateBlind(value: Boolean) {
        _uiState.update { it.copy(blind = value) }
    }

    fun updateRecipientSearch(value: String) {
        _uiState.update { it.copy(recipientSearchText = value) }
    }

    fun addRecipient(recipient: Recipient) {
        _uiState.update { state ->
            if (state.selectedRecipients.any { it.id == recipient.id && it.type == recipient.type }) {
                state
            } else {
                state.copy(selectedRecipients = state.selectedRecipients + recipient)
            }
        }
    }

    fun removeRecipient(recipient: Recipient) {
        _uiState.update { state ->
            state.copy(
                selectedRecipients = state.selectedRecipients.filterNot {
                    it.id == recipient.id && it.type == recipient.type
                },
            )
        }
    }

    fun toggleRecipient(recipient: Recipient) {
        val selected = _uiState.value.selectedRecipients.any {
            it.id == recipient.id && it.type == recipient.type
        }
        if (selected) removeRecipient(recipient) else addRecipient(recipient)
    }

    fun clearSendError() {
        _uiState.update { it.copy(sendError = null) }
    }

    fun send(session: TeachSession?) {
        val state = _uiState.value
        if (session == null) {
            _uiState.update { it.copy(sendError = "Not logged in.") }
            return
        }
        if (state.selectedRecipients.isEmpty()) {
            _uiState.update { it.copy(sendError = "Add at least one recipient.") }
            return
        }
        if (state.subject.trim().isEmpty()) {
            _uiState.update { it.copy(sendError = "Subject cannot be empty.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, sendError = null) }
            val contents = if (state.bodyHtml.isBlank()) {
                "<p></p>"
            } else if (state.bodyHtml.contains("<")) {
                state.bodyHtml
            } else {
                state.bodyHtml.wrappedInHtmlParagraphs()
            }
            runCatching {
                messagesClient.sendMessage(
                    session = session,
                    subject = state.subject,
                    contents = contents,
                    participants = state.selectedRecipients,
                    blind = state.blind,
                    inReplyTo = state.replyToMessageId,
                )
            }.onSuccess {
                _uiState.update { it.copy(isSending = false, didSend = true) }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isSending = false,
                        sendError = error.message ?: "Send failed",
                    )
                }
            }
        }
    }

    val filteredRecipients: List<Recipient>
        get() {
            val query = _uiState.value.recipientSearchText.trim().lowercase()
            val base = _uiState.value.allRecipients
            return if (query.isEmpty()) base else base.filter { it.displayName.lowercase().contains(query) }
        }

    suspend fun loadRecipients(session: TeachSession?, mode: ComposeMode, messageId: Int?, selfStaffId: Int?) {
        if (session == null) return
        _uiState.update { it.copy(isLoadingRecipients = true) }
        val contacts = runCatching { messagesClient.fetchContacts(session) }.getOrDefault(emptyList())
        val staff = runCatching { messagesClient.fetchStaff(session) }.getOrDefault(emptyList())
        val students = runCatching { messagesClient.fetchStudents(session) }.getOrDefault(emptyList())
        val tutors = runCatching { messagesClient.fetchTutors(session) }.getOrDefault(emptyList())
        val all = dedupRecipients(contacts + staff + students + tutors)
        _uiState.update { it.copy(allRecipients = all) }
        when (mode) {
            ComposeMode.Reply -> messageId?.let { applyMeta(messagesClient.fetchReplyMeta(session, it), "Re: ", selfStaffId) }
            ComposeMode.ReplyAll -> messageId?.let { applyMeta(messagesClient.fetchReplyAllMeta(session, it), "Re: ", selfStaffId) }
            ComposeMode.Forward -> messageId?.let { applyMeta(messagesClient.fetchForwardMeta(session, it), "Fwd: ", selfStaffId) }
            ComposeMode.New -> Unit
        }
        _uiState.update { it.copy(isLoadingRecipients = false) }
    }

    private fun applyMeta(meta: JsonObject, subjectPrefix: String, selfStaffId: Int?) {
        meta["subject"]?.jsonPrimitive?.content?.let { subject ->
            val prefixed = if (subject.startsWith(subjectPrefix)) subject else "$subjectPrefix$subject"
            _uiState.update { it.copy(subject = prefixed) }
        }
        meta["contents"]?.jsonPrimitive?.content?.let { contents ->
            if (_uiState.value.bodyHtml.isBlank()) {
                _uiState.update { it.copy(bodyHtml = contents) }
            }
        }
        val participants = meta["participants"]?.jsonArray?.mapNotNull { element ->
            parseRecipientFromMeta(element.jsonObject)
        } ?: emptyList()
        val filtered = if (selfStaffId != null) {
            participants.filterNot { it.type == RecipientType.Staff && it.id == selfStaffId }
        } else {
            participants
        }
        if (filtered.isNotEmpty()) {
            _uiState.update { it.copy(selectedRecipients = filtered) }
        }
    }

    private fun parseRecipientFromMeta(dict: JsonObject): Recipient? {
        val id = runCatching { dict["id"]?.jsonPrimitive?.int }.getOrNull() ?: return null
        val name = dict["name"]?.jsonPrimitive?.content
            ?: dict["xx_display"]?.jsonPrimitive?.content
            ?: "Unknown"
        val type = when {
            dict["student"]?.jsonPrimitive?.content == "true" -> RecipientType.Student
            dict["tutor"]?.jsonPrimitive?.content == "true" -> RecipientType.Tutor
            dict["staff"]?.jsonPrimitive?.content == "true" -> RecipientType.Staff
            else -> RecipientType.entries.firstOrNull {
                it.apiKey == dict["type"]?.jsonPrimitive?.content
            } ?: RecipientType.Staff
        }
        return Recipient(id = id, displayName = name, type = type)
    }

    private fun dedupRecipients(recipients: List<Recipient>): List<Recipient> {
        val seen = mutableSetOf<String>()
        val result = mutableListOf<Recipient>()
        for (recipient in recipients) {
            val key = recipient.displayName.lowercase()
            if (seen.add(key)) result.add(recipient)
        }
        return result
    }
}
