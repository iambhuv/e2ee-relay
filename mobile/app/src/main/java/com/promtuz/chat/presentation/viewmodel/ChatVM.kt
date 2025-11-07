package com.promtuz.chat.presentation.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import com.promtuz.chat.data.dummy.DummyMessage
import com.promtuz.chat.data.dummy.dummyMessages
import com.promtuz.chat.domain.model.UiMessage
import com.promtuz.chat.domain.model.UiMessagePosition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatVM(
    private val application: Application
) : ViewModel() {
    private val context: Context get() = application.applicationContext

    private val _messages = MutableStateFlow(emptyList<UiMessage>())
    val messages: StateFlow<List<UiMessage>> = _messages.asStateFlow()

    private fun List<DummyMessage>.toUi(): List<UiMessage> = mapIndexed { i, m ->
        val prev = getOrNull(i - 1)
        val next = getOrNull(i + 1)

        val samePrev = prev?.isSent == m.isSent
        val sameNext = next?.isSent == m.isSent

        val position = when {
            samePrev && sameNext -> UiMessagePosition.Middle
            samePrev && !sameNext -> UiMessagePosition.Start
            !samePrev && sameNext -> UiMessagePosition.End
            else -> UiMessagePosition.Single // This should never be reached
        }


        UiMessage(
            m.id, m.content, m.isSent, position, m.timestamp
        )
    }


    init {
        _messages.value += dummyMessages.toUi()
    }
}