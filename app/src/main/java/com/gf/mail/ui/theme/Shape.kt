package com.gf.mail.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    // Small components (buttons, cards, text fields)
    small = RoundedCornerShape(4.dp),
    // Medium components (floating action buttons, containers)
    medium = RoundedCornerShape(12.dp),
    // Large components (modal surfaces, sheets)
    large = RoundedCornerShape(16.dp)
)

// Email-specific shapes
object EmailShapes {
    val emailCard = RoundedCornerShape(8.dp)
    val attachmentChip = RoundedCornerShape(16.dp)
    val composeBottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
}
