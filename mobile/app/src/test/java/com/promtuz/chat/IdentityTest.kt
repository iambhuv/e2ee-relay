package com.promtuz.chat

import com.promtuz.chat.domain.model.Identity
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class IdentityTest {
    private val key = ByteArray(32) { (it + 1).toByte() }
    private val identity = Identity(key, "TestUser")
    private val identityHex =
        "5A5450070102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F20000000005465737455736572"

    @Test
    fun testToByteArray() {
        assertContentEquals(
            identityHex.hexToByteArray(HexFormat.UpperCase),
            identity.toByteArray()
        )
    }

    @Test
    fun testFromByteArray() {
        assertEquals(
            identity,
            Identity.fromByteArray(identityHex.hexToByteArray(HexFormat.UpperCase))
        )
    }
}