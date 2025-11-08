package com.gf.mail.presentation.ui.components.handwriting

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.gf.mail.domain.model.HandwritingData
import com.gf.mail.domain.model.HandwritingPath
import com.gf.mail.domain.model.HandwritingPoint
import kotlin.math.abs

/**
 * Handwriting input view for drawing signatures and handwritten text
 */
@Composable
fun HandwritingView(
    modifier: Modifier = Modifier,
    strokeColor: Color = Color.Black,
    strokeWidth: Float = 5f,
    backgroundColor: Color = Color.White,
    onHandwritingChanged: (HandwritingData) -> Unit = {},
    showControls: Boolean = true,
    initialData: HandwritingData? = null
) {
    var handwritingData by remember { 
        mutableStateOf(
            initialData ?: HandwritingData(
                id = "",
                width = 0,
                height = 0,
                strokeColor = strokeColor.toArgb().toLong(),
                strokeWidth = strokeWidth,
                paths = emptyList(),
                points = emptyList()
            )
        )
    }
    
    var currentPath by remember { mutableStateOf<List<HandwritingPoint>>(emptyList()) }
    var isDrawing by remember { mutableStateOf(false) }
    
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDrawing = true
                            currentPath = listOf(
                                HandwritingPoint(offset.x, offset.y)
                            )
                        },
                        onDrag = { _, dragAmount ->
                            if (isDrawing) {
                                val lastPoint = currentPath.lastOrNull()
                                val newPoint = HandwritingPoint(
                                    x = lastPoint?.x?.plus(dragAmount.x) ?: dragAmount.x,
                                    y = lastPoint?.y?.plus(dragAmount.y) ?: dragAmount.y
                                )
                                
                                // Only add point if it's far enough from the last point
                                if (lastPoint == null || 
                                    abs(newPoint.x - lastPoint.x) > 2f || 
                                    abs(newPoint.y - lastPoint.y) > 2f) {
                                    currentPath = currentPath + newPoint
                                }
                            }
                        },
                        onDragEnd = {
                            if (isDrawing && currentPath.isNotEmpty()) {
                                val newPath = HandwritingPath(currentPath)
                                val updatedPaths = handwritingData.paths + newPath
                                
                                handwritingData = handwritingData.copy(
                                    width = size.width.toInt(),
                                    height = size.height.toInt(),
                                    paths = updatedPaths
                                )
                                
                                onHandwritingChanged(handwritingData)
                                currentPath = emptyList()
                                isDrawing = false
                            }
                        }
                    )
                }
        ) {
            // Draw background
            drawRect(backgroundColor)
            
            // Draw existing paths
            handwritingData.paths.forEach { path ->
                drawHandwritingPath(path, strokeColor, strokeWidth)
            }
            
            // Draw current path
            if (currentPath.isNotEmpty()) {
                drawHandwritingPath(
                    HandwritingPath(currentPath), 
                    strokeColor, 
                    strokeWidth
                )
            }
        }
        
        // Control buttons
        if (showControls) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Undo button
                if (handwritingData.paths.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            val updatedPaths = handwritingData.paths.dropLast(1)
                            handwritingData = handwritingData.copy(paths = updatedPaths)
                            onHandwritingChanged(handwritingData)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                // Clear button
                if (handwritingData.paths.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            handwritingData = handwritingData.copy(paths = emptyList())
                            onHandwritingChanged(handwritingData)
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Draw a handwriting path on the canvas
 */
private fun DrawScope.drawHandwritingPath(
    path: HandwritingPath,
    strokeColor: Color,
    strokeWidth: Float
) {
    if (path.points.isEmpty()) return
    
    val nativePath = Path()
    path.points.forEachIndexed { index, point ->
        if (index == 0) {
            nativePath.moveTo(point.x, point.y)
        } else {
            nativePath.lineTo(point.x, point.y)
        }
    }
    
    drawPath(
        path = nativePath,
        color = strokeColor,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

/**
 * Handwriting recognition result
 */
data class HandwritingRecognitionResult(
    val text: String,
    val confidence: Float,
    val alternatives: List<String> = emptyList()
)

/**
 * Handwriting recognition service
 */
class HandwritingRecognitionService {
    // TODO: Implement ML Kit integration
    suspend fun recognizeHandwriting(handwritingData: HandwritingData): Result<HandwritingRecognitionResult> {
        // Placeholder implementation
        return Result.success(
            HandwritingRecognitionResult(
                text = "Recognized text",
                confidence = 0.8f,
                alternatives = listOf("Alternative 1", "Alternative 2")
            )
        )
    }
}
