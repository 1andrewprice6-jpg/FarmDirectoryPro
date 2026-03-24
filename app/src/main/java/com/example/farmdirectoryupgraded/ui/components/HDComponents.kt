package com.example.farmdirectoryupgraded.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Elevated gradient card with smooth hover/press animation */
@Composable
fun HDGradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = listOf(
        MaterialTheme.colorScheme.surface,
        MaterialTheme.colorScheme.surfaceVariant
    ),
    elevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    val cardModifier = modifier
        .graphicsLayer { scaleX = scale; scaleY = scale }
        .shadow(elevation, RoundedCornerShape(16.dp))
        .clip(RoundedCornerShape(16.dp))
        .background(Brush.verticalGradient(gradientColors))

    if (onClick != null) {
        Column(
            modifier = cardModifier.clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
            ).padding(16.dp),
            content = content
        )
    } else {
        Column(modifier = cardModifier.padding(16.dp), content = content)
    }
}

/** Neon/glowing header with gradient text effect */
@Composable
fun HDSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                )
            )
        ),
        modifier = modifier.padding(vertical = 8.dp)
    )
}

/** Animated loading shimmer placeholder */
@Composable
fun HDShimmerCard(
    modifier: Modifier = Modifier,
    lineCount: Int = 3
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "shimmer_alpha"
    )
    Card(
        modifier = modifier.fillMaxWidth().padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            repeat(lineCount) { i ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(if (i == lineCount - 1) 0.6f else 1f)
                        .height(16.dp)
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = alpha))
                )
                if (i < lineCount - 1) Spacer(Modifier.height(8.dp))
            }
        }
    }
}

/** Animated FAB with ripple glow */
@Composable
fun HDFloatingActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "glow"
    )
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(50))
                .background(containerColor.copy(alpha = glowAlpha * 0.3f))
        )
        FloatingActionButton(
            onClick = onClick,
            containerColor = containerColor,
            modifier = Modifier.align(Alignment.Center)
        ) { icon() }
    }
}
