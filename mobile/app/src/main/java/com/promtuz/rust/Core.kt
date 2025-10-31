package com.promtuz.rust

/**
 * TODO:
 *  - Rename `com.promtuz.rust` to `com.promtuz.core`
 *  - Move [getStaticKeypair] to [Crypto]
 */
class Core {
    companion object {
        init {
            System.loadLibrary("core")
        }
    }

    init {
        initLogger()
    }

    /**
     * returns `Pair(SecretKey, PublicKey)`
     */
    external fun getStaticKeypair(): Pair<ByteArray, ByteArray>

    external fun initLogger()
}