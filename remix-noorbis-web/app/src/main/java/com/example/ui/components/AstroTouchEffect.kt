package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyanGlow

/**
 * Gives every clickable button/key in the game a continuous visual "clickable" affordance:
 * 1. Continuous gentle breathing scale (1.0f <-> 1.022f) and pulsing neon border glow
 * 2. Continuous diagonal starlight shimmer sweep across the button surface
 * 3. Luminous corner indicator dots showing interactivity
 * 4. Strong spring bounce + expanding shockwave ring when tapped/pressed
 */
fun Modifier.astroTouchEffect(
    glowColor: Color = CyberCyanGlow,
    pressScale: Float = 0.91f,
    cornerRadius: Dp = 14.dp
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    var lastTapOffset by remember { mutableStateOf(Offset.Unspecified) }
    val haptic = LocalHapticFeedback.current

    // Continuous idle animations so the player immediately sees the button is clickable
    val infiniteTransition = rememberInfiniteTransition(label = "astro_clickable_idle")

    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    val shimmerProgress by infiniteTransition.animateFloat(
        initialValue = -0.4f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_sweep"
    )

    val pressScaleAnim by animateFloatAsState(
        targetValue = if (isPressed) pressScale else (1f + idlePulse * 0.022f),
        animationSpec = if (isPressed) {
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        } else {
            tween(durationMillis = 180)
        },
        label = "astro_touch_scale"
    )

    val pressFlashAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 0f,
        animationSpec = tween(durationMillis = if (isPressed) 40 else 320),
        label = "astro_touch_flash"
    )

    this
        .graphicsLayer {
            scaleX = pressScaleAnim
            scaleY = pressScaleAnim
        }
        .drawWithContent {
            drawContent()

            val cr = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            val w = size.width
            val h = size.height

            // 1. Continuous diagonal shimmer sweep across the clickable button
            val sweepX = w * shimmerProgress
            val bandWidth = (w * 0.42f).coerceAtLeast(36.dp.toPx())
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        glowColor.copy(alpha = 0.16f + idlePulse * 0.10f),
                        Color.White.copy(alpha = 0.24f + idlePulse * 0.08f),
                        glowColor.copy(alpha = 0.16f + idlePulse * 0.10f),
                        Color.Transparent
                    ),
                    start = Offset(sweepX - bandWidth, 0f),
                    end = Offset(sweepX + bandWidth, h)
                ),
                cornerRadius = cr
            )

            // 2. Continuous pulsing neon border so the button visibly stands out as clickable
            val borderAlpha = (0.42f + idlePulse * 0.45f).coerceIn(0f, 1f)
            val borderWidth = (1.3f + idlePulse * 1.1f).dp.toPx()
            drawRoundRect(
                color = glowColor.copy(alpha = borderAlpha),
                cornerRadius = cr,
                style = Stroke(width = borderWidth)
            )

            // 3. Subtle luminous tactile indicator dots in top-right / bottom-left corners
            val dotInset = 6.dp.toPx()
            val dotRadius = (1.6f + idlePulse * 0.9f).dp.toPx()
            if (w > 44.dp.toPx() && h > 26.dp.toPx()) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f + idlePulse * 0.40f),
                    radius = dotRadius,
                    center = Offset(w - dotInset, dotInset)
                )
                drawCircle(
                    color = glowColor.copy(alpha = 0.55f + idlePulse * 0.40f),
                    radius = dotRadius,
                    center = Offset(dotInset, h - dotInset)
                )
            }

            // 4. Active Press Flash & Expanding Shockwave Ring when clicked
            if (pressFlashAlpha > 0.01f) {
                val centerPt = if (lastTapOffset != Offset.Unspecified) {
                    Offset(
                        lastTapOffset.x.coerceIn(0f, w),
                        lastTapOffset.y.coerceIn(0f, h)
                    )
                } else {
                    Offset(w / 2f, h / 2f)
                }

                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = pressFlashAlpha * 0.55f),
                            glowColor.copy(alpha = pressFlashAlpha * 0.38f),
                            Color.Transparent
                        ),
                        center = centerPt,
                        radius = maxOf(w, h) * 0.85f
                    ),
                    cornerRadius = cr
                )

                // Expanding shockwave ring
                val waveProgress = 1f - pressFlashAlpha
                val waveRadius = (minOf(w, h) * 0.25f) + (maxOf(w, h) * 0.65f * waveProgress)
                drawCircle(
                    color = Color.White.copy(alpha = pressFlashAlpha * 0.75f),
                    radius = waveRadius.coerceAtMost(maxOf(w, h) * 0.55f),
                    center = centerPt,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                drawRoundRect(
                    color = Color.White.copy(alpha = pressFlashAlpha),
                    cornerRadius = cr,
                    style = Stroke(width = 2.8.dp.toPx())
                )
            }
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
                lastTapOffset = down.position
                isPressed = true
                runCatching {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                do {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                } while (event.changes.any { it.pressed })
                isPressed = false
            }
        }
}
