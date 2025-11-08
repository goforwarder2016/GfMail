package com.gf.mail.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * 使用Android Keystore进行安全的密码加密存储
 */
class CredentialEncryption(private val context: Context) {
    
    companion object {
        private const val KEY_ALIAS = "GfmailCredentialKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
        private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
        private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }
    
    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }
    
    init {
        generateKeyIfNeeded()
    }
    
    /**
     * 生成密钥（如果does not exist）
     */
    private fun generateKeyIfNeeded() {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(ENCRYPTION_BLOCK_MODE)
                .setEncryptionPaddings(ENCRYPTION_PADDING)
                .setUserAuthenticationRequired(false) // 不需要用户认证
                .setRandomizedEncryptionRequired(true)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    /**
     * 加密密码
     */
    fun encryptPassword(password: String): String {
        try {
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance("$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)
            
            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(password.toByteArray())
            
            // 将IV和加密数据组合
            val combined = iv + encryptedBytes
            return Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            throw SecurityException("密码加密失败", e)
        }
    }
    
    /**
     * 解密密码
     */
    fun decryptPassword(encryptedPassword: String): String {
        try {
            val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
            val cipher = Cipher.getInstance("$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING")
            
            val combined = Base64.decode(encryptedPassword, Base64.DEFAULT)
            val iv = combined.sliceArray(0 until GCM_IV_LENGTH)
            val encryptedBytes = combined.sliceArray(GCM_IV_LENGTH until combined.size)
            
            val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)
            
            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes)
        } catch (e: Exception) {
            throw SecurityException("密码解密失败", e)
        }
    }
    
    /**
     * 安全地存储密码到SharedPreferences
     */
    fun storePasswordSecurely(accountId: String, password: String) {
        try {
            val encryptedPassword = encryptPassword(password)
            val prefs = context.getSharedPreferences("secure_credentials", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("password_$accountId", encryptedPassword)
                .apply()
        } catch (e: Exception) {
            throw SecurityException("密码存储失败", e)
        }
    }
    
    /**
     * 安全地从SharedPreferences获取密码
     */
    fun getPasswordSecurely(accountId: String): String? {
        return try {
            val prefs = context.getSharedPreferences("secure_credentials", Context.MODE_PRIVATE)
            val encryptedPassword = prefs.getString("password_$accountId", null)
            encryptedPassword?.let { decryptPassword(it) }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 删除存储的密码
     */
    fun removePasswordSecurely(accountId: String) {
        val prefs = context.getSharedPreferences("secure_credentials", Context.MODE_PRIVATE)
        prefs.edit()
            .remove("password_$accountId")
            .apply()
    }
    
    /**
     * 清除所有存储的密码
     */
    fun clearAllPasswords() {
        val prefs = context.getSharedPreferences("secure_credentials", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
    
    /**
     * 检查密钥是否可用
     */
    fun isKeyAvailable(): Boolean {
        return try {
            keyStore.containsAlias(KEY_ALIAS)
        } catch (e: Exception) {
            false
        }
    }
}