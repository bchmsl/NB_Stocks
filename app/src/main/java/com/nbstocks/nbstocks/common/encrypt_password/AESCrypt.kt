package com.nbstocks.nbstocks.common.encrypt_password

import android.annotation.SuppressLint
import android.util.Base64
import java.security.Key
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object AESCrypt {
    private const val ALGORITHM = "AES"
    private const val KEY = "1Hbfh667adfDEJ78"

    @SuppressLint("GetInstance")
    @Throws(Exception::class)
    fun encrypt(value: String): String {
        val key: Key = generateKey()
        val cipher: Cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val encryptedByteValue: ByteArray =
            cipher.doFinal(value.toByteArray(charset("utf-8")))
        return Base64.encodeToString(encryptedByteValue, Base64.DEFAULT)
    }

    @Throws(Exception::class)
    private fun generateKey(): Key {
        return SecretKeySpec(KEY.toByteArray(), ALGORITHM)
    }
}