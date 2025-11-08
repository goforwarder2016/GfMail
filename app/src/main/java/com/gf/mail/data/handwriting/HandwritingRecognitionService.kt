package com.gf.mail.data.handwriting

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.gf.mail.domain.model.HandwritingData
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import dagger.hilt.android.qualifiers.ApplicationContext

/**
 * Service for recognizing handwritten text using ML Kit
 */
@Singleton
class HandwritingRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    /**
     * Recognize handwritten text from handwriting data
     */
    suspend fun recognizeHandwriting(handwritingData: HandwritingData): Result<HandwritingRecognitionResult> {
        return try {
            val bitmap = createBitmapFromHandwriting(handwritingData)
            val inputImage = InputImage.fromBitmap(bitmap, 0)
            
            val result = suspendCancellableCoroutine<HandwritingRecognitionResult> { continuation ->
                textRecognizer.process(inputImage)
                    .addOnSuccessListener { visionText ->
                        val recognizedText = visionText.text
                        val confidence = calculateConfidence(visionText)
                        val alternatives = extractAlternatives(visionText)
                        
                        continuation.resume(
                            HandwritingRecognitionResult(
                                text = recognizedText,
                                confidence = confidence,
                                alternatives = alternatives
                            )
                        )
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(
                            HandwritingRecognitionResult(
                                text = "",
                                confidence = 0f,
                                alternatives = emptyList()
                            )
                        )
                    }
            }
            
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
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
    
    /**
     * Calculate confidence score from vision text
     */
    private fun calculateConfidence(visionText: com.google.mlkit.vision.text.Text): Float {
        // Simple confidence calculation based on text length and block count
        val textLength = visionText.text.length
        val blockCount = visionText.textBlocks.size
        
        return when {
            textLength == 0 -> 0f
            blockCount == 0 -> 0f
            else -> (textLength.toFloat() / (textLength + blockCount)).coerceIn(0f, 1f)
        }
    }
    
    /**
     * Extract alternative text suggestions
     */
    private fun extractAlternatives(visionText: com.google.mlkit.vision.text.Text): List<String> {
        val alternatives = mutableListOf<String>()
        
        visionText.textBlocks.forEach { block ->
            block.lines.forEach { line ->
                alternatives.add(line.text)
            }
        }
        
        return alternatives.distinct().take(5) // Limit to 5 alternatives
    }
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        textRecognizer.close()
    }
}

/**
 * Result of handwriting recognition
 */
data class HandwritingRecognitionResult(
    val text: String,
    val confidence: Float,
    val alternatives: List<String> = emptyList()
)
