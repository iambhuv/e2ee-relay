package com.promtuz.chat.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.core.content.edit
import kotlinx.io.IOException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlin.io.encoding.Base64

class KeyManager(context: Context) {
    companion object {
        private const val WRAPPER_KEY_ALIAS = "encryptor"
        private const val PREFS_NAME = "keys"
        private const val IDENTITY_SECRET = "private_identity"
        private const val IDENTITY_SECRET_IV = "private_identity_iv"
        private const val IDENTITY_PUBLIC = "public_identity"
    }

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)


    fun initialize() {
        // keyStore.set
        if (!keyStore.containsAlias(WRAPPER_KEY_ALIAS)) {
            generateWrapperKey()
        }

        // UNSAFE_clearKeys()
    }

    fun UNSAFE_clearKeys() {
        prefs.edit {
            remove(WRAPPER_KEY_ALIAS)
            remove(PREFS_NAME)
            remove(IDENTITY_SECRET)
            remove(IDENTITY_SECRET_IV)
            remove(IDENTITY_PUBLIC)
        }
    }

    private fun generateWrapperKey() {
        val keyGenerator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val spec = KeyGenParameterSpec.Builder(
            WRAPPER_KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
            setRandomizedEncryptionRequired(true)
        }.build()
        keyGenerator.init(spec)
        keyGenerator.generateKey()
    }

    /**
     * Encrypts the original main secret key with keystore's key to safely store it
     * returning the cipher and iv in pair
     */
    private fun encryptWithKeystoreKey(data: ByteArray): Pair<ByteArray, ByteArray> {
        val secretKey = keyStore.getKey(WRAPPER_KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return Pair(cipher.doFinal(data), cipher.iv)
    }

    private fun decryptWithKeystoreKey(encryptedData: ByteArray, iv: ByteArray): ByteArray {
        val secretKey = keyStore.getKey(WRAPPER_KEY_ALIAS, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
        return cipher.doFinal(encryptedData)
    }


    fun storePublicKey(publicKey: ByteArray) {
        prefs.edit {
            putString(IDENTITY_PUBLIC, Base64.encode(publicKey))
        }
    }

    fun storeSecretKey(secretKey: ByteArray) {
        try {
            val (encryptedKey, iv) = encryptWithKeystoreKey(secretKey)

            prefs.edit {
                putString(IDENTITY_SECRET, Base64.encode(encryptedKey))
                putString(IDENTITY_SECRET_IV, Base64.encode(iv))
            }
        } finally {
            secretKey.fill(0)
        }
    }

    fun hasSecretKey(): Boolean {
        return null != prefs.getString(IDENTITY_SECRET, null)
                && null != prefs.getString(IDENTITY_SECRET_IV, null)
    }

    /** UNSAFE: It's returning decrypted bytearray of such sensitive secret key?
     *
     * TODO: Create a wrapper class `SecretKey`,
     *  it'll hold all necessary methods, even involving libcore,
     *  decrypting the key on-demand and clearing after use
     *
     */
    fun getSecretKey(): ByteArray? {
        val encryptedKeyStr = prefs.getString(IDENTITY_SECRET, null) ?: return null
        val ivStr = prefs.getString(IDENTITY_SECRET_IV, null) ?: return null
        val cipher = Base64.decode(encryptedKeyStr)
        val iv = Base64.decode(ivStr)
        return decryptWithKeystoreKey(cipher, iv)
    }


    @Throws(IOException::class)
    fun getPublicKey(): ByteArray {
        val pubKeyStr = prefs.getString(IDENTITY_PUBLIC, null) ?: throw IOException("Public Key not found in KeyManager")
        return Base64.decode(pubKeyStr)
    }
}