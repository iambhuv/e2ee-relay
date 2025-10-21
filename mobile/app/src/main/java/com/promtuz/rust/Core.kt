package com.promtuz.rust

class Core {
    companion object {
        init {
            System.loadLibrary("core")
        }
    }


    /**
     * returns `Pair(SecretKey, PublicKey)`
     */
    external fun getStaticKeypair(): Pair<ByteArray, ByteArray>

    external fun initLogger()
}