package com.example.farmdirectoryupgraded.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

// Smooth fade + slide transitions for navigation
val farmEnterTransition: EnterTransition = fadeIn(animationSpec = tween(300)) +
    slideInHorizontally(animationSpec = tween(300)) { it / 4 }

val farmExitTransition: ExitTransition = fadeOut(animationSpec = tween(200)) +
    slideOutHorizontally(animationSpec = tween(200)) { -it / 4 }

val farmPopEnterTransition: EnterTransition = fadeIn(animationSpec = tween(300)) +
    slideInHorizontally(animationSpec = tween(300)) { -it / 4 }

val farmPopExitTransition: ExitTransition = fadeOut(animationSpec = tween(200)) +
    slideOutHorizontally(animationSpec = tween(200)) { it / 4 }

// Shimmer loading effect
@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    val shimmerTranslateAnim = rememberInfiniteTransition(label = "shimmer")
    val shimmerColor by shimmerTranslateAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    Box(modifier = modifier) { content() }
}

// Scale + fade reveal animation
@Composable
fun RevealAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(initialScale = 0.85f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )) + fadeIn(tween(250)),
        exit = scaleOut(targetScale = 0.85f, animationSpec = tween(200)) + fadeOut(tween(200)),
        modifier = modifier,
        content = content
    )
}

// Pulsing glow modifier for key actions
@Composable
fun pulsingScale(): Float {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    return infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    ).value
}

// Staggered list item animation
@Composable
fun StaggeredItemAnimation(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 },
        modifier = modifier
    ) { content() }
}
