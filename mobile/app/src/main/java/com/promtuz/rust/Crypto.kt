package com.promtuz.rust

class Crypto {
    /**
     * returns the pointer to `EphemeralSecret Key` and `Ephemeral Public Key Bytes`
     */

    external fun getEphemeralKeypair(): Pair<Long, ByteArray>


    external fun ephemeralDiffieHellman(
        ephemeralSecretPtr: Long,
        publicKeyBytes: ByteArray
    ): ByteArray


    external fun diffieHellman(
        secretKeyBytes: ByteArray, publicKeyBytes: ByteArray
    ): ByteArray

    external fun deriveSharedKey(
        rawKey: ByteArray, salt: String, info: String
    ): ByteArray
}