package com.promtuz.rust


class Core {
    companion object {
        init {
            System.loadLibrary("core")
        }

        @Volatile
        private var INSTANCE: Core? = null

        fun getInstance(): Core {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Core().also { INSTANCE = it }
            }
        }
    }


    /**
     * returns `Pair(SecretKey, PublicKey)`
     */
    external fun getStaticKeypair(): Pair<ByteArray, ByteArray>
}