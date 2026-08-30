package com.cairnlabworks.mywealth.data.security

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Provides the 256-bit passphrase used to encrypt the SQLCipher database.
 *
 * The passphrase is generated once with a CSPRNG and stored inside
 * [EncryptedSharedPreferences], which is itself protected by a key held in the
 * Android Keystore (hardware-backed where available). The raw passphrase never
 * touches plaintext storage and never leaves the device.
 */
class DatabaseKeyProvider(context: Context) {

    private val appContext = context.applicationContext

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            SECURE_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /**
     * Returns a fresh copy of the database passphrase, generating and persisting
     * one on first use. A copy is returned because SQLCipher zeroes the array it
     * receives.
     */
    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        val existing = prefs.getString(KEY_DB_PASSPHRASE, null)
        val bytes = if (existing != null) {
            Base64.decode(existing, Base64.NO_WRAP)
        } else {
            val generated = ByteArray(KEY_SIZE_BYTES).also { SecureRandom().nextBytes(it) }
            prefs.edit()
                .putString(KEY_DB_PASSPHRASE, Base64.encodeToString(generated, Base64.NO_WRAP))
                .apply()
            generated
        }
        return bytes.copyOf()
    }

    private companion object {
        const val SECURE_PREFS_NAME = "mywealth_secure_prefs"
        const val KEY_DB_PASSPHRASE = "db_passphrase"
        const val KEY_SIZE_BYTES = 32
    }
}
