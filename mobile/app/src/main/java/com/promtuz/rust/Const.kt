package com.promtuz.rust


object Salts {
    const val HANDSHAKE = "Handshake Salt"
    const val EVENT = "Event Salt"
}

object Info {
    const val SERVER_HANDSHAKE_SV_TO_CL = "Handshake: Server->Client"
    const val CLIENT_HANDSHAKE_CL_TO_SV = "Handshake: Client->Server"
    const val SERVER_EVENT_SV_TO_CL = "Event: Server->Client"
    const val CLIENT_EVENT_CL_TO_SV = "Event: Client->Server"
}