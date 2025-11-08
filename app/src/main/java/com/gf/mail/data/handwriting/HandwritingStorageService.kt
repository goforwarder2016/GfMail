package com.gf.mail.data.handwriting

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.gf.mail.data.security.CredentialEncryption
import com.gf.mail.domain.model.HandwritingData
import com.gf.mail.domain.model.HandwritingPath
import com.gf.mail.domain.model.HandwritingPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for storing and retrieving handwriting data with encryption
 */
@Singleton
class HandwritingStorageService @Inject constructor(
    private val context: Context,
    private val credentialEncryption: CredentialEncryption
) {
    
    private val handwritingDir = File(context.filesDir, "handwriting")
    
    init {
        if (!handwritingDir.exists()) {
            handwritingDir.mkdirs()
        }
    }
    
    /**
     * Save handwriting data to encrypted file
     */
    suspend fun saveHandwritingData(
        handwritingData: HandwritingData,
        fileName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val actualFileName = fileName ?: "handwriting_${System.currentTimeMillis()}.dat"
            val file = File(handwritingDir, actualFileName)
            
            // Serialize handwriting data
            val serializedData = serializeHandwritingData(handwritingData)
            
            // Encrypt the data (convert ByteArray to Base64 string for encryption)
            val serializedString = android.util.Base64.encodeToString(serializedData, android.util.Base64.DEFAULT)
            val encryptedString = credentialEncryption.encryptPassword(serializedString)
            val encryptedData = encryptedString.toByteArray()
            
            // Write to file
            file.writeBytes(encryptedData)
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Load handwriting data from encrypted file
     */
    suspend fun loadHandwritingData(filePath: String): Result<HandwritingData> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(FileNotFoundException("File not found: $filePath"))
            }
            
            // Read encrypted data
            val encryptedData = file.readBytes()
            
            // Decrypt the data
            val encryptedString = String(encryptedData)
            val decryptedString = credentialEncryption.decryptPassword(encryptedString)
            
            // Convert back to ByteArray and deserialize handwriting data
            val decryptedData = android.util.Base64.decode(decryptedString, android.util.Base64.DEFAULT)
            val handwritingData = deserializeHandwritingData(decryptedData)
            
            Result.success(handwritingData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Save handwriting as bitmap image
     */
    suspend fun saveHandwritingAsBitmap(
        handwritingData: HandwritingData,
        fileName: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val actualFileName = fileName ?: "handwriting_${System.currentTimeMillis()}.png"
            val file = File(handwritingDir, actualFileName)
            
            // Create bitmap from handwriting data
            val bitmap = createBitmapFromHandwriting(handwritingData)
            
            // Save bitmap to file
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.close()
            
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Load handwriting from bitmap image
     */
    suspend fun loadHandwritingFromBitmap(filePath: String): Result<HandwritingData> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (!file.exists()) {
                return@withContext Result.failure(FileNotFoundException("File not found: $filePath"))
            }
            
            // Load bitmap from file
            val bitmap = BitmapFactory.decodeFile(filePath)
            if (bitmap == null) {
                return@withContext Result.failure(Exception("Failed to decode bitmap"))
            }
            
            // Convert bitmap to handwriting data (simplified - would need more complex logic for real conversion)
            val handwritingData = HandwritingData(
                id = "bitmap_${System.currentTimeMillis()}",
                width = bitmap.width,
                height = bitmap.height,
                strokeColor = android.graphics.Color.BLACK.toLong(),
                strokeWidth = 3f,
                paths = emptyList(), // Would need to extract paths from bitmap
                points = emptyList() // Would need to extract points from bitmap
            )
            
            Result.success(handwritingData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete handwriting file
     */
    suspend fun deleteHandwritingFile(filePath: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get all handwriting files
     */
    suspend fun getAllHandwritingFiles(): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val files = handwritingDir.listFiles()?.map { it.absolutePath } ?: emptyList()
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get handwriting file size
     */
    suspend fun getHandwritingFileSize(filePath: String): Result<Long> = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                Result.success(file.length())
            } else {
                Result.failure(FileNotFoundException("File not found: $filePath"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clean up old handwriting files
     */
    suspend fun cleanupOldHandwritingFiles(maxAgeDays: Int = 30): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val cutoffTime = System.currentTimeMillis() - (maxAgeDays * 24 * 60 * 60 * 1000L)
            var deletedCount = 0
            
            handwritingDir.listFiles()?.forEach { file ->
                if (file.lastModified() < cutoffTime) {
                    if (file.delete()) {
                        deletedCount++
                    }
                }
            }
            
            Result.success(deletedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Serialize handwriting data to byte array
     */
    private fun serializeHandwritingData(handwritingData: HandwritingData): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val objectOutputStream = ObjectOutputStream(outputStream)
        
        // Create serializable version of handwriting data
        val serializableData = SerializableHandwritingData(
            width = handwritingData.width,
            height = handwritingData.height,
            strokeColor = handwritingData.strokeColor.toInt(),
            strokeWidth = handwritingData.strokeWidth,
            paths = handwritingData.paths.map { path ->
                SerializableHandwritingPath(
                    points = path.points.map { point ->
                        SerializableHandwritingPoint(
                            x = point.x,
                            y = point.y
                        )
                    }
                )
            }
        )
        
        objectOutputStream.writeObject(serializableData)
        objectOutputStream.close()
        
        return outputStream.toByteArray()
    }
    
    /**
     * Deserialize handwriting data from byte array
     */
    private fun deserializeHandwritingData(data: ByteArray): HandwritingData {
        val inputStream = ByteArrayInputStream(data)
        val objectInputStream = ObjectInputStream(inputStream)
        
        val serializableData = objectInputStream.readObject() as SerializableHandwritingData
        objectInputStream.close()
        
        return HandwritingData(
            id = "deserialized_${System.currentTimeMillis()}",
            width = serializableData.width,
            height = serializableData.height,
            strokeColor = serializableData.strokeColor.toLong(),
            strokeWidth = serializableData.strokeWidth,
            paths = serializableData.paths.map { path ->
                HandwritingPath(
                    points = path.points.map { point ->
                        HandwritingPoint(
                            x = point.x,
                            y = point.y
                        )
                    }
                )
            },
            points = emptyList() // All points are contained within paths
        )
    }
    
    /**
     * Create bitmap from handwriting data
     */
    private fun createBitmapFromHandwriting(handwritingData: HandwritingData): Bitmap {
        val bitmap = Bitmap.createBitmap(
            handwritingData.width,
            handwritingData.height,
            Bitmap.Config.ARGB_8888
        )
        
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        
        val paint = Paint().apply {
            color = handwritingData.strokeColor.toInt()
            strokeWidth = handwritingData.strokeWidth
            style = Paint.Style.STROKE
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
        
        handwritingData.paths.forEach { pathData ->
            val path = Path()
            pathData.points.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(point.x, point.y)
                } else {
                    path.lineTo(point.x, point.y)
                }
            }
            canvas.drawPath(path, paint)
        }
        
        return bitmap
    }
}

/**
 * Serializable version of HandwritingData
 */
private data class SerializableHandwritingData(
    val width: Int,
    val height: Int,
    val strokeColor: Int,
    val strokeWidth: Float,
    val paths: List<SerializableHandwritingPath>
) : Serializable

/**
 * Serializable version of HandwritingPath
 */
private data class SerializableHandwritingPath(
    val points: List<SerializableHandwritingPoint>
) : Serializable

/**
 * Serializable version of HandwritingPoint
 */
private data class SerializableHandwritingPoint(
    val x: Float,
    val y: Float
) : Serializable
