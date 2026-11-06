package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CollectedTextEntity
import com.example.domain.model.FloatingTextRelic
import com.example.domain.model.MeditationsMatrix
import com.example.domain.model.NeuronCorruptionType
import com.example.domain.model.ObstacleType
import com.example.domain.model.PowerUpType
import com.example.domain.model.RescueTargetType
import com.example.domain.model.SpacePowerUp
import com.example.domain.model.SphereColorOption
import com.example.domain.model.VIEWPORT_HEIGHT
import com.example.domain.model.VIEWPORT_WIDTH
import com.example.domain.model.VirtueRemedyType
import com.example.domain.model.WORLD_HEIGHT
import com.example.domain.model.WORLD_WIDTH
import com.example.ui.theme.CoralHyperGlow
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldNova
import com.example.ui.theme.EmeraldNovaGlow
import com.example.ui.theme.NebulaGray
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextOverflow
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.PlasmaRose
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SolarGoldGlow
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.VioletPulsarGlow
import com.example.ui.viewmodel.ActiveGameState
import com.example.ui.viewmodel.GameStatus
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

private data class WorldStarPoint(
    val wx: Float, // 0f..WORLD_WIDTH
    val wy: Float, // 0f..WORLD_HEIGHT
    val radius: Float,
    val alpha: Float,
    val color: Color
)

