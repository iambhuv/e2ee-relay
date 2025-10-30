package com.promtuz.chat.domain.model

data class Chat(
    val nickname: String = "Anonymous",
    val lastMessage: LastMessage,
    val type: ChatType = ChatType.Direct
)


enum class ChatType {
    Direct,
    Group
}