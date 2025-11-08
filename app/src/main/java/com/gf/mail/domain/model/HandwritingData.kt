package com.gf.mail.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class representing handwriting data
 */
@Parcelize
data class HandwritingData(
    val id: String,
    val width: Int,
    val height: Int,
    val strokeColor: Long,
    val strokeWidth: Float,
    val paths: List<HandwritingPath>,
    val points: List<HandwritingPoint>
) : Parcelable

/**
 * Data class representing a handwriting path
 */
@Parcelize
data class HandwritingPath(
    val points: List<HandwritingPoint>
) : Parcelable

/**
 * Data class representing a handwriting point
 */
@Parcelize
data class HandwritingPoint(
    val x: Float,
    val y: Float
) : Parcelable