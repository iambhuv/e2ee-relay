package com.promtuz.chat.data.remote.realtime

import com.promtuz.chat.data.remote.dto.Bytes

/**
 * esk: pointer to EphemeralSecret in libcore JNI
 */
data class EphemeralKeyPair(val esk: Long, val epk: Bytes)
