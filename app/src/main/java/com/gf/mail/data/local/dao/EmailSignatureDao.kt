package com.gf.mail.data.local.dao

import androidx.room.*
import com.gf.mail.data.local.entity.EmailSignatureEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for email signature operations
 */
@Dao
interface EmailSignatureDao {

    @Query("SELECT * FROM email_signatures ORDER BY name ASC")
    suspend fun getAllSignatures(): List<EmailSignatureEntity>

    @Query("SELECT * FROM email_signatures ORDER BY name ASC")
    fun getAllSignaturesFlow(): Flow<List<EmailSignatureEntity>>

    @Query("SELECT * FROM email_signatures WHERE id = :id LIMIT 1")
    suspend fun getSignatureById(id: String): EmailSignatureEntity?

    @Query(
        "SELECT * FROM email_signatures WHERE accountId = :accountId OR accountId IS NULL ORDER BY name ASC"
    )
    suspend fun getSignaturesForAccount(accountId: String): List<EmailSignatureEntity>

    @Query(
        "SELECT * FROM email_signatures WHERE accountId = :accountId OR accountId IS NULL ORDER BY name ASC"
    )
    fun getSignaturesForAccountFlow(accountId: String): Flow<List<EmailSignatureEntity>>

    @Query(
        "SELECT * FROM email_signatures WHERE isDefault = 1 AND (accountId = :accountId OR accountId IS NULL) LIMIT 1"
    )
    suspend fun getDefaultSignature(accountId: String): EmailSignatureEntity?

    @Query("SELECT * FROM email_signatures WHERE isDefault = 1 AND accountId IS NULL LIMIT 1")
    suspend fun getGlobalDefaultSignature(): EmailSignatureEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSignature(signature: EmailSignatureEntity): Long

    @Update
    suspend fun updateSignature(signature: EmailSignatureEntity)

    @Delete
    suspend fun deleteSignature(signature: EmailSignatureEntity)

    @Query("DELETE FROM email_signatures WHERE id = :id")
    suspend fun deleteSignatureById(id: String)

    @Query("DELETE FROM email_signatures WHERE accountId = :accountId")
    suspend fun deleteSignaturesForAccount(accountId: String)

    // Reset all signatures as non-default, then set specific one as default
    @Transaction
    suspend fun setDefaultSignature(signatureId: String, accountId: String?) {
        // Reset all signatures for this account (or global if accountId is null)
        if (accountId != null) {
            resetDefaultSignaturesForAccount(accountId)
        } else {
            resetGlobalDefaultSignatures()
        }

        // Set the specified signature as default
        setSignatureAsDefault(signatureId)
    }

    @Query("UPDATE email_signatures SET isDefault = 0 WHERE accountId = :accountId")
    suspend fun resetDefaultSignaturesForAccount(accountId: String)

    @Query("UPDATE email_signatures SET isDefault = 0 WHERE accountId IS NULL")
    suspend fun resetGlobalDefaultSignatures()

    @Query("UPDATE email_signatures SET isDefault = 1 WHERE id = :signatureId")
    suspend fun setSignatureAsDefault(signatureId: String)

    @Query("SELECT COUNT(*) FROM email_signatures")
    suspend fun getSignatureCount(): Int

    @Query(
        "SELECT COUNT(*) FROM email_signatures WHERE accountId = :accountId OR accountId IS NULL"
    )
    suspend fun getSignatureCountForAccount(accountId: String): Int

    // Signature Templates
    @Query("SELECT * FROM email_signatures WHERE accountId IS NULL ORDER BY name ASC")
    suspend fun getSignatureTemplates(): List<EmailSignatureEntity>

    @Query("SELECT * FROM email_signatures WHERE accountId IS NULL ORDER BY name ASC")
    fun getSignatureTemplatesFlow(): Flow<List<EmailSignatureEntity>>
}