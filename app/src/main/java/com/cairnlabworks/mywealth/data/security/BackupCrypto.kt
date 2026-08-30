package com.cairnlabworks.mywealth.data.security

import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Password-based authenticated encryption for portfolio export/import files.
 *
 * File layout (all binary):
 *   magic[4] = "MWB1"
 *   version[1]
 *   salt[16]
 *   iv[12]
 *   ciphertext + GCM tag
 *
 * The key is derived from the user's password with PBKDF2-HMAC-SHA256 so an
 * exported file is useless without the password, keeping data secure at rest
 * even outside the device.
 */
object BackupCrypto {

    private val MAGIC = byteArrayOf('M'.code.toByte(), 'W'.code.toByte(), 'B'.code.toByte(), '1'.code.toByte())
    private const val VERSION: Byte = 1
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH = 12
    private const val TAG_LENGTH_BITS = 128
    private const val PBKDF2_ITERATIONS = 210_000
    private const val KEY_LENGTH_BITS = 256

    class InvalidBackupException(message: String, cause: Throwable? = null) : Exception(message, cause)

    fun encrypt(plaintext: ByteArray, password: CharArray): ByteArray {
        val salt = ByteArray(SALT_LENGTH).also { SecureRandom().nextBytes(it) }
        val iv = ByteArray(IV_LENGTH).also { SecureRandom().nextBytes(it) }
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        val ciphertext = cipher.doFinal(plaintext)

        return ByteArrayOutputStream().apply {
            write(MAGIC)
            write(byteArrayOf(VERSION))
            write(salt)
            write(iv)
            write(ciphertext)
        }.toByteArray()
    }

    fun decrypt(data: ByteArray, password: CharArray): ByteArray {
        val header = MAGIC.size + 1 + SALT_LENGTH + IV_LENGTH
        if (data.size < header) {
            throw InvalidBackupException("File is not a valid MyWealth backup.")
        }
        var offset = 0
        val magic = data.copyOfRange(offset, MAGIC.size); offset += MAGIC.size
        if (!magic.contentEquals(MAGIC)) {
            throw InvalidBackupException("File is not a valid MyWealth backup.")
        }
        offset += 1 // version (only v1 exists today)
        val salt = data.copyOfRange(offset, offset + SALT_LENGTH); offset += SALT_LENGTH
        val iv = data.copyOfRange(offset, offset + IV_LENGTH); offset += IV_LENGTH
        val ciphertext = data.copyOfRange(offset, data.size)

        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BITS, iv))
        return try {
            cipher.doFinal(ciphertext)
        } catch (e: Exception) {
            throw InvalidBackupException("Incorrect password or corrupted file.", e)
        }
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, PBKDF2_ITERATIONS, KEY_LENGTH_BITS)
        val keyBytes = factory.generateSecret(spec).encoded
        spec.clearPassword()
        return SecretKeySpec(keyBytes, "AES")
    }
}