@Composable
fun SpaceGameScreen(
    gameState: ActiveGameState,
    sphereColor: SphereColorOption,
    collectedInventory: List<CollectedTextEntity>,
    onTouchTarget: (Float?, Float?) -> Unit,
    onNudgePlayer: (Float, Float) -> Unit,
    onTogglePause: () -> Unit,
    onToggleQuickInventory: () -> Unit,
    onStartOrNext: () -> Unit,
    onRetryLevel: () -> Unit,
    onBackToHangar: () -> Unit,
    onOpenFullInventory: () -> Unit,
    onSetDirectionalInput: (Float, Float) -> Unit = { _, _ -> },
    isRoundJoystickEnabled: Boolean = true,
    isGridEnabled: Boolean = false,
    isMiniMapEnabled: Boolean = true,
    resumeCountdown: Int? = null,
    directCollectedText: FloatingTextRelic? = null,
    onDismissDirectCollectedText: () -> Unit = {},
    onDismissEndMissionTargetsPopup: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onAnchorThought: (String) -> Unit = {},
    onShareTextReward: () -> Unit = {},
    onSelectVirtue: (VirtueRemedyType) -> Unit = {},
    onTapPlayerSphere: () -> Unit = {},
    onDismissSphereRemedyMenu: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        if (gameState.showSphereRemedyMenu) {
            onDismissSphereRemedyMenu()
        } else if (gameState.status == GameStatus.QUICK_INVENTORY) {
            onToggleQuickInventory()
        } else {
            onBackToHangar()
        }
    }

    val textMeasurer = rememberTextMeasurer()
    var showGlossaryDialog by remember { mutableStateOf(false) }
    val currentGameState by rememberUpdatedState(gameState)
    val currentOnTapPlayerSphere by rememberUpdatedState(onTapPlayerSphere)

    // Twinkling stars distributed across the expanded space universe
    val worldStars = remember {
        List(260) { i ->
            val maxWx = (WORLD_WIDTH * 100).toInt().coerceAtLeast(100)
            val maxWy = (WORLD_HEIGHT * 100).toInt().coerceAtLeast(100)
            val wx = ((i * 137 + 19) % maxWx) / 100f
            val wy = ((i * 89 + 43) % maxWy) / 100f
            val starColor = when {
                i % 9 == 0 -> CyberCyanGlow
                i % 13 == 0 -> SolarGoldGlow
                else -> Color.White
            }
            WorldStarPoint(
                wx = wx,
                wy = wy,
                radius = if (i % 5 == 0) 2.5f else 1.4f,
                alpha = 0.35f + (i % 6) * 0.1f,
                color = starColor
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBackground)
    ) {
        // 1. Top Tactical Space HUD
        SpaceHudBar(
            gameState = gameState,
            sphereColor = sphereColor,
            inventoryCount = collectedInventory.size,
            onTogglePause = onTogglePause,
            onToggleQuickInventory = onToggleQuickInventory,
            onOpenSettings = onOpenSettings,
            onSelectVirtue = onSelectVirtue,
            onTapPlayerSphere = onTapPlayerSphere,
            onOpenGlossary = { showGlossaryDialog = true }
        )

        // Active Anchored Thought Ribbon (only shown during active gameplay so modals have full vertical space)
        if (gameState.activeAnchoredThought != null && gameState.status == GameStatus.PLAYING) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                color = SpaceSurface.copy(alpha = 0.90f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SolarGoldGlow.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🕊️", fontSize = 14.sp)
                    Text(
                        text = "« ${gameState.activeAnchoredThought} »",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = SolarGoldGlow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (gameState.peaceAuraTimer > 0f) {
                        Surface(
                            color = SolarGoldGlow.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "PAIX ${gameState.peaceAuraTimer.toInt()}s",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SolarGoldGlow,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive 4x Space Field Canvas + Overlays
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("space_game_canvas")
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = { offset ->
                                val st = currentGameState
                                val playerSx = (st.playerX - st.cameraX) * size.width
                                val playerSy = (st.playerY - st.cameraY) * size.height
                                val distToSphere = hypot(offset.x - playerSx, offset.y - playerSy)
                                if (distToSphere <= 58.dp.toPx() && st.status == GameStatus.PLAYING) {
                                    currentOnTapPlayerSphere()
                                } else {
                                    val nx = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                                    val ny = (offset.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                                    onTouchTarget(nx, ny)
                                    tryAwaitRelease()
                                    onTouchTarget(null, null)
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val nx = (offset.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                                val ny = (offset.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                                onTouchTarget(nx, ny)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val nx = (change.position.x / size.width.coerceAtLeast(1)).coerceIn(0f, 1f)
                                val ny = (change.position.y / size.height.coerceAtLeast(1)).coerceIn(0f, 1f)
                                onTouchTarget(nx, ny)
                            },
                            onDragEnd = { onTouchTarget(null, null) },
                            onDragCancel = { onTouchTarget(null, null) }
                        )
                    }
            ) {
                val w = size.width
                val h = size.height
                val minDim = size.minDimension
                val camX = gameState.cameraX
                val camY = gameState.cameraY

                // Background Nebula Gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF060913),
                            Color(0xFF0D152C),
                            Color(0xFF171130)
                        )
                    )
                )

                // Scrolling World Coordinate Grid (optional tactical grid, controlled by Quadrillage setting)
                if (isGridEnabled) {
                    val gridStep = 0.25f
                    var gx = 0f
                    while (gx <= WORLD_WIDTH + 0.01f) {
                        val sx = (gx - camX) * w
                        if (sx in -2f..(w + 2f)) {
                            val isCenterAxis = kotlin.math.abs(gx - 1.0f) < 0.01f
                            drawLine(
                                color = if (isCenterAxis) CyberCyan.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                                start = Offset(sx, 0f),
                                end = Offset(sx, h),
                                strokeWidth = if (isCenterAxis) 2.dp.toPx() else 1.dp.toPx()
                            )
                        }
                        gx += gridStep
                    }
                    var gy = 0f
                    while (gy <= WORLD_HEIGHT + 0.01f) {
                        val sy = (gy - camY) * h
                        if (sy in -2f..(h + 2f)) {
                            val isCenterAxis = kotlin.math.abs(gy - 1.0f) < 0.01f
                            drawLine(
                                color = if (isCenterAxis) CyberCyan.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.06f),
                                start = Offset(0f, sy),
                                end = Offset(w, sy),
                                strokeWidth = if (isCenterAxis) 2.dp.toPx() else 1.dp.toPx()
                            )
                        }
                        gy += gridStep
                    }
                }

                // Scrolling World Stars
                for (star in worldStars) {
                    val sx = (star.wx - camX) * w
                    val sy = (star.wy - camY) * h
                    if (sx in -10f..(w + 10f) && sy in -10f..(h + 10f)) {
                        drawCircle(
                            color = star.color.copy(alpha = star.alpha),
                            radius = star.radius.dp.toPx(),
                            center = Offset(sx, sy)
                        )
                    }
                }

                // Draw Outer Map Limits (Glowing Warp Borders at x=0, x=WORLD_WIDTH, y=0, y=WORLD_HEIGHT)
                val warpIntensity = (gameState.borderWrapTimer / 0.65f).coerceIn(0f, 1f)
                val dashPhaseSpeed = if (warpIntensity > 0f) 140f else 40f
                val dashEffect = PathEffect.dashPathEffect(
                    floatArrayOf(18f, 12f),
                    phase = gameState.elapsedTime * dashPhaseSpeed
                )
                val borderColor = lerp(
                    PlasmaRose.copy(alpha = 0.85f),
                    Color.White,
                    warpIntensity * 0.65f
                )
                val borderGlowColor = PlasmaRose.copy(alpha = 0.28f + warpIntensity * 0.52f)
                val borderStroke = (4f + warpIntensity * 5f).dp.toPx()
                val borderHaloStroke = (14f + warpIntensity * 22f).dp.toPx()

                val leftBorderX = (0f - camX) * w
                if (leftBorderX in -borderHaloStroke..(w + borderHaloStroke)) {
                    val bx = leftBorderX.coerceIn(2f, w - 2f)
                    drawLine(
                        color = borderGlowColor,
                        start = Offset(bx, 0f),
                        end = Offset(bx, h),
                        strokeWidth = borderHaloStroke
                    )
                    drawLine(
                        color = borderColor,
                        start = Offset(bx, 0f),
                        end = Offset(bx, h),
                        strokeWidth = borderStroke,
                        pathEffect = dashEffect
                    )
                }
                val rightBorderX = (WORLD_WIDTH - camX) * w
                if (rightBorderX in -borderHaloStroke..(w + borderHaloStroke)) {
                    val bx = rightBorderX.coerceIn(2f, w - 2f)
                    drawLine(
                        color = borderGlowColor,
                        start = Offset(bx, 0f),
                        end = Offset(bx, h),
                        strokeWidth = borderHaloStroke
                    )
                    drawLine(
                        color = borderColor,
                        start = Offset(bx, 0f),
                        end = Offset(bx, h),
                        strokeWidth = borderStroke,
                        pathEffect = dashEffect
                    )
                }
                val topBorderY = (0f - camY) * h
                if (topBorderY in -borderHaloStroke..(h + borderHaloStroke)) {
                    val by = topBorderY.coerceIn(2f, h - 2f)
                    drawLine(
                        color = borderGlowColor,
                        start = Offset(0f, by),
                        end = Offset(w, by),
                        strokeWidth = borderHaloStroke
                    )
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, by),
                        end = Offset(w, by),
                        strokeWidth = borderStroke,
                        pathEffect = dashEffect
                    )
                }
                val bottomBorderY = (WORLD_HEIGHT - camY) * h
                if (bottomBorderY in -borderHaloStroke..(h + borderHaloStroke)) {
                    val by = bottomBorderY.coerceIn(2f, h - 2f)
                    drawLine(
                        color = borderGlowColor,
                        start = Offset(0f, by),
                        end = Offset(w, by),
                        strokeWidth = borderHaloStroke
                    )
                    drawLine(
                        color = borderColor,
                        start = Offset(0f, by),
                        end = Offset(w, by),
                        strokeWidth = borderStroke,
                        pathEffect = dashEffect
                    )
                }

                // Animation d'onde de franchissement sur le bord de l'écran lorsque le joueur traverse la ligne rouge
                if (warpIntensity > 0f) {
                    val bandThickness = minDim * (0.18f * warpIntensity + 0.04f)
                    val edgeColors = listOf(
                        PlasmaRose.copy(alpha = 0.62f * warpIntensity),
                        CyberCyanGlow.copy(alpha = 0.32f * warpIntensity),
                        Color.Transparent
                    )
                    when (gameState.borderWrapEdge) {
                        "LEFT" -> {
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = edgeColors,
                                    startX = 0f,
                                    endX = bandThickness
                                ),
                                topLeft = Offset(0f, 0f),
                                size = Size(bandThickness, h)
                            )
                        }
                        "RIGHT" -> {
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = edgeColors.reversed(),
                                    startX = w - bandThickness,
                                    endX = w
                                ),
                                topLeft = Offset(w - bandThickness, 0f),
                                size = Size(bandThickness, h)
                            )
                        }
                        "TOP" -> {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = edgeColors,
                                    startY = 0f,
                                    endY = bandThickness
                                ),
                                topLeft = Offset(0f, 0f),
                                size = Size(w, bandThickness)
                            )
                        }
                        "BOTTOM" -> {
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = edgeColors.reversed(),
                                    startY = h - bandThickness,
                                    endY = h
                                ),
                                topLeft = Offset(0f, h - bandThickness),
                                size = Size(w, bandThickness)
                            )
                        }
                    }
                }

                // Draw Floating Fruit Relics (Amour, Joie, Paix, etc.) & Off-Screen Edge Radar Pointers
                for (relic in gameState.relics) {
                    if (!relic.isCollected && relic.isSpawned) {
                        val rx = (relic.x - camX) * w
                        val ry = ((relic.y + sin(relic.phase) * 0.012f) - camY) * h
                        val rRadius = minDim * 0.060f
                        val relicColor = MeditationsMatrix.getFruitGlowColor(relic.fruitId)

                        val isOnScreen = rx in -(rRadius * 3f)..(w + rRadius * 3f) && ry in -(rRadius * 3f)..(h + rRadius * 3f)
                        if (isOnScreen) {
                            // Sanctuaire d'Harmonisation (Zone d'Apaisement)
                            val sanctuaryPx = relic.sanctuaryRadius * minDim
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        relicColor.copy(alpha = if (relic.isHarmonizing) 0.38f else 0.12f),
                                        relicColor.copy(alpha = 0.04f),
                                        Color.Transparent
                                    ),
                                    center = Offset(rx, ry),
                                    radius = sanctuaryPx
                                ),
                                radius = sanctuaryPx,
                                center = Offset(rx, ry)
                            )
                            drawCircle(
                                color = relicColor.copy(alpha = if (relic.isHarmonizing) 0.70f else 0.28f),
                                radius = sanctuaryPx,
                                center = Offset(rx, ry),
                                style = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase = relic.phase * 15f)
                                )
                            )

                            // Harmonization Progress Arc
                            if (relic.harmonizationProgress > 0f) {
                                drawArc(
                                    color = SolarGoldGlow,
                                    startAngle = -90f,
                                    sweepAngle = relic.harmonizationProgress * 360f,
                                    useCenter = false,
                                    topLeft = Offset(rx - rRadius * 1.35f, ry - rRadius * 1.35f),
                                    size = Size(rRadius * 2.7f, rRadius * 2.7f),
                                    style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                                )

                                val previewSnippet = "🕊️ ${relic.keep.ifBlank { relic.verse }.take(32)}..."
                                val snippetMeasured = textMeasurer.measure(
                                    text = previewSnippet,
                                    style = TextStyle(
                                        color = SolarGoldGlow,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                drawText(
                                    textLayoutResult = snippetMeasured,
                                    topLeft = Offset(
                                        rx - snippetMeasured.size.width / 2f,
                                        ry - rRadius * 1.55f - snippetMeasured.size.height
                                    )
                                )
                            }

                            // Outer Holographic Halo
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        relicColor.copy(alpha = 0.50f),
                                        relicColor.copy(alpha = 0.12f),
                                        Color.Transparent
                                    ),
                                    center = Offset(rx, ry),
                                    radius = rRadius * 2.0f
                                ),
                                radius = rRadius * 2.0f,
                                center = Offset(rx, ry)
                            )

                            // Orbital Ring
                            drawCircle(
                                color = relicColor,
                                radius = rRadius,
                                center = Offset(rx, ry),
                                style = Stroke(width = 3.dp.toPx())
                            )
                            drawCircle(
                                color = SpaceBackground.copy(alpha = 0.88f),
                                radius = rRadius * 0.88f,
                                center = Offset(rx, ry)
                            )

                            // Draw Fruit Emoji & Name in center
                            val emojiText = relic.fruitEmoji.ifEmpty { "✨" }
                            val emojiMeasured = textMeasurer.measure(
                                text = emojiText,
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            )
                            drawText(
                                textLayoutResult = emojiMeasured,
                                topLeft = Offset(
                                    rx - emojiMeasured.size.width / 2f,
                                    ry - emojiMeasured.size.height / 2f - 6.dp.toPx()
                                )
                            )

                            val nameMeasured = textMeasurer.measure(
                                text = relic.letter,
                                style = TextStyle(
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    color = relicColor
                                )
                            )
                            drawText(
                                textLayoutResult = nameMeasured,
                                topLeft = Offset(
                                    rx - nameMeasured.size.width / 2f,
                                    ry + emojiMeasured.size.height / 2f - 4.dp.toPx()
                                )
                            )
                        } else {
                            // Off-screen edge radar beacon showing direction to the Fruit facet
                            val pad = 30.dp.toPx()
                            val indicatorX = rx.coerceIn(pad, w - pad)
                            val indicatorY = ry.coerceIn(pad, h - pad)
                            val indRadius = 18.dp.toPx()

                            drawCircle(
                                color = SpaceSurface.copy(alpha = 0.95f),
                                radius = indRadius,
                                center = Offset(indicatorX, indicatorY)
                            )
                            drawCircle(
                                color = relicColor,
                                radius = indRadius,
                                center = Offset(indicatorX, indicatorY),
                                style = Stroke(width = 2.dp.toPx())
                            )
                            val beaconText = relic.fruitEmoji.ifEmpty { relic.letter.take(1) }
                            val labelMeasured = textMeasurer.measure(
                                text = beaconText,
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            )
                            drawText(
                                textLayoutResult = labelMeasured,
                                topLeft = Offset(
                                    indicatorX - labelMeasured.size.width / 2f,
                                    indicatorY - labelMeasured.size.height / 2f
                                )
                            )
                        }
                    }
                }

                // Draw Brain Bonus Relic (🧠 Fragment 1..72) in Camera Space & Off-Screen Beacon
                val brainRelic = gameState.brainRelic
                if (brainRelic != null && !brainRelic.isCollected && brainRelic.isSpawned) {
                    val brx = (brainRelic.x - camX) * w
                    val bry = ((brainRelic.y + sin(brainRelic.phase) * 0.015f) - camY) * h
                    val brRadius = minDim * 0.062f
                    val brainGlow = VioletPulsarGlow

                    val isOnScreen = brx in -(brRadius * 3f)..(w + brRadius * 3f) && bry in -(brRadius * 3f)..(h + brRadius * 3f)
                    if (isOnScreen) {
                        // Sanctuaire d'Harmonisation Brain Bonus
                        val sanctuaryPx = brainRelic.sanctuaryRadius * minDim
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    brainGlow.copy(alpha = if (brainRelic.isHarmonizing) 0.38f else 0.12f),
                                    Color(0xFFEC4899).copy(alpha = 0.04f),
                                    Color.Transparent
                                ),
                                center = Offset(brx, bry),
                                radius = sanctuaryPx
                            ),
                            radius = sanctuaryPx,
                            center = Offset(brx, bry)
                        )
                        drawCircle(
                            color = brainGlow.copy(alpha = if (brainRelic.isHarmonizing) 0.70f else 0.28f),
                            radius = sanctuaryPx,
                            center = Offset(brx, bry),
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase = brainRelic.phase * 15f)
                            )
                        )

                        // Progress gauge when harmonizing
                        if (brainRelic.harmonizationProgress > 0f) {
                            drawArc(
                                color = SolarGoldGlow,
                                startAngle = -90f,
                                sweepAngle = brainRelic.harmonizationProgress * 360f,
                                useCenter = false,
                                topLeft = Offset(brx - brRadius * 1.35f, bry - brRadius * 1.35f),
                                size = Size(brRadius * 2.7f, brRadius * 2.7f),
                                style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Pulsing Holographic Aura
                        val pulse = 1f + 0.12f * sin(brainRelic.phase * 2f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    brainGlow.copy(alpha = 0.55f),
                                    Color(0xFFEC4899).copy(alpha = 0.18f),
                                    Color.Transparent
                                ),
                                center = Offset(brx, bry),
                                radius = brRadius * 2.2f * pulse
                            ),
                            radius = brRadius * 2.2f * pulse,
                            center = Offset(brx, bry)
                        )

                        // Rotating Dashed Orbit Ring
                        val rotAngle = brainRelic.phase * 45f
                        rotate(degrees = rotAngle, pivot = Offset(brx, bry)) {
                            drawCircle(
                                color = brainGlow,
                                radius = brRadius * 1.15f,
                                center = Offset(brx, bry),
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), phase = 0f)
                                )
                            )
                        }

                        // Core Capsule
                        drawCircle(
                            color = SpaceBackground.copy(alpha = 0.90f),
                            radius = brRadius * 0.92f,
                            center = Offset(brx, bry)
                        )
                        drawCircle(
                            color = brainGlow,
                            radius = brRadius * 0.92f,
                            center = Offset(brx, bry),
                            style = Stroke(width = 2.5.dp.toPx())
                        )

                        // Central Brain Emoji 🧠
                        val emojiMeasured = textMeasurer.measure(
                            text = "🧠",
                            style = TextStyle(fontSize = 17.sp)
                        )
                        drawText(
                            textLayoutResult = emojiMeasured,
                            topLeft = Offset(
                                brx - emojiMeasured.size.width / 2f,
                                bry - emojiMeasured.size.height / 2f - 6.dp.toPx()
                            )
                        )

                        // Subtitle "BONUS #N"
                        val tagMeasured = textMeasurer.measure(
                            text = "BONUS #${brainRelic.brainLevel}",
                            style = TextStyle(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 8.5.sp,
                                color = Color(0xFFF472B6)
                            )
                        )
                        drawText(
                            textLayoutResult = tagMeasured,
                            topLeft = Offset(
                                brx - tagMeasured.size.width / 2f,
                                bry + emojiMeasured.size.height / 2f - 4.dp.toPx()
                            )
                        )
                    } else {
                        // Off-screen edge radar beacon pointing to Brain Bonus
                        val pad = 30.dp.toPx()
                        val indicatorX = brx.coerceIn(pad, w - pad)
                        val indicatorY = bry.coerceIn(pad, h - pad)
                        val indRadius = 18.dp.toPx()

                        drawCircle(
                            color = SpaceSurface.copy(alpha = 0.95f),
                            radius = indRadius,
                            center = Offset(indicatorX, indicatorY)
                        )
                        drawCircle(
                            color = brainGlow,
                            radius = indRadius,
                            center = Offset(indicatorX, indicatorY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        val beaconText = textMeasurer.measure(
                            text = "🧠",
                            style = TextStyle(fontSize = 13.sp)
                        )
                        drawText(
                            textLayoutResult = beaconText,
                            topLeft = Offset(
                                indicatorX - beaconText.size.width / 2f,
                                indicatorY - beaconText.size.height / 2f
                            )
                        )
                    }
                }

                // Draw Space Power-Ups (Shield, Speed Boost, Magnet, EMP Nova) in Camera Space
                for (pup in gameState.powerUps) {
                    if (!pup.isCollected) {
                        val px = (pup.x - camX) * w
                        val py = (pup.y - camY) * h
                        val pRadius = pup.radius * minDim

                        if (px in -(pRadius * 2.5f)..(w + pRadius * 2.5f) && py in -(pRadius * 2.5f)..(h + pRadius * 2.5f)) {
                            val pulse = 1f + 0.14f * sin(pup.phase)
                            val glowColor = pup.type.glowColor
                            val primaryColor = pup.type.primaryColor

                            // Pulsing Outer Aura
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        glowColor.copy(alpha = 0.45f),
                                        primaryColor.copy(alpha = 0.12f),
                                        Color.Transparent
                                    ),
                                    center = Offset(px, py),
                                    radius = pRadius * 2.2f * pulse
                                ),
                                radius = pRadius * 2.2f * pulse,
                                center = Offset(px, py)
                            )

                            // Rotating Orbiting Ring
                            val ringAngle = pup.phase * 50f
                            rotate(degrees = ringAngle, pivot = Offset(px, py)) {
                                drawCircle(
                                    color = primaryColor,
                                    radius = pRadius * 1.15f * pulse,
                                    center = Offset(px, py),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase = pup.phase * 15f)
                                    )
                                )
                            }

                            // Glowing Core Capsule
                            drawCircle(
                                color = SpaceBackground.copy(alpha = 0.90f),
                                radius = pRadius * 0.95f,
                                center = Offset(px, py)
                            )
                            drawCircle(
                                color = glowColor,
                                radius = pRadius * 0.95f,
                                center = Offset(px, py),
                                style = Stroke(width = 2.dp.toPx())
                            )

                            // Central Power-Up Emoji (🛡️, ⚡, 🧲, 💥)
                            val pupEmojiMeasured = textMeasurer.measure(
                                text = pup.type.emoji,
                                style = TextStyle(fontSize = 15.sp)
                            )
                            drawText(
                                textLayoutResult = pupEmojiMeasured,
                                topLeft = Offset(
                                    px - pupEmojiMeasured.size.width / 2f,
                                    py - pupEmojiMeasured.size.height / 2f
                                )
                            )

                            // Orbiting Spark
                            val sparkAngle = pup.phase * 2f
                            val sparkX = px + cos(sparkAngle) * pRadius * 1.25f
                            val sparkY = py + sin(sparkAngle) * pRadius * 1.25f
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = Offset(sparkX, sparkY)
                            )
                        }
                    }
                }

                // Draw Rescue Targets (Les Étincelles de Lucidité, Les Fragments de Savoir, Les Souvenirs)
                // Note : dissimulés visuellement lorsqu'ils se trouvent à l'intérieur d'un Voile d'Obscurantisme (L'Ignorance)
                for (target in gameState.rescueTargets) {
                    if (!target.isSaved && !target.isDestroyed && !gameState.isTargetHiddenByObscurantism(target)) {
                        val tx = (target.x - camX) * w
                        val ty = ((target.y + sin(target.phase) * 0.008f) - camY) * h
                        val tRadius = target.radius * minDim

                        if (tx in -(tRadius * 3f)..(w + tRadius * 3f) && ty in -(tRadius * 3f)..(h + tRadius * 3f)) {
                            val pulse = 1f + 0.16f * sin(target.phase * 1.6f)
                            when (target.type) {
                                RescueTargetType.LUCIDITY_SPARK -> {
                                    // Étincelle de Lucidité : Petite orbe lumineuse très vive prête à éclore
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.85f),
                                                CyberCyanGlow.copy(alpha = 0.55f),
                                                CyberCyan.copy(alpha = 0.15f),
                                                Color.Transparent
                                            ),
                                            center = Offset(tx, ty),
                                            radius = tRadius * 2.3f * pulse
                                        ),
                                        radius = tRadius * 2.3f * pulse,
                                        center = Offset(tx, ty)
                                    )
                                    // 8 Blooming Starburst Light Rays
                                    for (ray in 0 until 8) {
                                        val rayAngle = (ray * 45f + target.phase * 25f) * (Math.PI.toFloat() / 180f)
                                        val rayLen = if (ray % 2 == 0) tRadius * 1.65f * pulse else tRadius * 1.15f
                                        drawLine(
                                            color = if (ray % 2 == 0) Color.White else CyberCyanGlow,
                                            start = Offset(tx - cos(rayAngle) * rayLen, ty - sin(rayAngle) * rayLen),
                                            end = Offset(tx + cos(rayAngle) * rayLen, ty + sin(rayAngle) * rayLen),
                                            strokeWidth = if (ray % 2 == 0) 2.2.dp.toPx() else 1.4.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                    drawCircle(
                                        color = CyberCyanGlow,
                                        radius = tRadius * 0.68f,
                                        center = Offset(tx, ty)
                                    )
                                    drawCircle(
                                        color = Color.White,
                                        radius = tRadius * 0.42f * pulse,
                                        center = Offset(tx, ty)
                                    )
                                }

                                RescueTargetType.KNOWLEDGE_FRAGMENT -> {
                                    // Fragment de Savoir : Cristal lumineux & glyphe géométrique doré
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                SolarGoldGlow.copy(alpha = 0.55f),
                                                SolarGold.copy(alpha = 0.18f),
                                                Color.Transparent
                                            ),
                                            center = Offset(tx, ty),
                                            radius = tRadius * 2.1f * pulse
                                        ),
                                        radius = tRadius * 2.1f * pulse,
                                        center = Offset(tx, ty)
                                    )
                                    val rotDeg = target.phase * 38f
                                    // Outer Sacred Geometric Crystal Rhombus
                                    rotate(degrees = rotDeg, pivot = Offset(tx, ty)) {
                                        val crystalPath = Path().apply {
                                            moveTo(tx, ty - tRadius * 1.25f)
                                            lineTo(tx + tRadius * 0.95f, ty)
                                            lineTo(tx, ty + tRadius * 1.25f)
                                            lineTo(tx - tRadius * 0.95f, ty)
                                            close()
                                        }
                                        drawPath(
                                            path = crystalPath,
                                            color = SolarGold.copy(alpha = 0.32f)
                                        )
                                        drawPath(
                                            path = crystalPath,
                                            color = SolarGoldGlow,
                                            style = Stroke(width = 2.2.dp.toPx())
                                        )
                                    }
                                    // Counter-rotating Golden Glyph Square
                                    rotate(degrees = -rotDeg + 45f, pivot = Offset(tx, ty)) {
                                        val glyphPath = Path().apply {
                                            moveTo(tx, ty - tRadius * 1.05f)
                                            lineTo(tx + tRadius * 1.05f, ty)
                                            lineTo(tx, ty + tRadius * 1.05f)
                                            lineTo(tx - tRadius * 1.05f, ty)
                                            close()
                                        }
                                        drawPath(
                                            path = glyphPath,
                                            color = Color(0xFFFFFBEB),
                                            style = Stroke(width = 1.5.dp.toPx())
                                        )
                                    }
                                    drawCircle(
                                        color = SolarGoldGlow,
                                        radius = tRadius * 0.36f * pulse,
                                        center = Offset(tx, ty)
                                    )
                                }

                                RescueTargetType.FRAGILE_MEMORY -> {
                                    // Souvenir : Bulle fragile irisée contenant un concept précieux
                                    val bubbleRadius = tRadius * 1.15f * (1f + 0.08f * sin(target.phase * 2.2f))
                                    drawCircle(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                VioletPulsarGlow.copy(alpha = 0.14f),
                                                PlasmaRose.copy(alpha = 0.34f)
                                            ),
                                            center = Offset(tx, ty),
                                            radius = bubbleRadius
                                        ),
                                        radius = bubbleRadius,
                                        center = Offset(tx, ty)
                                    )
                                    // Fragile Iridescent Membrane Rim
                                    drawCircle(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(
                                                PlasmaRose,
                                                CyberCyanGlow,
                                                VioletPulsarGlow,
                                                SolarGoldGlow,
                                                PlasmaRose
                                            ),
                                            center = Offset(tx, ty)
                                        ),
                                        radius = bubbleRadius,
                                        center = Offset(tx, ty),
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                    // Specular Bubble Highlight Arc (Top-Left reflection)
                                    drawArc(
                                        color = Color.White.copy(alpha = 0.80f),
                                        startAngle = 200f,
                                        sweepAngle = 55f,
                                        useCenter = false,
                                        topLeft = Offset(tx - bubbleRadius * 0.72f, ty - bubbleRadius * 0.72f),
                                        size = Size(bubbleRadius * 1.44f, bubbleRadius * 1.44f),
                                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                    // Tiny inner concept pearl
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.75f),
                                        radius = tRadius * 0.22f,
                                        center = Offset(tx, ty)
                                    )
                                }
                            }

                            // Bulle d'invulnérabilité dorée (Phare de Métacognition — Niv. 60)
                            if (gameState.metacognitionBeaconTimer > 0f) {
                                val shieldBubbleR = tRadius * 1.55f * (1f + 0.08f * sin(gameState.elapsedTime * 5f + target.id))
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFFFDE047).copy(alpha = 0.16f),
                                            Color(0xFFF59E0B).copy(alpha = 0.38f)
                                        ),
                                        center = Offset(tx, ty),
                                        radius = shieldBubbleR
                                    ),
                                    radius = shieldBubbleR,
                                    center = Offset(tx, ty)
                                )
                                drawCircle(
                                    color = Color(0xFFFDE047).copy(alpha = 0.90f),
                                    radius = shieldBubbleR,
                                    center = Offset(tx, ty),
                                    style = Stroke(
                                        width = 1.8.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), phase = gameState.elapsedTime * 28f)
                                    )
                                )
                            }

                            // Poetic Concept Tag below the target
                            val tagLabel = "${target.type.emoji} ${target.conceptLabel}"
                            val tagMeasured = textMeasurer.measure(
                                text = tagLabel,
                                style = TextStyle(
                                    color = target.type.glowColor,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            drawText(
                                textLayoutResult = tagMeasured,
                                topLeft = Offset(
                                    tx - tagMeasured.size.width / 2f,
                                    ty + tRadius * 1.28f
                                )
                            )
                        }
                    }
                }

                // Draw Infected & Purified Neurons in Space (Purification progressive & Résonance)
                val playerScreenX = (gameState.playerX - camX) * w
                val playerScreenY = (gameState.playerY - camY) * h
                for (neuron in gameState.infectedNeurons) {
                    val nx = (neuron.x - camX) * w
                    val ny = ((neuron.y + sin(neuron.phase * 0.7f) * 0.006f) - camY) * h
                    val nRadius = neuron.radius * minDim
                    val isNeuronOnScreen = nx in -(nRadius * 3.5f)..(w + nRadius * 3.5f) &&
                        ny in -(nRadius * 3.5f)..(h + nRadius * 3.5f)
                    val isVisibleAndNearby = gameState.isNeuronVisibleAndNearby(neuron)

                    if (isNeuronOnScreen) {
                        val prog = neuron.purificationProgress.coerceIn(0f, 1f)
                        val coreCol = lerp(
                            neuron.corruptionType.infectedCoreColor,
                            neuron.corruptionType.purifiedCoreColor,
                            prog
                        )
                        val auraCol = lerp(
                            neuron.corruptionType.infectedAuraColor,
                            neuron.corruptionType.purifiedAuraColor,
                            prog
                        )
                        val pulse = if (neuron.isPurified) {
                            1f + 0.14f * sin(neuron.phase * 1.5f)
                        } else {
                            when (neuron.corruptionType) {
                                NeuronCorruptionType.AGITATION -> 1f + 0.24f * sin(neuron.phase * 3.5f)
                                NeuronCorruptionType.TRISTESSE -> 1f + 0.08f * sin(neuron.phase * 0.9f)
                                NeuronCorruptionType.COLERE_PEUR -> 1f + 0.18f * sin(neuron.phase * 2.4f)
                                NeuronCorruptionType.RANCOEUR -> 1f + 0.14f * sin(neuron.phase * 1.8f)
                                NeuronCorruptionType.SURTENSION -> 1f + 0.28f * sin(neuron.phase * 4.5f)
                                NeuronCorruptionType.APATHIE -> 1f + 0.04f * sin(neuron.phase * 0.55f)
                                NeuronCorruptionType.RIGIDITE_OBSESSION -> 1f + 0.12f * sin(neuron.phase * 2.1f)
                                NeuronCorruptionType.BURNOUT_DISSOCIATION -> 1f + 0.06f * sin(neuron.phase * 0.7f)
                            }
                        }

                        // Lien de proximité lumineux entre le joueur et le neurone infecté lorsqu'il est à proximité sur la carte
                        if (isVisibleAndNearby) {
                            val linkColor = if (gameState.purificationEnergyPoints >= 10) {
                                SolarGoldGlow.copy(alpha = 0.78f)
                            } else {
                                CyberCyanGlow.copy(alpha = 0.45f)
                            }
                            drawLine(
                                color = linkColor,
                                start = Offset(playerScreenX, playerScreenY),
                                end = Offset(nx, ny),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(12f, 8f),
                                    phase = -gameState.elapsedTime * 45f
                                )
                            )
                            drawCircle(
                                color = linkColor,
                                radius = nRadius * 1.48f * pulse,
                                center = Offset(nx, ny),
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(10f, 6f),
                                        phase = gameState.elapsedTime * 35f
                                    )
                                )
                            )
                            val proxBadge = textMeasurer.measure(
                                text = "🎯 À PROXIMITÉ",
                                style = TextStyle(
                                    color = if (gameState.purificationEnergyPoints >= 10) SolarGoldGlow else CyberCyanGlow,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            drawText(
                                textLayoutResult = proxBadge,
                                topLeft = Offset(
                                    nx - proxBadge.size.width / 2f,
                                    ny - nRadius * 1.95f - proxBadge.size.height
                                )
                            )
                        }

                        // Outer Aura : passes from dark/abrasive red/violet to luminous Golden or Cyan
                        val auraMultiplier = if (neuron.isPurified) 2.75f else (2.15f + prog * 0.5f)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    auraCol.copy(alpha = if (neuron.isPurified) 0.72f else (0.45f + prog * 0.25f)),
                                    if (neuron.isPurified) SolarGoldGlow.copy(alpha = 0.28f) else coreCol.copy(alpha = 0.22f),
                                    Color.Transparent
                                ),
                                center = Offset(nx, ny),
                                radius = nRadius * auraMultiplier * pulse
                            ),
                            radius = nRadius * auraMultiplier * pulse,
                            center = Offset(nx, ny)
                        )

                        // 6 Neuronal Dendrites & Synaptic Terminals
                        val dendriteCount = 6
                        for (d in 0 until dendriteCount) {
                            val baseAngle = (d * 60f) * (Math.PI.toFloat() / 180f)
                            val tremor = if (neuron.isPurified) {
                                sin(neuron.phase + d) * 0.05f
                            } else {
                                sin(neuron.phase * 2.6f + d * 1.7f) * (0.22f * (1f - prog))
                            }
                            val angle = baseAngle + tremor
                            val dendriteBaseScale = if (!neuron.isPurified && neuron.corruptionType == NeuronCorruptionType.SURTENSION) {
                                1.15f // Branches synaptiques contractées et rapides pour Surtension & Hyperventilation
                            } else {
                                1.65f
                            }
                            val dendriteLen = nRadius * (dendriteBaseScale + 0.18f * sin(neuron.phase + d * 1.2f))
                            val midX = nx + cos(angle - 0.15f) * (dendriteLen * 0.55f)
                            val midY = ny + sin(angle - 0.15f) * (dendriteLen * 0.55f)
                            val endX = nx + cos(angle) * dendriteLen
                            val endY = ny + sin(angle) * dendriteLen

                            val dendritePath = Path().apply {
                                moveTo(nx, ny)
                                quadraticTo(midX, midY, endX, endY)
                            }
                            drawPath(
                                path = dendritePath,
                                color = if (neuron.hitFlashTimer > 0f) Color.White else auraCol,
                                style = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
                            )
                            // Synaptic Bulb at Dendrite Tip
                            drawCircle(
                                color = if (neuron.isPurified) SolarGoldGlow else auraCol,
                                radius = 3.2.dp.toPx() * pulse,
                                center = Offset(endX, endY)
                            )
                        }

                        // Neuron Soma (Cell Body)
                        drawCircle(
                            color = coreCol,
                            radius = nRadius * 0.88f * pulse,
                            center = Offset(nx, ny)
                        )
                        drawCircle(
                            color = if (neuron.hitFlashTimer > 0f) Color.White else auraCol,
                            radius = nRadius * 0.88f * pulse,
                            center = Offset(nx, ny),
                            style = Stroke(width = 2.2.dp.toPx())
                        )

                        // Progressive Purification Ring Arc (0% -> 100%)
                        if (prog > 0f && !neuron.isPurified) {
                            drawArc(
                                color = SolarGoldGlow,
                                startAngle = -90f,
                                sweepAngle = 360f * prog,
                                useCenter = false,
                                topLeft = Offset(nx - nRadius * 1.15f, ny - nRadius * 1.15f),
                                size = Size(nRadius * 2.3f, nRadius * 2.3f),
                                style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round)
                            )
                        } else if (neuron.isPurified) {
                            // Radiant Golden & Cyan Halo when purified
                            drawCircle(
                                color = SolarGoldGlow.copy(alpha = 0.85f),
                                radius = nRadius * 1.22f * pulse,
                                center = Offset(nx, ny),
                                style = Stroke(width = 2.2.dp.toPx())
                            )
                        }

                        // Inner Nucleus
                        drawCircle(
                            color = if (neuron.isPurified) Color.White else auraCol.copy(alpha = 0.85f),
                            radius = nRadius * 0.34f * pulse,
                            center = Offset(nx, ny)
                        )

                        // Label below the Neuron: Affect négatif + Remède de Vertu (or Illuminé when purified)
                        val statusText = if (neuron.isPurified) {
                            "✨ Purifié : ${neuron.corruptionType.remedyVirtue.displayName} (+10 pts)"
                        } else if (prog > 0f) {
                            "🧠 ${neuron.negativeAffectLabel} (${(prog * 100).toInt()}%) • ${neuron.corruptionType.categoryTitle}"
                        } else {
                            "🧠 ${neuron.negativeAffectLabel} • ${neuron.corruptionType.categoryTitle}"
                        }
                        val labelColor = if (neuron.isPurified) {
                            SolarGoldGlow
                        } else if (prog > 0f) {
                            CyberCyanGlow
                        } else {
                            Color(0xFFFCA5A5)
                        }
                        val nLabelMeasured = textMeasurer.measure(
                            text = statusText,
                            style = TextStyle(
                                color = labelColor,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        drawText(
                            textLayoutResult = nLabelMeasured,
                            topLeft = Offset(
                                nx - nLabelMeasured.size.width / 2f,
                                ny + nRadius * 1.55f
                            )
                        )
                    } else if (!neuron.isPurified) {
                        // Balise directionnelle en bord d'écran vers le neurone infecté hors-écran
                        // pour aider le joueur à s'en approcher sur la carte
                        val pad = 34.dp.toPx()
                        val indicatorX = nx.coerceIn(pad, w - pad)
                        val indicatorY = ny.coerceIn(pad, h - pad)
                        val indRadius = 15.dp.toPx()

                        drawCircle(
                            color = SpaceSurface.copy(alpha = 0.90f),
                            radius = indRadius,
                            center = Offset(indicatorX, indicatorY)
                        )
                        drawCircle(
                            color = neuron.corruptionType.infectedAuraColor,
                            radius = indRadius,
                            center = Offset(indicatorX, indicatorY),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        val beaconText = textMeasurer.measure(
                            text = "🧠",
                            style = TextStyle(fontSize = 11.sp)
                        )
                        drawText(
                            textLayoutResult = beaconText,
                            topLeft = Offset(
                                indicatorX - beaconText.size.width / 2f,
                                indicatorY - beaconText.size.height / 2f
                            )
                        )
                    }
                }

                // Draw Concentric Gamma Waves (Niveau 120 : Résonance Gamma / Synchronie)
                for (wave in gameState.gammaWaves) {
                    val wx = (wave.x - camX) * w
                    val wy = (wave.y - camY) * h
                    val wRadius = wave.currentRadius * minDim
                    val alphaFactor = (wave.life / 1.4f).coerceIn(0f, 1f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFC084FC).copy(alpha = 0.28f * alphaFactor),
                                Color(0xFFFDE047).copy(alpha = 0.55f * alphaFactor),
                                Color.Transparent
                            ),
                            center = Offset(wx, wy),
                            radius = wRadius.coerceAtLeast(10f)
                        ),
                        radius = wRadius.coerceAtLeast(10f),
                        center = Offset(wx, wy)
                    )
                    drawCircle(
                        color = Color(0xFFFDE047).copy(alpha = 0.85f * alphaFactor),
                        radius = wRadius.coerceAtLeast(6f),
                        center = Offset(wx, wy),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 8f), phase = gameState.elapsedTime * 90f)
                        )
                    )
                }

                // Draw Bioelectric Chain Arcs (Niveau 100 : Influx Bioélectrique / Potentiel d'Action)
                for (arc in gameState.bioelectricArcs) {
                    val sx = (arc.startX - camX) * w
                    val sy = (arc.startY - camY) * h
                    val ex = (arc.endX - camX) * w
                    val ey = (arc.endY - camY) * h
                    val arcAlpha = (arc.life / 0.55f).coerceIn(0f, 1f)
                    val midX = (sx + ex) * 0.5f + sin(gameState.elapsedTime * 45f) * 14.dp.toPx()
                    val midY = (sy + ey) * 0.5f + cos(gameState.elapsedTime * 45f) * 14.dp.toPx()
                    val lightningPath = Path().apply {
                        moveTo(sx, sy)
                        lineTo(midX, midY)
                        lineTo(ex, ey)
                    }
                    drawPath(
                        path = lightningPath,
                        color = Color(0xFF38BDF8).copy(alpha = 0.88f * arcAlpha),
                        style = Stroke(width = 4.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = lightningPath,
                        color = Color(0xFFF0FDF4).copy(alpha = 0.95f * arcAlpha),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Draw Virtue Word Projectiles (« PAIX », « JOIE », « MAÎTRISE DE SOI », « AMOUR », et paliers N.30-120)
                for (proj in gameState.virtueProjectiles) {
                    val vxScreen = (proj.x - camX) * w
                    val vyScreen = (proj.y - camY) * h
                    if (vxScreen in -80f..(w + 80f) && vyScreen in -80f..(h + 80f)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.9f),
                                    proj.glowColor.copy(alpha = 0.65f),
                                    Color.Transparent
                                ),
                                center = Offset(vxScreen, vyScreen),
                                radius = 20.dp.toPx()
                            ),
                            radius = 20.dp.toPx(),
                            center = Offset(vxScreen, vyScreen)
                        )
                        val wordTextMeasured = textMeasurer.measure(
                            text = "${proj.virtue.emoji} ${proj.wordLabel}",
                            style = TextStyle(
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        val padX = 7.dp.toPx()
                        val padY = 3.dp.toPx()
                        val pillW = wordTextMeasured.size.width + padX * 2f
                        val pillH = wordTextMeasured.size.height + padY * 2f
                        drawRoundRect(
                            color = SpaceBackground.copy(alpha = 0.88f),
                            topLeft = Offset(vxScreen - pillW / 2f, vyScreen - pillH / 2f),
                            size = Size(pillW, pillH),
                            cornerRadius = CornerRadius(10.dp.toPx())
                        )
                        drawRoundRect(
                            color = proj.glowColor,
                            topLeft = Offset(vxScreen - pillW / 2f, vyScreen - pillH / 2f),
                            size = Size(pillW, pillH),
                            cornerRadius = CornerRadius(10.dp.toPx()),
                            style = Stroke(width = 1.6.dp.toPx())
                        )
                        drawText(
                            textLayoutResult = wordTextMeasured,
                            topLeft = Offset(
                                vxScreen - wordTextMeasured.size.width / 2f,
                                vyScreen - wordTextMeasured.size.height / 2f
                            )
                        )
                    }
                }

                // Draw Rapid Purification Feedback Effects (0%, 50% or 100% Purifié shockwaves & floating badges)
                for (fb in gameState.purificationFeedbacks) {
                    val fx = (fb.x - camX) * w
                    val fy = (fb.y - camY) * h
                    if (fx in -120f..(w + 120f) && fy in -120f..(h + 120f)) {
                        val progress = (1f - (fb.life / fb.maxLife)).coerceIn(0f, 1f)
                        val alpha = (1f - progress * 0.85f).coerceIn(0f, 1f)
                        val baseWaveR = minDim * (0.045f + progress * 0.16f)

                        // Radial flash halo at impact point
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    fb.glowColor.copy(alpha = 0.55f * alpha),
                                    fb.primaryColor.copy(alpha = 0.25f * alpha),
                                    Color.Transparent
                                ),
                                center = Offset(fx, fy),
                                radius = baseWaveR * 1.4f
                            ),
                            radius = baseWaveR * 1.4f,
                            center = Offset(fx, fy)
                        )

                        when {
                            fb.percentage >= 100 -> {
                                // 100% PURIFIÉ : Double anneau d'onde doré/émeraude + 8 rayons stellaires
                                drawCircle(
                                    color = SolarGoldGlow.copy(alpha = 0.92f * alpha),
                                    radius = baseWaveR,
                                    center = Offset(fx, fy),
                                    style = Stroke(width = 3.dp.toPx())
                                )
                                drawCircle(
                                    color = EmeraldNovaGlow.copy(alpha = 0.80f * alpha),
                                    radius = baseWaveR * 0.72f,
                                    center = Offset(fx, fy),
                                    style = Stroke(
                                        width = 2.2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f), phase = progress * 60f)
                                    )
                                )
                                for (ray in 0 until 8) {
                                    val ang = (ray * 45f + progress * 35f) * (Math.PI.toFloat() / 180f)
                                    val innerR = baseWaveR * 0.35f
                                    val outerR = baseWaveR * 1.18f
                                    drawLine(
                                        color = SolarGoldGlow.copy(alpha = 0.85f * alpha),
                                        start = Offset(fx + cos(ang) * innerR, fy + sin(ang) * innerR),
                                        end = Offset(fx + cos(ang) * outerR, fy + sin(ang) * outerR),
                                        strokeWidth = 2.2.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                }
                            }
                            fb.percentage >= 50 -> {
                                // 50% PURIFIÉ : Anneau harmonique cyan + demi-arc doré à 180°
                                drawCircle(
                                    color = CyberCyanGlow.copy(alpha = 0.88f * alpha),
                                    radius = baseWaveR * 0.90f,
                                    center = Offset(fx, fy),
                                    style = Stroke(
                                        width = 2.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), phase = progress * 45f)
                                    )
                                )
                                val arcR = baseWaveR * 0.68f
                                drawArc(
                                    color = SolarGoldGlow.copy(alpha = 0.92f * alpha),
                                    startAngle = -90f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(fx - arcR, fy - arcR),
                                    size = Size(arcR * 2f, arcR * 2f),
                                    style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            else -> {
                                // 0% PURIFIÉ : Onde dissonante rouge + croix de déflexion
                                drawCircle(
                                    color = DangerRed.copy(alpha = 0.88f * alpha),
                                    radius = baseWaveR * 0.82f,
                                    center = Offset(fx, fy),
                                    style = Stroke(
                                        width = 2.4.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), phase = -progress * 40f)
                                    )
                                )
                                val crossR = baseWaveR * 0.42f
                                drawLine(
                                    color = Color(0xFFFCA5A5).copy(alpha = 0.90f * alpha),
                                    start = Offset(fx - crossR, fy - crossR),
                                    end = Offset(fx + crossR, fy + crossR),
                                    strokeWidth = 2.6.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                drawLine(
                                    color = Color(0xFFFCA5A5).copy(alpha = 0.90f * alpha),
                                    start = Offset(fx + crossR, fy - crossR),
                                    end = Offset(fx - crossR, fy + crossR),
                                    strokeWidth = 2.6.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }

                        // Badge flottant au-dessus du neurone : "100% PURIFIÉ !", "50% PURIFIÉ !", ou "0% PURIFIÉ !"
                        val fbTextMeasured = textMeasurer.measure(
                            text = fb.label,
                            style = TextStyle(
                                color = Color.White.copy(alpha = alpha),
                                fontSize = 11.sp,
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        val padX = 9.dp.toPx()
                        val padY = 4.dp.toPx()
                        val badgeW = fbTextMeasured.size.width + padX * 2f
                        val badgeH = fbTextMeasured.size.height + padY * 2f
                        val badgeTop = fy - minDim * 0.065f - badgeH
                        val badgeLeft = (fx - badgeW / 2f).coerceIn(8.dp.toPx(), (w - badgeW - 8.dp.toPx()).coerceAtLeast(8.dp.toPx()))

                        drawRoundRect(
                            color = SpaceBackground.copy(alpha = 0.92f * alpha),
                            topLeft = Offset(badgeLeft, badgeTop),
                            size = Size(badgeW, badgeH),
                            cornerRadius = CornerRadius(12.dp.toPx())
                        )
                        drawRoundRect(
                            color = fb.glowColor.copy(alpha = alpha),
                            topLeft = Offset(badgeLeft, badgeTop),
                            size = Size(badgeW, badgeH),
                            cornerRadius = CornerRadius(12.dp.toPx()),
                            style = Stroke(width = 2.dp.toPx())
                        )
                        drawText(
                            textLayoutResult = fbTextMeasured,
                            topLeft = Offset(badgeLeft + padX, badgeTop + padY)
                        )
                    }
                }

                // Draw Redesigned Mental Obstacles:
                // 1) ASTEROID -> Pensée Parasite / Toxine Cérébrale (Obstacle lourd et erratique)
                // 2) LASER_DRONE -> Foyer d'Angoisse / Pic de Stress (Obstacle traqueur ou à faisceau)
                // 3) PULSAR_MINE -> Crise de Panique / Décharge Émotive (Obstacle explosif à pulsations)
                for (obs in gameState.obstacles) {
                    val ox = (obs.x - camX) * w
                    val oy = (obs.y - camY) * h
                    val oRadius = obs.radius * minDim

                    if (ox in -(oRadius * 3.5f)..(w + oRadius * 3.5f) && oy in -(oRadius * 3.5f)..(h + oRadius * 3.5f)) {
                        when (obs.type) {
                            ObstacleType.ASTEROID -> {
                                // Pensée Parasite / Toxine Cérébrale & Obstacles simples de type ASTEROID (Coercition, Voie de fait, Spoliation, Simulacre, Mystification, Aliénation, Infantilisation, Ostracisme, Bâillonnement)
                                val isVulnerable = gameState.antiAsteroidTimer > 0f
                                val variant = obs.simpleVariant
                                val isAdvancedVariant = variant != null && variant.unlockLevel >= 10
                                val toxinGlow = when {
                                    isVulnerable -> EmeraldNovaGlow
                                    isAdvancedVariant -> variant.category.accentColor
                                    else -> Color(0xFFA855F7)
                                }
                                val toxinCore = if (isAdvancedVariant) variant.category.coreColor else Color(0xFF2E1065)
                                val sporeColor = if (isAdvancedVariant) variant.category.accentColor else Color(0xFF84CC16)
                                val pulse = 1f + 0.12f * sin(obs.pulsePhase * 1.4f)

                                // Miasme toxique externe
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            toxinGlow.copy(alpha = 0.42f),
                                            sporeColor.copy(alpha = 0.14f),
                                            Color.Transparent
                                        ),
                                        center = Offset(ox, oy),
                                        radius = oRadius * 1.85f * pulse
                                    ),
                                    radius = oRadius * 1.85f * pulse,
                                    center = Offset(ox, oy)
                                )

                                rotate(degrees = obs.rotation * 0.65f, pivot = Offset(ox, oy)) {
                                    // 6 lobes organiques de l'obstacle
                                    val lobeCount = 6
                                    for (i in 0 until lobeCount) {
                                        val ang = (i.toFloat() / lobeCount) * (Math.PI.toFloat() * 2f)
                                        val lobeWobble = 1f + 0.16f * sin(obs.pulsePhase * 2f + i * 1.3f)
                                        val dist = oRadius * 0.46f * lobeWobble
                                        val lx = ox + cos(ang) * dist
                                        val ly = oy + sin(ang) * dist
                                        val lobeR = oRadius * (if (i % 2 == 0) 0.66f else 0.54f)

                                        drawCircle(
                                            color = toxinCore,
                                            radius = lobeR,
                                            center = Offset(lx, ly)
                                        )
                                        drawCircle(
                                            color = if (i % 2 == 0) toxinGlow else sporeColor.copy(alpha = 0.75f),
                                            radius = lobeR,
                                            center = Offset(lx, ly),
                                            style = Stroke(width = 1.8.dp.toPx())
                                        )
                                        // Veine reliant au noyau
                                        drawLine(
                                            color = sporeColor.copy(alpha = 0.65f),
                                            start = Offset(ox, oy),
                                            end = Offset(lx, ly),
                                            strokeWidth = 1.6.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }

                                // Noyau sombre
                                drawCircle(
                                    color = toxinCore,
                                    radius = oRadius * 0.48f,
                                    center = Offset(ox, oy)
                                )
                                if (isAdvancedVariant) {
                                    val vEmoji = textMeasurer.measure(
                                        text = variant.emoji,
                                        style = TextStyle(fontSize = 11.sp)
                                    )
                                    drawText(
                                        textLayoutResult = vEmoji,
                                        topLeft = Offset(
                                            ox - vEmoji.size.width / 2f,
                                            oy - vEmoji.size.height / 2f
                                        )
                                    )
                                } else {
                                    drawCircle(
                                        color = sporeColor,
                                        radius = oRadius * 0.22f * pulse,
                                        center = Offset(ox, oy)
                                    )
                                }

                                if (isVulnerable) {
                                    drawCircle(
                                        color = EmeraldNovaGlow,
                                        radius = oRadius * 1.38f,
                                        center = Offset(ox, oy),
                                        style = Stroke(
                                            width = 2.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), phase = obs.pulsePhase * 20f)
                                        )
                                    )
                                }

                                val obsTag = textMeasurer.measure(
                                    text = if (isVulnerable) "🌿 DISSIPER • ${obs.effectiveShortTag}" else obs.effectiveShortTag,
                                    style = TextStyle(
                                        color = when {
                                            isVulnerable -> EmeraldNovaGlow
                                            isAdvancedVariant -> variant.category.accentColor
                                            else -> Color(0xFFD8B4FE)
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                drawText(
                                    textLayoutResult = obsTag,
                                    topLeft = Offset(
                                        ox - obsTag.size.width / 2f,
                                        oy + oRadius * 1.22f
                                    )
                                )
                            }

                            ObstacleType.LASER_DRONE -> {
                                // Foyer d'Angoisse / Pic de Stress & Obstacles simples de type LASER_DRONE (Lésion, Abus de faiblesse, Fallacie, Subversion, Duplicité, Impuissance apprise, Emprise, Arbitraire, Lois d'exception)
                                val isVulnerable = gameState.antiDroneTimer > 0f
                                val variant = obs.simpleVariant
                                val isAdvancedVariant = variant != null && variant.unlockLevel >= 10
                                val stressColor = when {
                                    isVulnerable -> CyberCyanGlow
                                    isAdvancedVariant -> variant.category.accentColor
                                    else -> DangerRed
                                }
                                val beamColor = when {
                                    isVulnerable -> CyberCyan
                                    isAdvancedVariant -> variant.category.accentColor
                                    else -> Color(0xFFF43F5E)
                                }
                                val coreBg = if (isAdvancedVariant) variant.category.coreColor else Color(0xFF4C0519)
                                val pulse = 1f + 0.18f * sin(obs.pulsePhase * 2.4f)

                                // Halo de tension nerveuse
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            beamColor.copy(alpha = 0.45f),
                                            coreBg.copy(alpha = 0.22f),
                                            Color.Transparent
                                        ),
                                        center = Offset(ox, oy),
                                        radius = oRadius * 1.9f * pulse
                                    ),
                                    radius = oRadius * 1.9f * pulse,
                                    center = Offset(ox, oy)
                                )

                                // Faisceaux croisés et Pics de Stress en rotation
                                rotate(degrees = obs.rotation, pivot = Offset(ox, oy)) {
                                    // 2 longs faisceaux traqueurs
                                    drawLine(
                                        color = beamColor.copy(alpha = 0.85f),
                                        start = Offset(ox - oRadius * 1.55f, oy),
                                        end = Offset(ox + oRadius * 1.55f, oy),
                                        strokeWidth = 3.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )
                                    drawLine(
                                        color = beamColor.copy(alpha = 0.85f),
                                        start = Offset(ox, oy - oRadius * 1.55f),
                                        end = Offset(ox, oy + oRadius * 1.55f),
                                        strokeWidth = 3.5.dp.toPx(),
                                        cap = StrokeCap.Round
                                    )

                                    // Étoile à 8 aiguilles (Pics de Stress)
                                    val spikePath = Path().apply {
                                        val points = 16
                                        for (i in 0 until points) {
                                            val ang = (i.toFloat() / points) * (Math.PI.toFloat() * 2f)
                                            val r = if (i % 2 == 0) oRadius * 1.22f * pulse else oRadius * 0.42f
                                            val sx = ox + cos(ang) * r
                                            val sy = oy + sin(ang) * r
                                            if (i == 0) moveTo(sx, sy) else lineTo(sx, sy)
                                        }
                                        close()
                                    }
                                    drawPath(path = spikePath, color = coreBg)
                                    drawPath(
                                        path = spikePath,
                                        color = stressColor,
                                        style = Stroke(width = 1.8.dp.toPx())
                                    )
                                }

                                // Pupille centrale
                                drawCircle(
                                    color = coreBg,
                                    radius = oRadius * 0.46f,
                                    center = Offset(ox, oy)
                                )
                                if (isAdvancedVariant) {
                                    val vEmoji = textMeasurer.measure(
                                        text = variant.emoji,
                                        style = TextStyle(fontSize = 11.sp)
                                    )
                                    drawText(
                                        textLayoutResult = vEmoji,
                                        topLeft = Offset(
                                            ox - vEmoji.size.width / 2f,
                                            oy - vEmoji.size.height / 2f
                                        )
                                    )
                                } else {
                                    drawCircle(
                                        color = Color.White,
                                        radius = oRadius * 0.18f,
                                        center = Offset(ox, oy)
                                    )
                                }

                                val obsTag = textMeasurer.measure(
                                    text = if (isVulnerable) "💠 APAISER • ${obs.effectiveShortTag}" else obs.effectiveShortTag,
                                    style = TextStyle(
                                        color = when {
                                            isVulnerable -> CyberCyanGlow
                                            isAdvancedVariant -> variant.category.accentColor
                                            else -> Color(0xFFFDA4AF)
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                drawText(
                                    textLayoutResult = obsTag,
                                    topLeft = Offset(
                                        ox - obsTag.size.width / 2f,
                                        oy + oRadius * 1.32f
                                    )
                                )
                            }

                            ObstacleType.PULSAR_MINE -> {
                                // Crise de Panique / Décharge Émotive & Obstacles simples de type PULSAR_MINE (Captation, Subornation, Novlangue, Fabulation, Subterfuge, Conditionnement, Asservissement, Inquisition)
                                val isVulnerable = gameState.antiMineTimer > 0f
                                val variant = obs.simpleVariant
                                val isAdvancedVariant = variant != null && variant.unlockLevel >= 10
                                val panicGlow = when {
                                    isVulnerable -> CyberCyanGlow
                                    isAdvancedVariant -> variant.category.accentColor
                                    else -> DangerRed
                                }
                                val dischargeColor = when {
                                    isVulnerable -> EmeraldNovaGlow
                                    isAdvancedVariant -> variant.category.accentColor
                                    else -> SolarGoldGlow
                                }
                                val pulseScale = 1f + 0.25f * sin(obs.pulsePhase * 1.6f)
                                val outerWave = 1f + 0.35f * ((obs.pulsePhase * 0.8f) % 1f)

                                // Onde de choc externe (Décharge Émotive)
                                drawCircle(
                                    color = panicGlow.copy(alpha = 0.26f),
                                    radius = oRadius * 1.65f * pulseScale,
                                    center = Offset(ox, oy)
                                )
                                drawCircle(
                                    color = dischargeColor.copy(alpha = 0.65f),
                                    radius = oRadius * 1.35f * outerWave,
                                    center = Offset(ox, oy),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), phase = obs.pulsePhase * 25f)
                                    )
                                )
                                drawCircle(
                                    color = panicGlow,
                                    radius = oRadius * pulseScale,
                                    center = Offset(ox, oy),
                                    style = Stroke(width = 2.8.dp.toPx())
                                )

                                // 8 éclairs émotionnels radiaux
                                for (arc in 0 until 8) {
                                    val ang = (arc * 45f + obs.rotation * 1.4f) * (Math.PI.toFloat() / 180f)
                                    val innerR = oRadius * 0.45f
                                    val outerR = oRadius * (1.15f + 0.2f * sin(obs.pulsePhase * 3f + arc))
                                    val midR = (innerR + outerR) * 0.5f
                                    val jitter = if (arc % 2 == 0) 0.16f else -0.16f
                                    val arcPath = Path().apply {
                                        moveTo(ox + cos(ang) * innerR, oy + sin(ang) * innerR)
                                        lineTo(ox + cos(ang + jitter) * midR, oy + sin(ang + jitter) * midR)
                                        lineTo(ox + cos(ang) * outerR, oy + sin(ang) * outerR)
                                    }
                                    drawPath(
                                        path = arcPath,
                                        color = if (arc % 2 == 0) dischargeColor else panicGlow,
                                        style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                                    )
                                }

                                // Cœur instable
                                drawCircle(
                                    color = if (isAdvancedVariant) variant.category.coreColor else panicGlow,
                                    radius = oRadius * 0.52f * pulseScale,
                                    center = Offset(ox, oy)
                                )
                                if (isAdvancedVariant) {
                                    val vEmoji = textMeasurer.measure(
                                        text = variant.emoji,
                                        style = TextStyle(fontSize = 11.sp)
                                    )
                                    drawText(
                                        textLayoutResult = vEmoji,
                                        topLeft = Offset(
                                            ox - vEmoji.size.width / 2f,
                                            oy - vEmoji.size.height / 2f
                                        )
                                    )
                                } else {
                                    drawCircle(
                                        color = Color.White,
                                        radius = oRadius * 0.24f,
                                        center = Offset(ox, oy)
                                    )
                                }

                                val obsTag = textMeasurer.measure(
                                    text = if (isVulnerable) "🌊 CALMER • ${obs.effectiveShortTag}" else obs.effectiveShortTag,
                                    style = TextStyle(
                                        color = when {
                                            isVulnerable -> CyberCyanGlow
                                            isAdvancedVariant -> variant.category.accentColor
                                            else -> SolarGoldGlow
                                        },
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                drawText(
                                    textLayoutResult = obsTag,
                                    topLeft = Offset(
                                        ox - obsTag.size.width / 2f,
                                        oy + oRadius * 1.38f
                                    )
                                )
                            }

                            ObstacleType.CONFORMISM_SWARM -> {
                                // 1. Conformisme (L'esprit "Mouton") : La Nuée Grégaire
                                // Essaim compact (3x3 comme avant) entouré d'une grande zone d'action rectangulaire en pointillé
                                val dullGreyCore = Color(0xFF475569)
                                val dullGreyRim = Color(0xFF94A3B8)
                                val dullField = Color(0xFF334155)
                                val actionHalfW = oRadius * 2.65f
                                val actionHalfH = oRadius * 1.95f

                                // Grande zone d'action rectangulaire en pointillé autour de la Nuée Grégaire
                                drawRoundRect(
                                    color = dullField.copy(alpha = 0.20f),
                                    topLeft = Offset(ox - actionHalfW, oy - actionHalfH),
                                    size = Size(actionHalfW * 2f, actionHalfH * 2f),
                                    cornerRadius = CornerRadius(14.dp.toPx())
                                )
                                drawRoundRect(
                                    color = Color(0xFFCBD5E1).copy(alpha = 0.78f),
                                    topLeft = Offset(ox - actionHalfW, oy - actionHalfH),
                                    size = Size(actionHalfW * 2f, actionHalfH * 2f),
                                    cornerRadius = CornerRadius(14.dp.toPx()),
                                    style = Stroke(
                                        width = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase = obs.pulsePhase * 16f)
                                    )
                                )

                                // Petit cadre interne de l'essaim (comme avant)
                                drawRoundRect(
                                    color = dullField.copy(alpha = 0.36f),
                                    topLeft = Offset(ox - oRadius * 1.25f, oy - oRadius * 0.95f),
                                    size = Size(oRadius * 2.5f, oRadius * 1.9f),
                                    cornerRadius = CornerRadius(12.dp.toPx())
                                )
                                drawRoundRect(
                                    color = dullGreyRim.copy(alpha = 0.55f),
                                    topLeft = Offset(ox - oRadius * 1.25f, oy - oRadius * 0.95f),
                                    size = Size(oRadius * 2.5f, oRadius * 1.9f),
                                    cornerRadius = CornerRadius(12.dp.toPx()),
                                    style = Stroke(
                                        width = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), phase = obs.pulsePhase * 12f)
                                    )
                                )

                                // Rangs serrés 3 x 3 d'entités grises parfaitement identiques (taille d'origine)
                                val unitR = oRadius * 0.22f
                                val stepX = oRadius * 0.68f
                                val stepY = oRadius * 0.52f
                                for (row in -1..1) {
                                    for (col in -1..1) {
                                        val ux = ox + col * stepX
                                        val uy = oy + row * stepY
                                        drawCircle(
                                            color = dullGreyCore,
                                            radius = unitR,
                                            center = Offset(ux, uy)
                                        )
                                        drawCircle(
                                            color = dullGreyRim,
                                            radius = unitR,
                                            center = Offset(ux, uy),
                                            style = Stroke(width = 1.5.dp.toPx())
                                        )
                                        // Petits yeux ternes uniformes
                                        drawCircle(
                                            color = Color(0xFFCBD5E1),
                                            radius = unitR * 0.24f,
                                            center = Offset(ux - unitR * 0.28f, uy - unitR * 0.1f)
                                        )
                                        drawCircle(
                                            color = Color(0xFFCBD5E1),
                                            radius = unitR * 0.24f,
                                            center = Offset(ux + unitR * 0.28f, uy - unitR * 0.1f)
                                        )
                                    }
                                }

                                val obsTag = textMeasurer.measure(
                                    text = "🐑 Nuée Grégaire (Anti-Bonus)",
                                    style = TextStyle(
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                drawText(
                                    textLayoutResult = obsTag,
                                    topLeft = Offset(
                                        ox - obsTag.size.width / 2f,
                                        oy + oRadius * 1.05f
                                    )
                                )
                            }

                            ObstacleType.DOGMATIC_PRISM -> {
                                // 2. Endoctrinement : Le Prisme Dogmatique
                                // Structure géométrique froide, rigide et inébranlable (monolithe aux arêtes tranchantes)
                                val steelCold = Color(0xFF38BDF8)
                                val prismDeep = Color(0xFF0F172A)
                                val prismEdge = Color(0xFFE0F2FE)
                                val isNearbyForVirtue = gameState.isObstacleVisibleAndNearbyForVirtue(obs)

                                if (isNearbyForVirtue) {
                                    drawLine(
                                        color = VioletPulsarGlow.copy(alpha = 0.75f),
                                        start = Offset(playerScreenX, playerScreenY),
                                        end = Offset(ox, oy),
                                        strokeWidth = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), phase = -gameState.elapsedTime * 40f)
                                    )
                                }

                                rotate(degrees = obs.rotation, pivot = Offset(ox, oy)) {
                                    // Monolithe hexagonal aux arêtes tranchantes
                                    val prismPath = Path().apply {
                                        val vertices = 6
                                        for (v in 0 until vertices) {
                                            val ang = (v * 60f) * (Math.PI.toFloat() / 180f)
                                            val r = if (v % 2 == 0) oRadius * 1.25f else oRadius * 0.95f
                                            val vxPt = ox + cos(ang) * r
                                            val vyPt = oy + sin(ang) * r
                                            if (v == 0) moveTo(vxPt, vyPt) else lineTo(vxPt, vyPt)
                                        }
                                        close()
                                    }
                                    drawPath(path = prismPath, color = prismDeep)
                                    drawPath(
                                        path = prismPath,
                                        color = prismEdge,
                                        style = Stroke(width = 2.6.dp.toPx())
                                    )

                                    // Arêtes internes cristallines rigides
                                    for (v in 0 until 6) {
                                        val ang = (v * 60f) * (Math.PI.toFloat() / 180f)
                                        val r = if (v % 2 == 0) oRadius * 1.25f else oRadius * 0.95f
                                        drawLine(
                                            color = steelCold.copy(alpha = 0.75f),
                                            start = Offset(ox, oy),
                                            end = Offset(ox + cos(ang) * r, oy + sin(ang) * r),
                                            strokeWidth = 1.5.dp.toPx()
                                        )
                                    }
                                }

                                drawCircle(
                                    color = steelCold,
                                    radius = oRadius * 0.24f,
                                    center = Offset(ox, oy)
                                )

                                val obsTag = textMeasurer.measure(
                                    text = if (isNearbyForVirtue) "💎 Cliquez Sphère → 🔍 Vérité" else "💎 Prisme Dogmatique",
                                    style = TextStyle(
                                        color = if (isNearbyForVirtue) VioletPulsarGlow else prismEdge,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                drawText(
                                    textLayoutResult = obsTag,
                                    topLeft = Offset(
                                        ox - obsTag.size.width / 2f,
                                        oy + oRadius * 1.32f
                                    )
                                )
                            }

                            ObstacleType.OBSCURANTISM_VEIL -> {
                                // 3. L'Ignorance : Le Voile d'Obscurantisme
                                // Nébuleuse d'encre noire / brouillard statique très dense qui brouille les capteurs et cache les Pensées Pures
                                val inkBlack = Color(0xFF020617)
                                val fogEdge = Color(0xFF334155)
                                val isNearbyForVirtue = gameState.isObstacleVisibleAndNearbyForVirtue(obs)

                                if (isNearbyForVirtue) {
                                    drawLine(
                                        color = SolarGoldGlow.copy(alpha = 0.72f),
                                        start = Offset(playerScreenX, playerScreenY),
                                        end = Offset(ox, oy),
                                        strokeWidth = 2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), phase = -gameState.elapsedTime * 40f)
                                    )
                                }

                                // Nuage d'encre noire multi-couches très dense
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            inkBlack.copy(alpha = 0.96f),
                                            Color(0xFF090D16).copy(alpha = 0.92f),
                                            Color(0xFF1E293B).copy(alpha = 0.65f),
                                            Color.Transparent
                                        ),
                                        center = Offset(ox, oy),
                                        radius = oRadius * 1.25f
                                    ),
                                    radius = oRadius * 1.25f,
                                    center = Offset(ox, oy)
                                )

                                // Brouillard statique (grains d'interférence)
                                for (s in 0 until 10) {
                                    val ang = (s * 36f + obs.pulsePhase * 18f) * (Math.PI.toFloat() / 180f)
                                    val dist = oRadius * (0.25f + (s % 4) * 0.20f)
                                    val sx = ox + cos(ang) * dist
                                    val sy = oy + sin(ang * 1.3f) * dist
                                    drawCircle(
                                        color = fogEdge.copy(alpha = 0.55f),
                                        radius = (3 + (s % 3)).dp.toPx(),
                                        center = Offset(sx, sy)
                                    )
                                }

                                drawCircle(
                                    color = if (isNearbyForVirtue) SolarGoldGlow.copy(alpha = 0.75f) else fogEdge.copy(alpha = 0.5f),
                                    radius = oRadius * 0.95f,
                                    center = Offset(ox, oy),
                                    style = Stroke(
                                        width = 1.8.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f), phase = obs.pulsePhase * 15f)
                                    )
                                )

                                val obsTag = textMeasurer.measure(
                                    text = if (isNearbyForVirtue) "🌑 Cliquez Sphère → Tirer Vertu" else "🌑 Voile d'Obscurantisme",
                                    style = TextStyle(
                                        color = if (isNearbyForVirtue) SolarGoldGlow else Color(0xFF94A3B8),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                drawText(
                                    textLayoutResult = obsTag,
                                    topLeft = Offset(
                                        ox - obsTag.size.width / 2f,
                                        oy - obsTag.size.height / 2f
                                    )
                                )
                            }

                            ObstacleType.MENTAL_MIRE -> {
                                // 4. Incapacité : Le Bourbier Mental
                                // Champ de force poisseux / anomalie gravitationnelle visqueuse aux teintes grisâtres (Vitesse ÷ 3)
                                val mireGrey = Color(0xFF64748B)
                                val mireDark = Color(0xFF1E293B)
                                val pulse = 1f + 0.08f * sin(obs.pulsePhase * 1.5f)

                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            mireDark.copy(alpha = 0.85f),
                                            mireGrey.copy(alpha = 0.45f),
                                            Color.Transparent
                                        ),
                                        center = Offset(ox, oy),
                                        radius = oRadius * 1.15f * pulse
                                    ),
                                    radius = oRadius * 1.15f * pulse,
                                    center = Offset(ox, oy)
                                )

                                // Anneaux concentriques visqueux
                                for (ring in 1..3) {
                                    val ringR = oRadius * (ring * 0.30f) * pulse
                                    drawCircle(
                                        color = Color(0xFF94A3B8).copy(alpha = 0.55f),
                                        radius = ringR,
                                        center = Offset(ox, oy),
                                        style = Stroke(
                                            width = 2.2.dp.toPx(),
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase = -obs.pulsePhase * 18f * ring)
                                        )
                                    )
                                }

                                val obsTag = textMeasurer.measure(
                                    text = "🕸️ Bourbier Mental (Vitesse ÷3)",
                                    style = TextStyle(
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                                drawText(
                                    textLayoutResult = obsTag,
                                    topLeft = Offset(
                                        ox - obsTag.size.width / 2f,
                                        oy + oRadius * 0.95f
                                    )
                                )
                            }

                            ObstacleType.NARCISSUS_MIRROR -> {
                                // 5. L'Ego : Le Miroir de Narcisse
                                // Astre artificiellement brillant et scintillant qui imite l'apparence des bonus ou des cœurs (Aimant inversé)
                                val pulse = 1f + 0.22f * sin(obs.pulsePhase * 3.2f)
                                val pullWave = 1f - ((obs.pulsePhase * 0.6f) % 1f)

                                // Ondes d'attraction inversées (Aimant Cosmique inversé)
                                drawCircle(
                                    color = PlasmaRose.copy(alpha = 0.42f * (1f - pullWave)),
                                    radius = oRadius * (1.2f + pullWave * 2.2f),
                                    center = Offset(ox, oy),
                                    style = Stroke(
                                        width = 1.8.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), phase = 0f)
                                    )
                                )

                                // Halo doré et rose trompeur (imite un cœur / bonus)
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.95f),
                                            SolarGoldGlow.copy(alpha = 0.70f),
                                            PlasmaRose.copy(alpha = 0.35f),
                                            Color.Transparent
                                        ),
                                        center = Offset(ox, oy),
                                        radius = oRadius * 2.3f * pulse
                                    ),
                                    radius = oRadius * 2.3f * pulse,
                                    center = Offset(ox, oy)
                                )

                                // Rayons scintillants artificiels
                                rotate(degrees = obs.rotation * 1.2f, pivot = Offset(ox, oy)) {
                                    for (ray in 0 until 8) {
                                        val ang = (ray * 45f) * (Math.PI.toFloat() / 180f)
                                        val len = if (ray % 2 == 0) oRadius * 1.65f * pulse else oRadius * 1.05f
                                        drawLine(
                                            color = if (ray % 2 == 0) SolarGoldGlow else PlasmaRose,
                                            start = Offset(ox - cos(ang) * len, oy - sin(ang) * len),
                                            end = Offset(ox + cos(ang) * len, oy + sin(ang) * len),
                                            strokeWidth = 2.2.dp.toPx(),
                                            cap = StrokeCap.Round
                                        )
                                    }
                                }

                                drawCircle(
                                    color = PlasmaRose,
                                    radius = oRadius * 0.82f,
                                    center = Offset(ox, oy)
                                )
                                drawCircle(
                                    color = SolarGoldGlow,
                                    radius = oRadius * 0.82f,
                                    center = Offset(ox, oy),
                                    style = Stroke(width = 2.dp.toPx())
                                )

                                val lureEmoji = textMeasurer.measure(
                                    text = "💖",
                                    style = TextStyle(fontSize = 13.sp)
                                )
                                drawText(
                                    textLayoutResult = lureEmoji,
                                    topLeft = Offset(
                                        ox - lureEmoji.size.width / 2f,
                                        oy - lureEmoji.size.height / 2f
                                    )
                                )

                                val obsTag = textMeasurer.measure(
                                    text = "🪞 Miroir de Narcisse",
                                    style = TextStyle(
                                        color = SolarGoldGlow,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                drawText(
                                    textLayoutResult = obsTag,
                                    topLeft = Offset(
                                        ox - obsTag.size.width / 2f,
                                        oy + oRadius * 1.32f
                                    )
                                )
                            }
                        }

                        // Indicateur de stase / court-circuit si l'obstacle est figé (Souffle Vagal N.30 ou Influx Bioélectrique N.100)
                        if (obs.paralyzedTimer > 0f) {
                            drawCircle(
                                color = Color(0xFF38BDF8).copy(alpha = 0.85f),
                                radius = oRadius * 1.45f,
                                center = Offset(ox, oy),
                                style = Stroke(
                                    width = 2.2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), phase = gameState.elapsedTime * 40f)
                                )
                            )
                            val stasisBadge = textMeasurer.measure(
                                text = "⚡ FIGÉ (${obs.paralyzedTimer.toInt() + 1}s)",
                                style = TextStyle(
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            drawText(
                                textLayoutResult = stasisBadge,
                                topLeft = Offset(
                                    ox - stasisBadge.size.width / 2f,
                                    oy - oRadius * 1.65f - stasisBadge.size.height
                                )
                            )
                        } else if (obs.myelinSlowTimer > 0f) {
                            drawCircle(
                                color = Color(0xFF2DD4BF).copy(alpha = 0.82f),
                                radius = oRadius * 1.38f,
                                center = Offset(ox, oy),
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), phase = gameState.elapsedTime * 22f)
                                )
                            )
                            val myelinBadge = textMeasurer.measure(
                                text = "🧬 -65% (${obs.myelinSlowTimer.toInt() + 1}s)",
                                style = TextStyle(
                                    color = Color(0xFF99F6E4),
                                    fontSize = 7.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            drawText(
                                textLayoutResult = myelinBadge,
                                topLeft = Offset(
                                    ox - myelinBadge.size.width / 2f,
                                    oy - oRadius * 1.55f - myelinBadge.size.height
                                )
                            )
                        }
                    }
                }

                // Draw Burst Particles in Camera Space
                for (p in gameState.particles) {
                    val sx = (p.x - camX) * w
                    val sy = (p.y - camY) * h
                    drawCircle(
                        color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                        radius = (4.dp.toPx() * p.life),
                        center = Offset(sx, sy)
                    )
                }

                // État physique et émotionnel de L'Étincelle de l'Esprit
                val px = (gameState.playerX - camX) * w
                val py = (gameState.playerY - camY) * h
                val pRadius = minDim * 0.042f
                val isBlinking = gameState.invulnerableTimer > 0f &&
                    ((gameState.invulnerableTimer * 10).toInt() % 2 == 0)

                val isHyperPropulsion = gameState.speedBoostTimer > 0f || gameState.elanDashTimer > 0f
                val isInMentalMire = gameState.homeostasisFieldTimer <= 0f && gameState.obstacles.any { obs ->
                    obs.type == ObstacleType.MENTAL_MIRE &&
                        hypot(obs.x - gameState.playerX, obs.y - gameState.playerY) <= (obs.radius + 0.042f)
                }
                val isMentallyAltered = isInMentalMire ||
                    (gameState.conformismSlowTimer > 0f && gameState.homeostasisFieldTimer <= 0f)

                val vx = gameState.playerVx
                val vy = gameState.playerVy
                val speedMag = hypot(vx, vy)
                val moveAngleRad = if (speedMag > 0.008f) atan2(vy, vx) else 0f
                val moveAngleDeg = (moveAngleRad * (180f / Math.PI.toFloat()))

                // Altération mentale (Bourbier Mental / Conformisme) ou Déphasage Contemplatif (Intangibilité Thêta N.120)
                val baseFlicker = if (isMentallyAltered) {
                    0.72f + 0.28f * sin(gameState.elapsedTime * 27f) * cos(gameState.elapsedTime * 19f)
                } else {
                    1f
                }
                val oxygenFlicker = if (gameState.phaseShiftTimer > 0f) {
                    (0.46f + 0.16f * sin(gameState.elapsedTime * 7f)) * baseFlicker
                } else {
                    baseFlicker
                }
                val mentalContraction = if (isInMentalMire) {
                    0.60f * baseFlicker
                } else if (isMentallyAltered) {
                    0.76f * baseFlicker
                } else {
                    1f
                }

                // 1. Le Sillage Spirituel & Lueur Arrière Courbée d'Hyper-Propulsion
                val trailSize = gameState.trail.size

                // En mode Hyper-Propulsion (ou Élan Vital), la lueur arrière suit la courbe réelle du virage (gauche / droite)
                // le long de la trajectoire du sillage au lieu de former une barre rigide et droite.
                if (isHyperPropulsion && trailSize >= 2) {
                    val maxPropulsionNodes = minOf(trailSize, 12)
                    var prevCurveX = px
                    var prevCurveY = py
                    for (idx in 0 until maxPropulsionNodes) {
                        val pt = gameState.trail[idx]
                        val tx = (pt.x - camX) * w
                        val ty = (pt.y - camY) * h
                        if (hypot(tx - prevCurveX, ty - prevCurveY) < minDim * 0.25f) {
                            val curveProgress = 1f - (idx.toFloat() / maxPropulsionNodes.toFloat())
                            val subSteps = 3
                            for (step in 1..subSteps) {
                                val t = step.toFloat() / subSteps.toFloat()
                                val interpX = prevCurveX + (tx - prevCurveX) * t
                                val interpY = prevCurveY + (ty - prevCurveY) * t
                                val localProg = (curveProgress - (t / maxPropulsionNodes.toFloat())).coerceIn(0f, 1f)
                                val blazeNodeRadius = pRadius * (0.65f + 1.85f * localProg) * mentalContraction
                                val blazeAlpha = (localProg * localProg * 0.52f * oxygenFlicker).coerceIn(0f, 1f)

                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = blazeAlpha * 0.85f),
                                            SolarGoldGlow.copy(alpha = blazeAlpha * 0.80f),
                                            CoralHyperGlow.copy(alpha = blazeAlpha * 0.45f),
                                            Color.Transparent
                                        ),
                                        center = Offset(interpX, interpY),
                                        radius = blazeNodeRadius
                                    ),
                                    radius = blazeNodeRadius,
                                    center = Offset(interpX, interpY)
                                )
                            }
                            prevCurveX = tx
                            prevCurveY = ty
                        } else {
                            break
                        }
                    }
                }

                val isMyelinSheathActive = gameState.myelinSheathTimer > 0f
                for ((idx, pt) in gameState.trail.withIndex()) {
                    val progress = 1f - (idx.toFloat() / trailSize.coerceAtLeast(1))
                    val tx = (pt.x - camX) * w
                    val ty = (pt.y - camY) * h
                    val wispDriftX = sin(gameState.elapsedTime * 3.5f + idx * 0.7f) * minDim * 0.004f * (1f - progress)
                    val wispDriftY = cos(gameState.elapsedTime * 3.1f + idx * 0.7f) * minDim * 0.004f * (1f - progress)
                    val cx = tx + wispDriftX
                    val cy = ty + wispDriftY

                    val hyperTrailBoost = when {
                        isMyelinSheathActive -> 1.55f
                        isHyperPropulsion -> 1.45f
                        else -> 1f
                    }
                    val wispRadius = pRadius * (0.35f + 1.25f * progress) * mentalContraction * hyperTrailBoost
                    val wispAlpha = (pt.alpha * progress * (if (isHyperPropulsion || isMyelinSheathActive) 0.76f else 0.50f) * oxygenFlicker).coerceIn(0f, 1f)

                    val trailCoreColor = when {
                        isMyelinSheathActive -> Color(0xFF5EEAD4)
                        isHyperPropulsion -> lerp(sphereColor.glowColor, SolarGoldGlow, 0.55f)
                        else -> sphereColor.glowColor
                    }
                    val trailOuterColor = when {
                        isMyelinSheathActive -> Color(0xFF0D9488)
                        isHyperPropulsion -> CoralHyperGlow
                        else -> sphereColor.primaryColor
                    }

                    // Ruban fluide reliant les points consécutifs du sillage (sauf en cas de saut toroïdal)
                    if (idx < trailSize - 1) {
                        val nextPt = gameState.trail[idx + 1]
                        val ntx = (nextPt.x - camX) * w
                        val nty = (nextPt.y - camY) * h
                        if (hypot(tx - ntx, ty - nty) < minDim * 0.25f) {
                            drawLine(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        trailCoreColor.copy(alpha = wispAlpha * 0.65f),
                                        trailOuterColor.copy(alpha = wispAlpha * 0.22f)
                                    ),
                                    start = Offset(cx, cy),
                                    end = Offset(ntx, nty)
                                ),
                                start = Offset(cx, cy),
                                end = Offset(ntx, nty),
                                strokeWidth = wispRadius * 1.15f,
                                cap = StrokeCap.Round
                            )
                            if (isMyelinSheathActive) {
                                drawLine(
                                    color = Color(0xFF99F6E4).copy(alpha = wispAlpha * 0.70f),
                                    start = Offset(cx, cy),
                                    end = Offset(ntx, nty),
                                    strokeWidth = 2.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    // Nébuleuse vaporeuse de chaque point du sillage
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = wispAlpha * 0.55f),
                                trailCoreColor.copy(alpha = wispAlpha * 0.75f),
                                trailOuterColor.copy(alpha = wispAlpha * 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = wispRadius * 1.4f
                        ),
                        radius = wispRadius * 1.4f,
                        center = Offset(cx, cy)
                    )
                }

                // Auras des bonus et états actifs autour de L'Étincelle de l'Esprit
                if (gameState.invulnerableTimer > 0f) {
                    drawCircle(
                        color = CyberCyanGlow.copy(alpha = 0.75f),
                        radius = pRadius * 1.35f,
                        center = Offset(px, py),
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                }

                // Magnet Magnetic Waves Aura
                if (gameState.magnetTimer > 0f) {
                    val pulse = (gameState.elapsedTime * 3.5f) % 1f
                    drawCircle(
                        color = VioletPulsarGlow.copy(alpha = (1f - pulse) * 0.70f),
                        radius = pRadius * (1.25f + pulse * 1.5f),
                        center = Offset(px, py),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Anti-Asteroid Spiked Crystal Ring
                if (gameState.antiAsteroidTimer > 0f) {
                    val angle = gameState.elapsedTime * 90f
                    rotate(degrees = angle, pivot = Offset(px, py)) {
                        drawCircle(
                            color = EmeraldNovaGlow.copy(alpha = 0.85f),
                            radius = pRadius * 1.6f,
                            center = Offset(px, py),
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), phase = 0f)
                            )
                        )
                    }
                }

                // Anti-Drone Lightning Arc Ring
                if (gameState.antiDroneTimer > 0f) {
                    val angle = -gameState.elapsedTime * 120f
                    rotate(degrees = angle, pivot = Offset(px, py)) {
                        drawCircle(
                            color = CyberCyanGlow.copy(alpha = 0.85f),
                            radius = pRadius * 1.55f,
                            center = Offset(px, py),
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 6f), phase = 0f)
                            )
                        )
                    }
                }

                // Anti-Mine Defusal Field
                if (gameState.antiMineTimer > 0f) {
                    val pulse = 1f + 0.15f * sin(gameState.elapsedTime * 6f)
                    drawCircle(
                        color = CoralHyperGlow.copy(alpha = 0.80f),
                        radius = pRadius * 1.5f * pulse,
                        center = Offset(px, py),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Aura de Paix (Anneaux d'or lumineux et apaisants gravitant lentement comme un bouclier céleste)
                if (gameState.peaceAuraTimer > 0f || gameState.activeAnchoredThought != null) {
                    val auraPulse = 1f + 0.10f * sin(gameState.elapsedTime * 2.4f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                SolarGoldGlow.copy(alpha = 0.42f),
                                SolarGold.copy(alpha = 0.18f),
                                CyberCyanGlow.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = Offset(px, py),
                            radius = pRadius * 2.85f * auraPulse
                        ),
                        radius = pRadius * 2.85f * auraPulse,
                        center = Offset(px, py)
                    )
                    // Premier anneau céleste d'or (gravitation lente horaire)
                    val outerOrbitRot = gameState.elapsedTime * 28f
                    rotate(degrees = outerOrbitRot, pivot = Offset(px, py)) {
                        drawCircle(
                            color = SolarGoldGlow.copy(alpha = 0.85f),
                            radius = pRadius * 2.15f * auraPulse,
                            center = Offset(px, py),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(22f, 12f), phase = 0f)
                            )
                        )
                    }
                    // Second anneau céleste d'or (gravitation lente anti-horaire avec perles de lumière)
                    val innerOrbitRot = -gameState.elapsedTime * 19f
                    rotate(degrees = innerOrbitRot, pivot = Offset(px, py)) {
                        val ringR = pRadius * 1.62f * auraPulse
                        drawCircle(
                            color = Color(0xFFFDE047).copy(alpha = 0.75f),
                            radius = ringR,
                            center = Offset(px, py),
                            style = Stroke(
                                width = 1.6.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), phase = 6f)
                            )
                        )
                        for (orb in 0 until 4) {
                            val orbAng = (orb * 90f) * (Math.PI.toFloat() / 180f)
                            drawCircle(
                                color = Color.White,
                                radius = 2.6.dp.toPx(),
                                center = Offset(px + cos(orbAng) * ringR, py + sin(orbAng) * ringR)
                            )
                        }
                    }
                }

                // Champ Harmonique d'Homéostasie & Présence (Niveau 80 : Ancrage Intéroceptif, 6s)
                if (gameState.homeostasisFieldTimer > 0f) {
                    val fieldRadius = minDim * 0.44f
                    val fieldPulse = 1f + 0.06f * sin(gameState.elapsedTime * 3.2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF059669).copy(alpha = 0.30f),
                                Color(0xFF34D399).copy(alpha = 0.14f),
                                Color.Transparent
                            ),
                            center = Offset(px, py),
                            radius = fieldRadius * fieldPulse
                        ),
                        radius = fieldRadius * fieldPulse,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = Color(0xFF34D399).copy(alpha = 0.78f),
                        radius = fieldRadius * fieldPulse,
                        center = Offset(px, py),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), phase = gameState.elapsedTime * 45f)
                        )
                    )
                }

                // Niveau 40 — Égide du Libre Arbitre (Prisme cristallin opalescent)
                if (gameState.freeWillAegisTimer > 0f) {
                    val aegisRadius = pRadius * 1.78f * (1f + 0.06f * sin(gameState.elapsedTime * 5f))
                    val aegisRot = gameState.elapsedTime * 36f
                    rotate(degrees = aegisRot, pivot = Offset(px, py)) {
                        val hexPath = Path().apply {
                            for (v in 0 until 6) {
                                val ang = (v * 60f) * (Math.PI.toFloat() / 180f)
                                val vxHex = px + cos(ang) * aegisRadius
                                val vyHex = py + sin(ang) * aegisRadius
                                if (v == 0) moveTo(vxHex, vyHex) else lineTo(vxHex, vyHex)
                            }
                            close()
                        }
                        drawPath(
                            path = hexPath,
                            color = Color(0xFF38BDF8).copy(alpha = 0.22f)
                        )
                        drawPath(
                            path = hexPath,
                            color = Color(0xFFE0F2FE).copy(alpha = 0.92f),
                            style = Stroke(width = 2.2.dp.toPx())
                        )
                    }
                }

                // Niveau 60 — Phare de Métacognition (Impulsion radar dorée anti-obscurantisme)
                if (gameState.metacognitionBeaconTimer > 0f) {
                    val sonarWave = (gameState.elapsedTime * 0.85f) % 1f
                    val sonarRadius = pRadius * (1.4f + sonarWave * 5.2f)
                    drawCircle(
                        color = Color(0xFFFDE047).copy(alpha = (1f - sonarWave) * 0.75f),
                        radius = sonarRadius,
                        center = Offset(px, py),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Niveau 100 — Ancrage d'Humilité (Inversion gravitationnelle indigo)
                if (gameState.humilityAnchorTimer > 0f) {
                    val repulseWave = (gameState.elapsedTime * 1.2f) % 1f
                    val repulseRadius = pRadius * (1.35f + repulseWave * 2.8f)
                    drawCircle(
                        color = Color(0xFF818CF8).copy(alpha = (1f - repulseWave) * 0.82f),
                        radius = repulseRadius,
                        center = Offset(px, py),
                        style = Stroke(
                            width = 2.2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 8f), phase = gameState.elapsedTime * 30f)
                        )
                    )
                }

                // Niveau 110 — Amortisseur de choc de la Gaine de Myéline
                if (gameState.myelinSheathTimer > 0f && gameState.myelinShockAbsorberAvailable) {
                    val myelinPulse = 1f + 0.08f * sin(gameState.elapsedTime * 6f)
                    drawCircle(
                        color = Color(0xFF5EEAD4).copy(alpha = 0.88f),
                        radius = pRadius * 1.48f * myelinPulse,
                        center = Offset(px, py),
                        style = Stroke(width = 2.4.dp.toPx())
                    )
                }

                // Niveau 120 — Déphasage Contemplatif (Ondes cérébrales Thêta & Intangibilité)
                if (gameState.phaseShiftTimer > 0f) {
                    val thetaPulse = 1f + 0.12f * sin(gameState.elapsedTime * 4.5f)
                    drawCircle(
                        color = Color(0xFFDDD6FE).copy(alpha = 0.72f),
                        radius = pRadius * 1.65f * thetaPulse,
                        center = Offset(px, py),
                        style = Stroke(
                            width = 1.8.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), phase = -gameState.elapsedTime * 25f)
                        )
                    )
                }

                // Niveau 130 — Arche de Plénitude (Sanctuaire Nomade : Anneau d'or et d'émeraude)
                if (gameState.plenitudeArkTimer > 0f) {
                    val arkRadius = minDim * 0.24f
                    val arkPulse = 1f + 0.04f * sin(gameState.elapsedTime * 3.0f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.20f),
                                Color(0xFFFBBF24).copy(alpha = 0.14f),
                                Color.Transparent
                            ),
                            center = Offset(px, py),
                            radius = arkRadius * arkPulse
                        ),
                        radius = arkRadius * arkPulse,
                        center = Offset(px, py)
                    )
                    rotate(degrees = gameState.elapsedTime * 24f, pivot = Offset(px, py)) {
                        drawCircle(
                            color = Color(0xFFFDE047).copy(alpha = 0.88f),
                            radius = arkRadius * arkPulse,
                            center = Offset(px, py),
                            style = Stroke(
                                width = 2.6.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), phase = 0f)
                            )
                        )
                    }
                    rotate(degrees = -gameState.elapsedTime * 18f, pivot = Offset(px, py)) {
                        drawCircle(
                            color = Color(0xFF34D399).copy(alpha = 0.82f),
                            radius = arkRadius * 0.92f * arkPulse,
                            center = Offset(px, py),
                            style = Stroke(
                                width = 1.8.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), phase = 8f)
                            )
                        )
                    }
                }

                // Portail de Franchissement Toroïdal autour de l'Étincelle (quand on traverse la ligne rouge)
                if (gameState.borderWrapTimer > 0f) {
                    val wrapProgress = 1f - (gameState.borderWrapTimer / 0.65f).coerceIn(0f, 1f)
                    val wrapAlpha = (1f - wrapProgress).coerceIn(0f, 1f)
                    val outerRingRadius = pRadius * (1.25f + wrapProgress * 3.2f)
                    val innerRingRadius = pRadius * (1.05f + wrapProgress * 1.9f)

                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.55f * wrapAlpha),
                                PlasmaRose.copy(alpha = 0.42f * wrapAlpha),
                                CyberCyanGlow.copy(alpha = 0.22f * wrapAlpha),
                                Color.Transparent
                            ),
                            center = Offset(px, py),
                            radius = outerRingRadius
                        ),
                        radius = outerRingRadius,
                        center = Offset(px, py)
                    )
                    drawCircle(
                        color = PlasmaRose.copy(alpha = 0.92f * wrapAlpha),
                        radius = outerRingRadius,
                        center = Offset(px, py),
                        style = Stroke(
                            width = 3.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(
                                floatArrayOf(14f, 8f),
                                phase = gameState.elapsedTime * 120f
                            )
                        )
                    )
                    drawCircle(
                        color = CyberCyanGlow.copy(alpha = 0.88f * wrapAlpha),
                        radius = innerRingRadius,
                        center = Offset(px, py),
                        style = Stroke(width = 2.2.dp.toPx())
                    )
                }

                // 2. La Couronne Éthérée (Halo circulaire souple qui épouse les virages sans barre rigide)
                val normalizedSpeed = (speedMag / 0.48f).coerceIn(0f, 1.8f)
                val crownBreathing = (1f + 0.12f * sin(gameState.elapsedTime * 2.6f)) * mentalContraction

                // Décalage arrière souple aligné sur le point récent du sillage pour suivre naturellement les virages à gauche/droite
                val rearAnchorOffset = if (trailSize >= 3 && speedMag > 0.02f) {
                    val anchorPt = gameState.trail[minOf(2, trailSize - 1)]
                    val ax = (anchorPt.x - camX) * w - px
                    val ay = (anchorPt.y - camY) * h - py
                    val aDist = hypot(ax, ay)
                    if (aDist in 1f..(minDim * 0.22f)) {
                        val maxShift = pRadius * (if (isHyperPropulsion) 0.42f else 0.22f)
                        Offset((ax / aDist) * minOf(aDist * 0.45f, maxShift), (ay / aDist) * minOf(aDist * 0.45f, maxShift))
                    } else {
                        Offset.Zero
                    }
                } else if (speedMag > 0.02f) {
                    val shiftMag = pRadius * (if (isHyperPropulsion) 0.35f else 0.20f) * normalizedSpeed.coerceAtMost(1.2f)
                    Offset(-cos(moveAngleRad) * shiftMag, -sin(moveAngleRad) * shiftMag)
                } else {
                    Offset.Zero
                }

                val haloCenter = Offset(px + rearAnchorOffset.x, py + rearAnchorOffset.y)
                val midHaloCenter = Offset(px + rearAnchorOffset.x * 0.45f, py + rearAnchorOffset.y * 0.45f)
                val outerCrownRadius = pRadius * 2.85f * crownBreathing
                val midCrownRadius = pRadius * 1.85f * crownBreathing

                // Halo solaire d'Hyper-Propulsion autour du cœur (suit le décalage courbé rearAnchorOffset sans rester droit)
                if (isHyperPropulsion) {
                    val blazeRadius = pRadius * 2.65f * crownBreathing
                    val blazeCenter = Offset(px + rearAnchorOffset.x * 1.25f, py + rearAnchorOffset.y * 1.25f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.85f),
                                SolarGoldGlow.copy(alpha = 0.68f),
                                CoralHyperGlow.copy(alpha = 0.36f),
                                Color.Transparent
                            ),
                            center = blazeCenter,
                            radius = blazeRadius
                        ),
                        radius = blazeRadius,
                        center = blazeCenter
                    )
                }

                // Couche externe de la Couronne Éthérée (diffuse la couleur choisie dans le vide spatial)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            sphereColor.glowColor.copy(alpha = (if (isBlinking) 0.22f else 0.52f) * oxygenFlicker),
                            sphereColor.primaryColor.copy(alpha = (if (isBlinking) 0.12f else 0.26f) * oxygenFlicker),
                            sphereColor.primaryColor.copy(alpha = 0.07f * oxygenFlicker),
                            Color.Transparent
                        ),
                        center = haloCenter,
                        radius = outerCrownRadius
                    ),
                    radius = outerCrownRadius,
                    center = haloCenter
                )

                // Couche médiane vaporeuse de la Couronne Éthérée
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = (if (isBlinking) 0.35f else 0.78f) * oxygenFlicker),
                            sphereColor.glowColor.copy(alpha = (if (isBlinking) 0.30f else 0.68f) * oxygenFlicker),
                            sphereColor.primaryColor.copy(alpha = 0.20f * oxygenFlicker),
                            Color.Transparent
                        ),
                        center = midHaloCenter,
                        radius = midCrownRadius
                    ),
                    radius = midCrownRadius,
                    center = midHaloCenter
                )

                // 3. Flammèches éthérées de feu follet cosmique autour du noyau
                val wispCount = 3
                for (wIdx in 0 until wispCount) {
                    val wispAngle = gameState.elapsedTime * (2.2f + wIdx * 0.6f) + wIdx * 2.094f
                    val wispOrbit = pRadius * (0.48f + 0.16f * sin(gameState.elapsedTime * 3.8f + wIdx)) * mentalContraction
                    val wx = px + cos(wispAngle) * wispOrbit
                    val wy = py + sin(wispAngle) * wispOrbit
                    val wispR = pRadius * 0.62f * mentalContraction
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.65f * oxygenFlicker),
                                sphereColor.glowColor.copy(alpha = 0.45f * oxygenFlicker),
                                Color.Transparent
                            ),
                            center = Offset(wx, wy),
                            radius = wispR
                        ),
                        radius = wispR,
                        center = Offset(wx, wy)
                    )
                }

                // 4. Le Noyau de Conscience : Point d'un blanc incandescent sans bordure stricte qui "respire"
                val coreBreathing = 1f + 0.16f * sin(gameState.elapsedTime * 3.4f)
                val coreRadius = pRadius * 0.95f * coreBreathing * mentalContraction
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = (if (isBlinking) 0.55f else 1.0f) * oxygenFlicker),
                            Color.White.copy(alpha = (if (isBlinking) 0.45f else 0.92f) * oxygenFlicker),
                            sphereColor.glowColor.copy(alpha = 0.55f * oxygenFlicker),
                            Color.Transparent
                        ),
                        center = Offset(px, py),
                        radius = coreRadius
                    ),
                    radius = coreRadius,
                    center = Offset(px, py)
                )

                // Indicateur visuel sur l'Étincelle quand le joueur a assez de Pensées Pures (>= 10 pts)
                // ET qu'un neurone infecté (ou Prisme Dogmatique / Voile d'Obscurantisme) est à proximité et visible sur la carte
                val hasUnpurifiedNeurons = gameState.infectedNeurons.any { !it.isPurified }
                val nearbyVisibleNeuron = gameState.nearestVisibleNearbyUnpurifiedNeuron()
                val nearbyVirtueObs = gameState.nearestVisibleNearbyVirtueObstacle()
                if (gameState.purificationEnergyPoints >= 10 && (nearbyVisibleNeuron != null || nearbyVirtueObs != null)) {
                    val readyPulse = 1f + 0.14f * sin(gameState.elapsedTime * 5f)
                    drawCircle(
                        color = SolarGoldGlow.copy(alpha = 0.85f),
                        radius = pRadius * 1.38f * readyPulse,
                        center = Offset(px, py),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), phase = gameState.elapsedTime * 35f)
                        )
                    )
                    val sphereHint = textMeasurer.measure(
                        text = "✨ Cliquez (Remède)",
                        style = TextStyle(
                            color = SolarGoldGlow,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    drawText(
                        textLayoutResult = sphereHint,
                        topLeft = Offset(
                            px - sphereHint.size.width / 2f,
                            py + pRadius * 1.35f
                        )
                    )
                } else if (gameState.purificationEnergyPoints >= 10 && hasUnpurifiedNeurons) {
                    val approachHint = textMeasurer.measure(
                        text = "🧭 Approchez d'un neurone 🧠",
                        style = TextStyle(
                            color = CyberCyanGlow.copy(alpha = 0.90f),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    drawText(
                        textLayoutResult = approachHint,
                        topLeft = Offset(
                            px - approachHint.size.width / 2f,
                            py + pRadius * 1.30f
                        )
                    )
                }

                // Draw 4x Space Mini-Map Radar in Top-Right Corner (when enabled in Paramètres)
                // Note : Le Voile d'Obscurantisme (L'Ignorance) brouille complètement les capteurs et masque les éléments de la mini-carte !
                if (isMiniMapEnabled) {
                    val mapSize = 78.dp.toPx()
                    val mapMargin = 12.dp.toPx()
                    val mapLeft = w - mapSize - mapMargin
                    val mapTop = mapMargin

                    drawRoundRect(
                        color = SpaceBackground.copy(alpha = 0.82f),
                        topLeft = Offset(mapLeft, mapTop),
                        size = Size(mapSize, mapSize),
                        cornerRadius = CornerRadius(10.dp.toPx())
                    )
                    drawRoundRect(
                        color = if (gameState.isMiniMapJammedByObscurantism) Color(0xFF64748B) else CyberCyan.copy(alpha = 0.6f),
                        topLeft = Offset(mapLeft, mapTop),
                        size = Size(mapSize, mapSize),
                        cornerRadius = CornerRadius(10.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    if (gameState.isMiniMapJammedByObscurantism) {
                        // Brouillard statique dense sur la mini-carte tant qu'un Voile d'Obscurantisme est actif
                        drawRoundRect(
                            color = Color(0xFF020617).copy(alpha = 0.92f),
                            topLeft = Offset(mapLeft + 2.dp.toPx(), mapTop + 2.dp.toPx()),
                            size = Size(mapSize - 4.dp.toPx(), mapSize - 4.dp.toPx()),
                            cornerRadius = CornerRadius(8.dp.toPx())
                        )
                        val jammedMeasured = textMeasurer.measure(
                            text = "🌑 VOILE\nBROUILLÉ",
                            style = TextStyle(
                                color = Color(0xFF94A3B8),
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        )
                        drawText(
                            textLayoutResult = jammedMeasured,
                            topLeft = Offset(
                                mapLeft + (mapSize - jammedMeasured.size.width) / 2f,
                                mapTop + (mapSize - jammedMeasured.size.height) / 2f
                            )
                        )
                    } else {
                        // Camera Viewport Box inside 4x Mini-Map
                        val vpLeft = mapLeft + (camX / WORLD_WIDTH) * mapSize
                        val vpTop = mapTop + (camY / WORLD_HEIGHT) * mapSize
                        val vpW = (VIEWPORT_WIDTH / WORLD_WIDTH) * mapSize
                        val vpH = (VIEWPORT_HEIGHT / WORLD_HEIGHT) * mapSize
                        drawRect(
                            color = Color.White.copy(alpha = 0.45f),
                            topLeft = Offset(vpLeft, vpTop),
                            size = Size(vpW, vpH),
                            style = Stroke(width = 1.dp.toPx())
                        )

                        // Power-Ups on Mini-Map Radar
                        for (pup in gameState.powerUps) {
                            if (!pup.isCollected) {
                                val mpx = mapLeft + (pup.x / WORLD_WIDTH) * mapSize
                                val mpy = mapTop + (pup.y / WORLD_HEIGHT) * mapSize
                                drawCircle(
                                    color = pup.type.glowColor,
                                    radius = 2.5.dp.toPx(),
                                    center = Offset(mpx, mpy)
                                )
                            }
                        }

                        // Rescue Targets on Mini-Map Radar
                        for (target in gameState.rescueTargets) {
                            if (!target.isSaved && !target.isDestroyed) {
                                val mtx = mapLeft + (target.x / WORLD_WIDTH) * mapSize
                                val mty = mapTop + (target.y / WORLD_HEIGHT) * mapSize
                                drawCircle(
                                    color = target.type.glowColor,
                                    radius = 2.2.dp.toPx(),
                                    center = Offset(mtx, mty)
                                )
                            }
                        }

                        // Infected Neurons on Mini-Map Radar (supprimés de la mini-carte dès qu'ils sont purifiés)
                        for (neuron in gameState.infectedNeurons) {
                            if (!neuron.isPurified) {
                                val mnx = mapLeft + (neuron.x / WORLD_WIDTH) * mapSize
                                val mny = mapTop + (neuron.y / WORLD_HEIGHT) * mapSize
                                drawCircle(
                                    color = neuron.corruptionType.infectedAuraColor,
                                    radius = 2.8.dp.toPx(),
                                    center = Offset(mnx, mny)
                                )
                            }
                        }

                        // Relics A, B, C on Mini-Map
                        for (relic in gameState.relics) {
                            if (!relic.isCollected) {
                                val mrx = mapLeft + (relic.x / WORLD_WIDTH) * mapSize
                                val mry = mapTop + (relic.y / WORLD_HEIGHT) * mapSize
                                val rCol = MeditationsMatrix.getFruitGlowColor(relic.fruitId)
                                drawCircle(
                                    color = rCol,
                                    radius = 3.5.dp.toPx(),
                                    center = Offset(mrx, mry)
                                )
                            }
                        }

                        // Brain Bonus on Mini-Map Radar
                        val miniBrain = gameState.brainRelic
                        if (miniBrain != null && !miniBrain.isCollected) {
                            val mbx = mapLeft + (miniBrain.x / WORLD_WIDTH) * mapSize
                            val mby = mapTop + (miniBrain.y / WORLD_HEIGHT) * mapSize
                            drawCircle(
                                color = VioletPulsarGlow,
                                radius = 3.8.dp.toPx(),
                                center = Offset(mbx, mby)
                            )
                        }

                        // Player Dot on Mini-Map
                        val mpx = mapLeft + (gameState.playerX / WORLD_WIDTH) * mapSize
                        val mpy = mapTop + (gameState.playerY / WORLD_HEIGHT) * mapSize
                        drawCircle(
                            color = sphereColor.primaryColor,
                            radius = 4.dp.toPx(),
                            center = Offset(mpx, mpy)
                        )
                    }
                }
            }

            // Active Power-Up Timers Chips (Top Left below HUD, stacking underneath when top row is full)
            val activeBuffs = remember(
                gameState.antiAsteroidTimer,
                gameState.antiDroneTimer,
                gameState.antiMineTimer,
                gameState.speedBoostTimer,
                gameState.magnetTimer,
                gameState.invulnerableTimer,
                gameState.homeostasisFieldTimer,
                gameState.elanDashTimer,
                gameState.freeWillAegisTimer,
                gameState.metacognitionBeaconTimer,
                gameState.humilityAnchorTimer,
                gameState.myelinSheathTimer,
                gameState.phaseShiftTimer,
                gameState.plenitudeArkTimer
            ) {
                buildList {
                    if (gameState.antiAsteroidTimer > 0f) {
                        add(Triple("🌿", "Anti-Parasite ${gameState.antiAsteroidTimer.toInt() + 1}s", EmeraldNovaGlow))
                    }
                    if (gameState.antiDroneTimer > 0f) {
                        add(Triple("💠", "Anti-Angoisse ${gameState.antiDroneTimer.toInt() + 1}s", CyberCyanGlow))
                    }
                    if (gameState.antiMineTimer > 0f) {
                        add(Triple("🌊", "Anti-Panique ${gameState.antiMineTimer.toInt() + 1}s", CoralHyperGlow))
                    }
                    if (gameState.speedBoostTimer > 0f) {
                        add(Triple("⚡", "Turbo ${gameState.speedBoostTimer.toInt() + 1}s", SolarGoldGlow))
                    }
                    if (gameState.magnetTimer > 0f) {
                        add(Triple("🧲", "Aimant ${gameState.magnetTimer.toInt() + 1}s", VioletPulsarGlow))
                    }
                    if (gameState.invulnerableTimer > 1.5f) {
                        add(Triple("🛡️", "Bouclier ${gameState.invulnerableTimer.toInt()}s", CyberCyanGlow))
                    }
                    if (gameState.homeostasisFieldTimer > 0f) {
                        add(Triple("🌿", "Homéostasie ${gameState.homeostasisFieldTimer.toInt() + 1}s", Color(0xFF34D399)))
                    }
                    if (gameState.elanDashTimer > 0f) {
                        add(Triple("🔥", "Élan Vital ${gameState.elanDashTimer.toInt() + 1}s", Color(0xFFFB923C)))
                    }
                    if (gameState.freeWillAegisTimer > 0f) {
                        add(Triple("🔷", "Libre Arbitre ${gameState.freeWillAegisTimer.toInt() + 1}s", Color(0xFFE0F2FE)))
                    }
                    if (gameState.metacognitionBeaconTimer > 0f) {
                        add(Triple("🏮", "Métacognition ${gameState.metacognitionBeaconTimer.toInt() + 1}s", Color(0xFFFDE047)))
                    }
                    if (gameState.humilityAnchorTimer > 0f) {
                        add(Triple("⚓", "Humilité ${gameState.humilityAnchorTimer.toInt() + 1}s", Color(0xFFA5B4FC)))
                    }
                    if (gameState.myelinSheathTimer > 0f) {
                        add(Triple("🧬", "Myéline ${gameState.myelinSheathTimer.toInt() + 1}s", Color(0xFF5EEAD4)))
                    }
                    if (gameState.phaseShiftTimer > 0f) {
                        add(Triple("👻", "Déphasage Thêta ${gameState.phaseShiftTimer.toInt() + 1}s", Color(0xFFDDD6FE)))
                    }
                    if (gameState.plenitudeArkTimer > 0f) {
                        add(Triple("🏛️", "Arche Plénitude ${gameState.plenitudeArkTimer.toInt() + 1}s", Color(0xFFFDE047)))
                    }
                }
            }

            if (activeBuffs.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 10.dp, start = 14.dp, end = if (isMiniMapEnabled) 96.dp else 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activeBuffs.chunked(2).forEach { rowBuffs ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            rowBuffs.forEach { (icon, label, glow) ->
                                Surface(
                                    color = SpaceSurface.copy(alpha = 0.92f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, glow)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(icon, fontSize = 12.sp)
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = glow
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Live Notification Banner (When A, B, or C is collected)
            if (gameState.bannerMessage != null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp, start = 16.dp, end = 96.dp),
                    color = SpaceSurface.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, sphereColor.glowColor)
                ) {
                    Text(
                        text = gameState.bannerMessage,
                        style = MaterialTheme.typography.labelLarge,
                        color = sphereColor.glowColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Message rapide avec effets indiquant 0%, 50% ou 100% purifié selon le choix du joueur
            val latestPurificationFeedback = gameState.purificationFeedbacks.lastOrNull()
            if (latestPurificationFeedback != null) {
                val fbProgress = (1f - (latestPurificationFeedback.life / latestPurificationFeedback.maxLife)).coerceIn(0f, 1f)
                val popScale = when {
                    fbProgress < 0.14f -> 0.82f + (fbProgress / 0.14f) * 0.28f // Burst up to 1.10x
                    fbProgress < 0.28f -> 1.10f - ((fbProgress - 0.14f) / 0.14f) * 0.10f // Settle to 1.0x
                    fbProgress > 0.82f -> 1.0f - ((fbProgress - 0.82f) / 0.18f) * 0.15f // Smooth fade-shrink
                    else -> 1.0f
                }
                val bannerAlpha = if (fbProgress > 0.78f) {
                    ((1f - fbProgress) / 0.22f).coerceIn(0f, 1f)
                } else {
                    1f
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 64.dp, start = 24.dp, end = 24.dp)
                        .scale(popScale)
                        .testTag("purification_result_flash_banner"),
                    color = SpaceSurface.copy(alpha = 0.95f * bannerAlpha),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(2.dp, latestPurificationFeedback.glowColor.copy(alpha = bannerAlpha)),
                    shadowElevation = 10.dp
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        latestPurificationFeedback.primaryColor.copy(alpha = 0.28f * bannerAlpha),
                                        SpaceSurface.copy(alpha = 0.92f * bannerAlpha),
                                        latestPurificationFeedback.primaryColor.copy(alpha = 0.28f * bannerAlpha)
                                    )
                                )
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = latestPurificationFeedback.label,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            ),
                            color = latestPurificationFeedback.glowColor.copy(alpha = bannerAlpha),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = latestPurificationFeedback.subLabel,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = StarlightWhite.copy(alpha = 0.95f * bannerAlpha),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Floating Circular Directional Joystick (Active during gameplay when enabled in Paramètres)
            if (isRoundJoystickEnabled && (gameState.status == GameStatus.PLAYING || gameState.status == GameStatus.READY)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 20.dp, bottom = 20.dp)
                ) {
                    RoundDirectionalJoystick(
                        sphereColor = sphereColor,
                        onDirectionChanged = onSetDirectionalInput
                    )
                }
            }

            // Status Modals (Paused, Quick Inventory, Level Complete, Game Over)
            when (gameState.status) {
                GameStatus.READY -> {
                    MissionStartOverlay(
                        gameState = gameState,
                        levelName = "Niveau ${gameState.level.levelNumber} : ${gameState.level.name}",
                        situationTitle = gameState.level.texts.firstOrNull()?.situationTitle ?: "",
                        sphereColor = sphereColor,
                        onStart = onStartOrNext,
                        onOpenGlossary = { showGlossaryDialog = true }
                    )
                }

                GameStatus.PAUSED -> {
                    if (resumeCountdown == null) {
                        PauseOverlay(
                            levelNumber = gameState.level.levelNumber,
                            runScore = gameState.runScore,
                            sphereColor = sphereColor,
                            onResume = onStartOrNext,
                            onOpenQuickInventory = onToggleQuickInventory,
                            onOpenSettings = onOpenSettings,
                            onBackToHangar = onBackToHangar,
                            onOpenGlossary = { showGlossaryDialog = true }
                        )
                    }
                }

                GameStatus.QUICK_INVENTORY -> {
                    QuickInventoryOverlay(
                        collectedInventory = collectedInventory,
                        currentLevel = gameState.level.levelNumber,
                        onCloseAndResume = onToggleQuickInventory,
                        onOpenFullInventory = onOpenFullInventory
                    )
                }

                GameStatus.LEVEL_COMPLETE -> {
                    LevelCompleteOverlay(
                        levelNumber = gameState.level.levelNumber,
                        sectorName = gameState.level.name,
                        runScore = gameState.runScore,
                        bonusPoints = gameState.level.completionBonusPoints,
                        relics = gameState.relics,
                        brainRelic = gameState.brainRelic,
                        sphereColor = sphereColor,
                        onNextLevel = onStartOrNext,
                        onConsultInventory = onOpenFullInventory
                    )
                }

                GameStatus.GAME_OVER -> {
                    GameOverOverlay(
                        levelNumber = gameState.level.levelNumber,
                        runScore = gameState.runScore,
                        collectedCountInRun = gameState.collectedLettersInRun.size,
                        onRetry = onRetryLevel,
                        onBackToHangar = onBackToHangar
                    )
                }

                GameStatus.PLAYING -> Unit
            }

            // Direct Text Collection Reader Dialog (when Texte direct is active)
            if (directCollectedText != null) {
                val alreadyClaimedInDb = collectedInventory.any {
                    it.title == directCollectedText.title && it.hasClaimedShareReward
                }
                DirectCollectedTextDialog(
                    relic = directCollectedText,
                    sphereColor = sphereColor,
                    onContinueMission = onDismissDirectCollectedText,
                    onOpenFullInventory = onOpenFullInventory,
                    onAnchorThought = onAnchorThought,
                    onShareTextReward = onShareTextReward,
                    isShareRewardAlreadyClaimed = alreadyClaimedInDb
                )
            } else if (gameState.showEndMissionTargetsPopup &&
                (gameState.status == GameStatus.LEVEL_COMPLETE || gameState.status == GameStatus.GAME_OVER)
            ) {
                EndMissionTargetsScoreDialog(
                    levelNumber = gameState.level.levelNumber,
                    isVictory = gameState.status == GameStatus.LEVEL_COMPLETE,
                    savedCount = gameState.endMissionSavedCount,
                    unsavedOnMapCount = gameState.endMissionUnsavedOnMapCount,
                    destroyedCount = gameState.endMissionDestroyedCount,
                    savedPoints = gameState.endMissionSavedPoints,
                    lostPoints = gameState.endMissionLostPoints,
                    playerTotalBefore = gameState.endMissionPlayerTotalBefore,
                    playerTotalAfter = gameState.endMissionPlayerTotalAfter,
                    missionRunScore = gameState.runScore,
                    sphereColor = sphereColor,
                    onDismiss = onDismissEndMissionTargetsPopup
                )
            }

            // 2-second countdown overlay before resuming game after Direct Text
            if (resumeCountdown != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.55f))
                        .testTag("resume_countdown_overlay"),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        color = SpaceSurface.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(2.dp, sphereColor.glowColor),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 36.dp, vertical = 26.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "REPRISE DANS",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = StarlightWhite
                            )
                            Text(
                                text = "$resumeCountdown",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontFamily = OrbitronFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 60.sp
                                ),
                                color = sphereColor.primaryColor
                            )
                            Text(
                                text = "Bouclier temporaire actif",
                                style = MaterialTheme.typography.bodySmall,
                                color = NebulaGray
                            )
                        }
                    }
                }
            }

            // Menu des Remèdes proposé quand le joueur clique sur sa sphère et qu'un ou plusieurs neurones infectés (ou Prisme / Voile) sont à proximité et visibles sur la carte
            val connectedNeuronsForMenu = gameState.allVisibleNearbyUnpurifiedNeurons()
            val nearbyVisibleForMenu = connectedNeuronsForMenu.firstOrNull()
            val nearbyVirtueObsForMenu = gameState.nearestVisibleNearbyVirtueObstacle()
            if (gameState.showSphereRemedyMenu &&
                gameState.status == GameStatus.PLAYING &&
                (nearbyVisibleForMenu != null || nearbyVirtueObsForMenu != null)
            ) {
                val targetWorldY = if (connectedNeuronsForMenu.isNotEmpty()) {
                    connectedNeuronsForMenu.map { it.y }.average().toFloat()
                } else {
                    nearbyVirtueObsForMenu?.y ?: gameState.playerY
                }
                val targetScreenYRatio = ((targetWorldY - gameState.cameraY) / VIEWPORT_HEIGHT).coerceIn(0f, 1f)
                val affectLabel = when {
                    connectedNeuronsForMenu.size > 1 ->
                        connectedNeuronsForMenu.joinToString(" • ") { it.negativeAffectLabel }
                    nearbyVisibleForMenu != null ->
                        nearbyVisibleForMenu.negativeAffectLabel
                    else ->
                        nearbyVirtueObsForMenu?.type?.displayName
                }
                val categoryLabel = when {
                    connectedNeuronsForMenu.size > 1 ->
                        connectedNeuronsForMenu.map { it.corruptionType.categoryTitle }.distinct().joinToString(" • ")
                    nearbyVisibleForMenu != null ->
                        nearbyVisibleForMenu.corruptionType.categoryTitle
                    else ->
                        nearbyVirtueObsForMenu?.type?.subtitle
                }
                SphereRemedyChoicesOverlay(
                    sphereColor = sphereColor,
                    purificationEnergyPoints = gameState.purificationEnergyPoints,
                    currentLevel = gameState.level.levelNumber,
                    nearestNeuronAffect = affectLabel,
                    nearestNeuronCategory = categoryLabel,
                    placeAtTop = targetScreenYRatio > 0.48f,
                    onChooseRemedy = onSelectVirtue,
                    onDismiss = onDismissSphereRemedyMenu
                )
            }

            if (showGlossaryDialog) {
                MapGlossaryDialog(
                    sphereColor = sphereColor,
                    onDismiss = { showGlossaryDialog = false }
                )
            }
        }
    }
}
