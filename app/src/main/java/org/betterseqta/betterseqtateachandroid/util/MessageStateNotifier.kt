package org.betterseqta.betterseqtateachandroid.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.betterseqta.betterseqtateachandroid.domain.model.MessageStateChange
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageStateNotifier @Inject constructor() {
    private val _events = MutableSharedFlow<MessageStateChange>(extraBufferCapacity = 16)
    val events: SharedFlow<MessageStateChange> = _events.asSharedFlow()

    fun post(change: MessageStateChange) {
        _events.tryEmit(change)
    }
}
