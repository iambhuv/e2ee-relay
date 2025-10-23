package com.promtuz.chat.data.remote.events

import com.promtuz.chat.data.remote.dto.Bytes
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

object ConnectionEvents {
    @Serializable
    @SerialName("CONNECT")
    data class Connect(
        val proof: Bytes
    ) : ClientEvents()


    @Serializable
    @SerialName("ACCEPT")
    data object Accept : ServerEvents()
}
