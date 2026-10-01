package org.betterseqta.betterseqtateachandroid.domain.model

sealed interface MessageStateChange {
    data class Read(val messageId: Int, val isRead: Boolean) : MessageStateChange
    data class Starred(val messageId: Int, val isStarred: Boolean) : MessageStateChange
    data class Trashed(val messageId: Int) : MessageStateChange
    data class Moved(val messageId: Int) : MessageStateChange
}
