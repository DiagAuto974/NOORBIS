package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.domain.service.ContemplativeService
import com.example.domain.service.FeelingType
import com.example.domain.service.KeywordChallenge
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.components.astroTouchEffect
import com.example.data.local.CollectedTextEntity
import com.example.domain.model.FloatingTextRelic
import com.example.domain.model.MeditationsMatrix
import com.example.domain.model.NeuronCorruptionType
import com.example.domain.model.ObstacleType
import com.example.domain.model.PowerUpType
import com.example.domain.model.SimpleObstacleCategory
import com.example.domain.model.SimpleObstacleVariant
import com.example.domain.model.SphereColorOption
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.DangerRed
import com.example.ui.theme.EmeraldNova
import com.example.ui.theme.EmeraldNovaGlow
import com.example.ui.theme.NebulaGray
import com.example.ui.theme.PlasmaRose
import com.example.ui.theme.PlasmaRoseGlow
import androidx.compose.runtime.LaunchedEffect
import com.example.domain.service.AstroSoundManager
import com.example.ui.theme.OrbitronFontFamily
import com.example.ui.theme.SolarGoldGlow
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.SpaceSurfaceVariant
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.VioletPulsarGlow
import com.example.domain.model.VirtueRemedyType
import com.example.ui.viewmodel.ActiveGameState
import com.example.ui.viewmodel.GameStatus
import kotlin.math.hypot

@Composable
internal fun SpaceHudBar(
    gameState: ActiveGameState,
    sphereColor: SphereColorOption,
    inventoryCount: Int,
    onTogglePause: () -> Unit,
    onToggleQuickInventory: () -> Unit,
    onOpenSettings: () -> Unit,
    onSelectVirtue: (VirtueRemedyType) -> Unit = {},
    onTapPlayerSphere: () -> Unit = {},
    onOpenGlossary: () -> Unit = {}
) {
    Surface(
        color = SpaceSurface,
        border = BorderStroke(1.dp, SpaceCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Surface(
                        color = sphereColor.primaryColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, sphereColor.primaryColor)
                    ) {
                        Text(
                            text = "NIV. ${gameState.level.levelNumber}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = sphereColor.glowColor,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    val heartSize = if (gameState.maxShields > 4) 12.dp else 14.dp
                    Row(horizontalArrangement = Arrangement.spacedBy(1.5.dp)) {
                        for (i in 1..gameState.maxShields.coerceAtMost(7)) {
                            Icon(
                                imageVector = if (i <= gameState.shields) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Bouclier $i",
                                tint = if (i <= gameState.shields) PlasmaRose else NebulaGray,
                                modifier = Modifier.size(heartSize)
                            )
                        }
                    }

                    if (gameState.infectedNeurons.isNotEmpty() || gameState.rescueTargets.isNotEmpty()) {
                        val neuronGoalDone = gameState.isHalfNeuronsPurified
                        val neuronPrefix = if (neuronGoalDone) "✓🧠" else "🧠"
                        Surface(
                            color = if (neuronGoalDone) EmeraldNova.copy(alpha = 0.20f) else CyberCyan.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                1.dp,
                                if (neuronGoalDone) EmeraldNovaGlow.copy(alpha = 0.7f) else CyberCyanGlow.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                text = "✨${gameState.targetsSavedInLevel}/${gameState.rescueTargets.size}•$neuronPrefix${gameState.neuronsPurifiedInLevel}/${gameState.infectedNeurons.size}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.5.sp
                                ),
                                color = if (neuronGoalDone) EmeraldNovaGlow else CyberCyanGlow,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(start = 6.dp)
                ) {
                    Surface(
                        color = VioletPulsarGlow.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, VioletPulsarGlow.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenGlossary() }
                            .testTag("hud_glossary_button")
                    ) {
                        Text(
                            text = "📖 Glossaire",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            ),
                            color = StarlightWhite,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = "${gameState.runScore} PTS",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        ),
                        color = SolarGoldGlow,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.testTag("hud_score_text")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Fruit :",
                    style = MaterialTheme.typography.labelMedium,
                    color = NebulaGray
                )
                gameState.relics.forEach { relic ->
                    val isDone = relic.isCollected
                    val slotColor = MeditationsMatrix.getFruitGlowColor(relic.fruitId)
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hud_slot_${relic.fruitId}"),
                        color = if (isDone) slotColor.copy(alpha = 0.25f) else SpaceSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isDone) slotColor else SpaceCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDone) "✓ ${relic.fruitEmoji} ${relic.letter}" else "${relic.fruitEmoji} ${relic.letter}",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = if (isDone) slotColor else NebulaGray,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Brain Bonus slot (🧠 Fragment 1..72)
                gameState.brainRelic?.let { brain ->
                    val isBrainDone = brain.isCollected
                    Surface(
                        modifier = Modifier.testTag("hud_slot_brain"),
                        color = if (isBrainDone) PlasmaRoseGlow.copy(alpha = 0.25f) else SpaceSurfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isBrainDone) PlasmaRoseGlow else SpaceCardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(if (isBrainDone) "✓ 🧠" else "🧠", fontSize = 12.sp)
                            Text(
                                text = "N.${brain.brainLevel}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isBrainDone) PlasmaRoseGlow else NebulaGray
                            )
                        }
                    }
                }
            }

            // Énergie des Pensées Pures & Indication d'ouverture des remèdes sur la sphère
            val hasEnoughEnergy = gameState.purificationEnergyPoints >= 10
            val unpurifiedCount = gameState.infectedNeurons.count { !it.isPurified }
            val nearbyVisibleNeuron = gameState.nearestVisibleNearbyUnpurifiedNeuron()
            val nearbyVirtueObs = gameState.nearestVisibleNearbyVirtueObstacle()
            val canOpenRemedyNow = hasEnoughEnergy && (nearbyVisibleNeuron != null || nearbyVirtueObs != null)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (hasEnoughEnergy) SolarGoldGlow.copy(alpha = 0.20f) else SpaceSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (hasEnoughEnergy) SolarGoldGlow else SpaceCardBorder
                    )
                ) {
                    Text(
                        text = "✨ Pensées : ${gameState.purificationEnergyPoints} pts",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = if (hasEnoughEnergy) SolarGoldGlow else NebulaGray,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTapPlayerSphere() }
                        .testTag("hud_sphere_remedy_trigger"),
                    color = if (canOpenRemedyNow) {
                        sphereColor.primaryColor.copy(alpha = 0.28f)
                    } else {
                        SpaceSurfaceVariant
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        1.dp,
                        if (canOpenRemedyNow) sphereColor.glowColor else SpaceCardBorder
                    )
                ) {
                    Text(
                        text = when {
                            canOpenRemedyNow && nearbyVisibleNeuron != null ->
                                "🔮 Neurone « ${nearbyVisibleNeuron.negativeAffectLabel} » à proximité : Cliquez sur votre sphère"
                            canOpenRemedyNow && nearbyVirtueObs != null ->
                                "🔮 ${nearbyVirtueObs.type.displayName} à proximité : Cliquez sur votre sphère (Vertu)"
                            gameState.allFragmentsCollected && gameState.isBonusTextCollected && !gameState.isHalfNeuronsPurified ->
                                "🧠 Purifiez au moins la moitié des neurones (${gameState.neuronsPurifiedInLevel}/${gameState.requiredPurifiedNeurons}) pour passer au niveau suivant"
                            gameState.allFragmentsCollected && !gameState.isBonusTextCollected && gameState.isHalfNeuronsPurified ->
                                "🧠 Récupérez le Texte Bonus (N.${gameState.level.levelNumber}) sur la carte pour passer au niveau suivant"
                            !gameState.allFragmentsCollected && gameState.isBonusTextCollected && gameState.isHalfNeuronsPurified ->
                                "✨ Récupérez tous les fragments (${gameState.relics.count { it.isCollected }}/${gameState.relics.size}) pour passer au niveau suivant"
                            unpurifiedCount == 0 ->
                                "✓ Tous les neurones sont purifiés ! Récupérez tous les fragments et le Texte Bonus 🧠"
                            hasEnoughEnergy ->
                                "🧭 Approchez-vous d'un neurone infecté sur la carte pour le purifier (${gameState.neuronsPurifiedInLevel}/${gameState.requiredPurifiedNeurons} requis)"
                            else ->
                                "Récupérez une Pensée Pure (+10 pts) puis approchez d'un neurone infecté (${gameState.neuronsPurifiedInLevel}/${gameState.requiredPurifiedNeurons} requis)"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (canOpenRemedyNow) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 9.5.sp
                        ),
                        color = if (canOpenRemedyNow) StarlightWhite else NebulaGray,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

/**
 * Circular Directional Joystick with a movable directional center dot ("modèle en rond - un point au centre directionnel").
 * Floats directly in the space play area without any explanatory text or side frames.
 */

@Composable
internal fun RoundDirectionalJoystick(
    sphereColor: SphereColorOption,
    onDirectionChanged: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    // Normalized knob offset in -1f..1f
    var knobX by remember { mutableStateOf(0f) }
    var knobY by remember { mutableStateOf(0f) }
    var isActive by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .size(96.dp)
            .clip(CircleShape)
            .testTag("round_directional_joystick")
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    isActive = true
                    val centerX = size.width / 2f
                    val centerY = size.height / 2f
                    val maxRadius = (minOf(size.width, size.height) / 2f) * 0.68f

                    fun updateFromPosition(pos: Offset) {
                        val rawDx = pos.x - centerX
                        val rawDy = pos.y - centerY
                        val dist = hypot(rawDx, rawDy)
                        if (dist > 0.001f) {
                            val clampedDist = dist.coerceAtMost(maxRadius)
                            val nx = (rawDx / dist) * (clampedDist / maxRadius)
                            val ny = (rawDy / dist) * (clampedDist / maxRadius)
                            knobX = nx
                            knobY = ny
                            onDirectionChanged(nx, ny)
                        } else {
                            knobX = 0f
                            knobY = 0f
                            onDirectionChanged(0f, 0f)
                        }
                    }

                    updateFromPosition(down.position)

                    do {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null && change.pressed) {
                            change.consume()
                            updateFromPosition(change.position)
                        }
                    } while (event.changes.any { it.pressed })

                    isActive = false
                    knobX = 0f
                    knobY = 0f
                    onDirectionChanged(0f, 0f)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.minDimension / 2f) - 3.dp.toPx()
            val maxKnobTravel = outerRadius * 0.62f

            // 1. Outer Circular Base
            drawCircle(
                color = SpaceSurfaceVariant.copy(alpha = 0.82f),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = if (isActive) sphereColor.glowColor else sphereColor.primaryColor.copy(alpha = 0.65f),
                radius = outerRadius,
                center = center,
                style = Stroke(width = if (isActive) 2.5.dp.toPx() else 1.8.dp.toPx())
            )

            // 2. Subtle Inner Guide Ring & Crosshairs
            drawCircle(
                color = SpaceCardBorder.copy(alpha = 0.7f),
                radius = outerRadius * 0.55f,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
            drawLine(
                color = SpaceCardBorder.copy(alpha = 0.5f),
                start = Offset(center.x - outerRadius * 0.8f, center.y),
                end = Offset(center.x + outerRadius * 0.8f, center.y),
                strokeWidth = 1.dp.toPx()
            )
            drawLine(
                color = SpaceCardBorder.copy(alpha = 0.5f),
                start = Offset(center.x, center.y - outerRadius * 0.8f),
                end = Offset(center.x, center.y + outerRadius * 0.8f),
                strokeWidth = 1.dp.toPx()
            )

            // 3. Cardinal Perimeter Dots (N, S, E, W)
            val cardinalDist = outerRadius * 0.84f
            val dotRadius = 2.5.dp.toPx()
            listOf(
                Offset(center.x, center.y - cardinalDist),
                Offset(center.x, center.y + cardinalDist),
                Offset(center.x - cardinalDist, center.y),
                Offset(center.x + cardinalDist, center.y)
            ).forEach { pt ->
                drawCircle(
                    color = sphereColor.glowColor.copy(alpha = 0.6f),
                    radius = dotRadius,
                    center = pt
                )
            }

            // 4. Movable Directional Center Dot (Point central directionnel)
            val knobCenter = Offset(
                x = center.x + knobX * maxKnobTravel,
                y = center.y + knobY * maxKnobTravel
            )
            val knobRadius = 15.dp.toPx()

            // Direction vector line from center to dot when displaced
            if (isActive) {
                drawLine(
                    color = sphereColor.glowColor.copy(alpha = 0.55f),
                    start = center,
                    end = knobCenter,
                    strokeWidth = 3.dp.toPx()
                )
            }

            // Outer glow around the directional center dot
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        sphereColor.glowColor.copy(alpha = if (isActive) 0.75f else 0.45f),
                        sphereColor.primaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = knobCenter,
                    radius = knobRadius * 1.9f
                ),
                radius = knobRadius * 1.9f,
                center = knobCenter
            )

            // Solid directional center dot
            drawCircle(
                color = sphereColor.primaryColor,
                radius = knobRadius,
                center = knobCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = knobRadius * 0.38f,
                center = Offset(
                    knobCenter.x - knobRadius * 0.22f,
                    knobCenter.y - knobRadius * 0.22f
                )
            )
        }
    }
}

@Composable
fun GameSettingsDialog(
    isRoundJoystickEnabled: Boolean,
    isDirectTextEnabled: Boolean,
    isGridEnabled: Boolean,
    isMiniMapEnabled: Boolean,
    isSoundEnabled: Boolean = true,
    isEffectsSoundEnabled: Boolean = true,
    isInventoryAscending: Boolean = false,
    sphereColor: SphereColorOption,
    onToggleRoundJoystick: (Boolean) -> Unit,
    onToggleDirectText: (Boolean) -> Unit,
    onToggleGrid: (Boolean) -> Unit,
    onToggleMiniMap: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit = {},
    onToggleEffectsSound: (Boolean) -> Unit = {},
    onToggleInventoryAscending: (Boolean) -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .testTag("game_settings_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(1.5.dp, sphereColor.primaryColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PARAMÈTRES",
                        style = MaterialTheme.typography.headlineMedium,
                        color = StarlightWhite
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = sphereColor.glowColor, cornerRadius = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer les paramètres",
                            tint = StarlightWhite
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 0a. Musique de fond
                    item {
                        var selectedThemeOverride by remember {
                            mutableStateOf(AstroSoundManager.getManualMusicThemeOverride())
                        }
                        val themeLabels = remember { AstroSoundManager.getAllMusicThemeLabels() }

                        Surface(
                            color = SpaceSurfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isSoundEnabled) EmeraldNovaGlow else SpaceCardBorder
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = "Musique de fond",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = StarlightWhite
                                        )
                                        Text(
                                            text = if (isSoundEnabled)
                                                "Activé : 14 styles musicaux variés (Harpe, Synthwave, Carillon, Bols, Marimba, Électro, Valse...)."
                                            else
                                                "Désactivé : Musique d'ambiance coupée.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NebulaGray
                                        )
                                    }
                                    Switch(
                                        checked = isSoundEnabled,
                                        onCheckedChange = onToggleSound,
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = SpaceBackground,
                                            checkedTrackColor = EmeraldNovaGlow,
                                            uncheckedThumbColor = NebulaGray,
                                            uncheckedTrackColor = SpaceBackground
                                        ),
                                        modifier = Modifier.testTag("toggle_sound_switch")
                                    )
                                }

                                if (isSoundEnabled) {
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        item {
                                            val isAuto = selectedThemeOverride == -1
                                            Surface(
                                                onClick = {
                                                    selectedThemeOverride = -1
                                                    AstroSoundManager.setManualMusicThemeOverride(-1)
                                                },
                                                color = if (isAuto) EmeraldNovaGlow.copy(alpha = 0.22f) else SpaceBackground,
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isAuto) EmeraldNovaGlow else SpaceCardBorder
                                                )
                                            ) {
                                                Text(
                                                    text = "🔄 Auto (par niveau)",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isAuto) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isAuto) EmeraldNovaGlow else NebulaGray,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                        items(themeLabels.size) { idx ->
                                            val isSelected = selectedThemeOverride == idx
                                            Surface(
                                                onClick = {
                                                    selectedThemeOverride = idx
                                                    AstroSoundManager.setManualMusicThemeOverride(idx)
                                                },
                                                color = if (isSelected) CyberCyanGlow.copy(alpha = 0.24f) else SpaceBackground,
                                                shape = RoundedCornerShape(50),
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) CyberCyanGlow else SpaceCardBorder
                                                )
                                            ) {
                                                Text(
                                                    text = "🎵 ${themeLabels[idx]}",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    ),
                                                    color = if (isSelected) CyberCyanGlow else StarlightWhite,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 0b. Son des Effets
                    item {
                        Surface(
                            color = SpaceSurfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isEffectsSoundEnabled) CyberCyanGlow else SpaceCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "Son des Effets",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = StarlightWhite
                                    )
                                    Text(
                                        text = if (isEffectsSoundEnabled)
                                            "Activé : Effets sonores des collisions, collectes, neurones et vertus."
                                        else
                                            "Désactivé : Effets sonores d'action coupés.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NebulaGray
                                    )
                                }
                                Switch(
                                    checked = isEffectsSoundEnabled,
                                    onCheckedChange = onToggleEffectsSound,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SpaceBackground,
                                        checkedTrackColor = CyberCyanGlow,
                                        uncheckedThumbColor = NebulaGray,
                                        uncheckedTrackColor = SpaceBackground
                                    ),
                                    modifier = Modifier.testTag("toggle_effects_sound_switch")
                                )
                            }
                        }
                    }

                    // 0b. (Inventaire : activer ordre croissant) + Bouton
                    item {
                        Surface(
                            color = SpaceSurfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isInventoryAscending) VioletPulsarGlow else SpaceCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "(Inventaire : activer ordre croissant)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = StarlightWhite
                                    )
                                    Text(
                                        text = if (isInventoryAscending)
                                            "Activé : Niveaux des fragments classés par ordre croissant (1 → 135)."
                                        else
                                            "Désactivé : Niveaux des fragments classés par ordre décroissant (135 → 1).",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NebulaGray
                                    )
                                }
                                Switch(
                                    checked = isInventoryAscending,
                                    onCheckedChange = onToggleInventoryAscending,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SpaceBackground,
                                        checkedTrackColor = VioletPulsarGlow,
                                        uncheckedThumbColor = NebulaGray,
                                        uncheckedTrackColor = SpaceBackground
                                    ),
                                    modifier = Modifier.testTag("toggle_inventory_ascending_switch")
                                )
                            }
                        }
                    }

                    // 1. (Joystick) + Bouton
                    item {
                        Surface(
                            color = SpaceSurfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isRoundJoystickEnabled) sphereColor.primaryColor else SpaceCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "(Joystick)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = StarlightWhite
                                    )
                                    Text(
                                        text = if (isRoundJoystickEnabled)
                                            "Activé : Contrôleur en rond avec point central directionnel."
                                        else
                                            "Désactivé : Pilotage direct par glissement sur l'espace.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NebulaGray
                                    )
                                }
                                Switch(
                                    checked = isRoundJoystickEnabled,
                                    onCheckedChange = onToggleRoundJoystick,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SpaceBackground,
                                        checkedTrackColor = sphereColor.primaryColor,
                                        uncheckedThumbColor = NebulaGray,
                                        uncheckedTrackColor = SpaceBackground
                                    ),
                                    modifier = Modifier.testTag("toggle_round_joystick_switch")
                                )
                            }
                        }
                    }

                    // 2. (Texte direct) + Bouton
                    item {
                        Surface(
                            color = SpaceSurfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isDirectTextEnabled) CyberCyanGlow else SpaceCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "(Texte direct)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = StarlightWhite
                                    )
                                    Text(
                                        text = if (isDirectTextEnabled)
                                            "Coché : Affiche le texte directement à la collecte."
                                        else
                                            "Décoché : Lecture en fin de niveau ou dans l'inventaire.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NebulaGray
                                    )
                                }
                                Switch(
                                    checked = isDirectTextEnabled,
                                    onCheckedChange = onToggleDirectText,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SpaceBackground,
                                        checkedTrackColor = CyberCyanGlow,
                                        uncheckedThumbColor = NebulaGray,
                                        uncheckedTrackColor = SpaceBackground
                                    ),
                                    modifier = Modifier.testTag("toggle_direct_text_switch")
                                )
                            }
                        }
                    }

                    // 3. (Quadrillage) + Bouton
                    item {
                        Surface(
                            color = SpaceSurfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isGridEnabled) SolarGoldGlow else SpaceCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "(Quadrillage)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = StarlightWhite
                                    )
                                    Text(
                                        text = if (isGridEnabled)
                                            "Activé : Lignes de quadrillage tactiques visibles sur la carte."
                                        else
                                            "Désactivé : Espace profond épuré sans quadrillage.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NebulaGray
                                    )
                                }
                                Switch(
                                    checked = isGridEnabled,
                                    onCheckedChange = onToggleGrid,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SpaceBackground,
                                        checkedTrackColor = SolarGoldGlow,
                                        uncheckedThumbColor = NebulaGray,
                                        uncheckedTrackColor = SpaceBackground
                                    ),
                                    modifier = Modifier.testTag("toggle_grid_switch")
                                )
                            }
                        }
                    }

                    // 4. (Mini-carte) + Bouton
                    item {
                        Surface(
                            color = SpaceSurfaceVariant,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(
                                1.dp,
                                if (isMiniMapEnabled) CyberCyan else SpaceCardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "(Mini-carte)",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = StarlightWhite
                                    )
                                    Text(
                                        text = if (isMiniMapEnabled)
                                            "Activé : Radar spatial miniature visible en haut à droite."
                                        else
                                            "Désactivé : Radar miniature masqué pour un espace épuré.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NebulaGray
                                    )
                                }
                                Switch(
                                    checked = isMiniMapEnabled,
                                    onCheckedChange = onToggleMiniMap,
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = SpaceBackground,
                                        checkedTrackColor = CyberCyan,
                                        uncheckedThumbColor = NebulaGray,
                                        uncheckedTrackColor = SpaceBackground
                                    ),
                                    modifier = Modifier.testTag("toggle_minimap_switch")
                                )
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = sphereColor.glowColor, cornerRadius = 14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sphereColor.primaryColor,
                        contentColor = SpaceBackground
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("VALIDER", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
fun DirectCollectedTextDialog(
    relic: FloatingTextRelic,
    sphereColor: SphereColorOption,
    onContinueMission: () -> Unit,
    onOpenFullInventory: () -> Unit,
    onAnchorThought: (String) -> Unit = {},
    onShareTextReward: () -> Unit = {},
    isShareRewardAlreadyClaimed: Boolean = false
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val relicColor = if (relic.isBrainBonus) VioletPulsarGlow else MeditationsMatrix.getFruitGlowColor(relic.fruitId)

    // Challenge keyword extraction
    val challenge = remember(relic) {
        ContemplativeService.extractKeywordChallenge(relic.verse.ifBlank { relic.content }, relic.letter)
    }

    var isDecoded by remember(relic) { mutableStateOf(false) }
    var selectedOption by remember(relic) { mutableStateOf<String?>(null) }
    var showHint by remember(relic) { mutableStateOf(false) }

    // Gemini personal resonance state
    var selectedFeeling by remember(relic) { mutableStateOf<FeelingType?>(null) }
    var reflectionText by remember(relic) { mutableStateOf<String?>(null) }
    var isLoadingReflection by remember(relic) { mutableStateOf(false) }

    // Press & Hold to Anchor thought (2 seconds)
    var anchorProgress by remember(relic) { mutableStateOf(0f) }
    var isAnchored by remember(relic) { mutableStateOf(false) }
    var isHolding by remember(relic) { mutableStateOf(false) }
    var shareRewardClaimed by remember(relic.title, isShareRewardAlreadyClaimed) {
        mutableStateOf(isShareRewardAlreadyClaimed)
    }
    var justClaimedInThisDialog by remember(relic.title) { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(
        targetValue = anchorProgress,
        label = "anchorProgress"
    )

    Dialog(
        onDismissRequest = { if (isDecoded) onContinueMission() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = isDecoded,
            dismissOnClickOutside = isDecoded
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .widthIn(max = 460.dp)
                .testTag("direct_collected_text_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(2.dp, if (isAnchored) SolarGoldGlow else relicColor)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = relicColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, relicColor)
                    ) {
                        Text(
                            text = if (relic.isBrainBonus) "FRAGMENT BONUS • 🧠 NIVEAU ${relic.brainLevel}" else "SANCTUAIRE • ${relic.fruitEmoji} ${relic.letter.uppercase()}",
                            style = MaterialTheme.typography.labelLarge,
                            color = relicColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    IconButton(
                        onClick = { if (isDecoded) onContinueMission() },
                        enabled = isDecoded,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = relicColor, cornerRadius = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer la lecture",
                            tint = if (isDecoded) StarlightWhite else NebulaGray.copy(alpha = 0.35f)
                        )
                    }
                }

                Text(
                    text = relic.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = StarlightWhite
                )

                // Scrollable contemplative content
                Surface(
                    color = SpaceSurfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, SpaceCardBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 340.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Étape 1 : Décodage du Verset
                        item {
                            Surface(
                                color = SpaceSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isDecoded) EmeraldNovaGlow else relicColor.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isDecoded) "📖 Verset Dévoilé (${relic.scriptureRef})" else "✨ Étape 1 : Révélez la parole",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = if (isDecoded) EmeraldNovaGlow else relicColor
                                        )
                                        if (!isDecoded) {
                                            Text(
                                                text = "Trouvez le mot clé",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = NebulaGray
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (isDecoded) relic.verse.ifBlank { relic.content } else challenge.maskedVerse,
                                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                        color = if (isDecoded) StarlightWhite else relicColor
                                    )

                                    if (!isDecoded) {
                                        Text(
                                            text = "Touchez le mot qui complète le verset :",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = NebulaGray
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            for (opt in challenge.options) {
                                                val isCorrect = opt.equals(challenge.targetWord, ignoreCase = true)
                                                Surface(
                                                    color = if (selectedOption == opt) {
                                                        if (isCorrect) EmeraldNova.copy(alpha = 0.3f) else DangerRed.copy(alpha = 0.25f)
                                                    } else SpaceSurfaceVariant,
                                                    shape = RoundedCornerShape(20.dp),
                                                    border = BorderStroke(
                                                        1.dp,
                                                        if (selectedOption == opt && isCorrect) EmeraldNovaGlow else SpaceCardBorder
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .astroTouchEffect(glowColor = EmeraldNovaGlow, cornerRadius = 20.dp)
                                                        .clickable {
                                                            selectedOption = opt
                                                            if (isCorrect) {
                                                                isDecoded = true
                                                                showHint = false
                                                            } else {
                                                                showHint = true
                                                            }
                                                        }
                                                ) {
                                                    Text(
                                                        text = opt,
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                        color = if (selectedOption == opt && isCorrect) EmeraldNovaGlow else StarlightWhite,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(vertical = 10.dp)
                                                    )
                                                }
                                            }
                                        }

                                        if (showHint) {
                                            Text(
                                                text = "« Écoutez avec le cœur... réessayez avec bienveillance. »",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SolarGoldGlow
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Étape 2 : Explication & Prière (affichées une fois décodé)
                        if (isDecoded) {
                            if (relic.explain.isNotBlank()) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "💡 Éclairage Spirituel :",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = SolarGoldGlow
                                        )
                                        Text(
                                            text = relic.explain,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = StarlightWhite
                                        )
                                    }
                                }
                            }

                            if (!relic.isBrainBonus && relic.prayer.isNotBlank()) {
                                item {
                                    Surface(
                                        color = sphereColor.primaryColor.copy(alpha = 0.12f),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, sphereColor.glowColor.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = "🙏 Prière du Sanctuaire :",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = sphereColor.glowColor
                                            )
                                            Text(
                                                text = relic.prayer,
                                                style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                                                color = StarlightWhite
                                            )
                                        }
                                    }
                                }
                            }

                            // Étape 3 : Résonance Personnelle (uniquement pour les méditations du Fruit de l'Esprit)
                            if (!relic.isBrainBonus) {
                                item {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "💬 Résonance : Ce texte me parle parce que...",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = CyberCyanGlow,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "↔ Glisser",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = NebulaGray
                                            )
                                        }

                                        LazyRow(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("resonance_feelings_lazy_row"),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            items(FeelingType.values().toList(), key = { it.name }) { feeling ->
                                                val isSelected = selectedFeeling == feeling
                                                Surface(
                                                    color = if (isSelected) CyberCyan.copy(alpha = 0.35f) else SpaceSurface,
                                                    shape = RoundedCornerShape(14.dp),
                                                    border = BorderStroke(
                                                        width = if (isSelected) 1.5.dp else 1.dp,
                                                        color = if (isSelected) CyberCyanGlow else SpaceCardBorder
                                                    ),
                                                    modifier = Modifier
                                                        .minimumInteractiveComponentSize()
                                                        .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 14.dp)
                                                        .clickable {
                                                            selectedFeeling = feeling
                                                            isLoadingReflection = true
                                                            reflectionText = null
                                                            coroutineScope.launch {
                                                                val res = ContemplativeService.generatePersonalReflection(
                                                                    verse = relic.verse.ifBlank { relic.content },
                                                                    fruit = relic.letter,
                                                                    situation = relic.situationTitle,
                                                                    feeling = feeling
                                                                )
                                                                reflectionText = res
                                                                isLoadingReflection = false
                                                            }
                                                        }
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Text(feeling.emoji, fontSize = 18.sp)
                                                        Text(
                                                            text = feeling.label,
                                                            style = MaterialTheme.typography.labelLarge.copy(
                                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                            ),
                                                            color = if (isSelected) CyberCyanGlow else StarlightWhite,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        if (isLoadingReflection) {
                                            Row(
                                                modifier = Modifier.padding(vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = CyberCyanGlow
                                                )
                                                Text(
                                                    text = "Écoute contemplative et réconfort...",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = CyberCyanGlow
                                                )
                                            }
                                        }

                                        if (reflectionText != null) {
                                            Surface(
                                                color = CyberCyan.copy(alpha = 0.14f),
                                                shape = RoundedCornerShape(12.dp),
                                                border = BorderStroke(1.dp, CyberCyanGlow),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = "🕊️ Parole d'encouragement :",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = CyberCyanGlow
                                                    )
                                                    Text(
                                                        text = reflectionText ?: "",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = StarlightWhite
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Étape 4 : Pensée Clé à Ancrer
                            if (relic.keep.isNotBlank()) {
                                item {
                                    Surface(
                                        color = SolarGoldGlow.copy(alpha = if (isAnchored) 0.28f else 0.12f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.5.dp, if (isAnchored) SolarGoldGlow else SolarGoldGlow.copy(alpha = 0.4f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "⚓ À retenir au cœur :",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = SolarGoldGlow
                                                )
                                                if (isAnchored) {
                                                    Text(
                                                        text = "✓ PENSÉE ANCRÉE",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = SolarGoldGlow
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "« ${relic.keep} »",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = StarlightWhite
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Interactive Hold-to-Anchor Action Bar (without the 2s instruction line)
                if (isDecoded) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Press and Hold Anchor Button
                        Surface(
                            color = if (isAnchored) SolarGoldGlow else relicColor,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .astroTouchEffect(glowColor = SolarGoldGlow, pressScale = 0.96f, cornerRadius = 14.dp)
                                .pointerInput(isAnchored, relic.keep) {
                                    if (!isAnchored) {
                                        detectTapGestures(
                                            onPress = {
                                                isHolding = true
                                                var holdTime = 0f
                                                val step = 0.05f
                                                while (isHolding && holdTime < 1.6f) {
                                                    kotlinx.coroutines.delay(50)
                                                    holdTime += step
                                                    anchorProgress = (holdTime / 1.6f).coerceIn(0f, 1f)
                                                }
                                                if (holdTime >= 1.6f) {
                                                    isAnchored = true
                                                    anchorProgress = 1f
                                                    onAnchorThought(relic.keep)
                                                } else {
                                                    anchorProgress = 0f
                                                }
                                                tryAwaitRelease()
                                                isHolding = false
                                                if (!isAnchored) anchorProgress = 0f
                                            }
                                        )
                                    } else {
                                        detectTapGestures(
                                            onTap = { onContinueMission() }
                                        )
                                    }
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                if (isHolding && !isAnchored) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.White.copy(alpha = 0.35f))
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isAnchored) "🕊️ PENSÉE ANCRÉE • REPRENDRE (+AURA DE PAIX)" else if (isHolding) "ANCRAGE EN COURS (${(anchorProgress * 100).toInt()}%)" else "MAINTENIR POUR ANCRER LA PENSÉE",
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = SpaceBackground,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        if (isHolding && !isAnchored) {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp),
                                color = SolarGoldGlow,
                                trackColor = SpaceSurfaceVariant
                            )
                        }
                    }
                }

                if (shareRewardClaimed) {
                    Surface(
                        color = EmeraldNova.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, EmeraldNovaGlow),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (justClaimedInThisDialog) {
                                "✨ +3 Pensées Positives (+30 pts d'énergie) gagnées !"
                            } else {
                                "✓ Bonus de partage (+3 Pensées Positives) déjà obtenu pour ce texte"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = EmeraldNovaGlow,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Redesigned Bottom Action Bar: Partager, Inventaire, Reprendre (clean, single-line, with icons & touch effect)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!shareRewardClaimed) {
                                shareRewardClaimed = true
                                justClaimedInThisDialog = true
                                onShareTextReward()
                            }
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, relic.title)
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "${relic.title}\n\n« ${relic.verse.ifBlank { relic.content }} »\n\nÀ retenir : « ${relic.keep} »\n\n— Noôrbis, Demeurer dans le Fruit de l'Esprit"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Partager la méditation"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = if (shareRewardClaimed) EmeraldNovaGlow else StarlightWhite, cornerRadius = 14.dp)
                            .testTag("direct_share_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (shareRewardClaimed) EmeraldNovaGlow else SpaceCardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (shareRewardClaimed) EmeraldNova.copy(alpha = 0.18f) else SpaceSurfaceVariant.copy(alpha = 0.65f),
                            contentColor = if (shareRewardClaimed) EmeraldNovaGlow else StarlightWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = if (shareRewardClaimed) EmeraldNovaGlow else StarlightWhite,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (shareRewardClaimed) "✓ +3 ✨" else "Partager (+3 ✨)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (shareRewardClaimed) EmeraldNovaGlow else StarlightWhite,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenFullInventory,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 14.dp)
                            .testTag("direct_inventory_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.75f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CyberCyan.copy(alpha = 0.14f),
                            contentColor = CyberCyanGlow
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = null,
                            tint = CyberCyanGlow,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Inventaire",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = CyberCyanGlow,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Button(
                        onClick = onContinueMission,
                        enabled = isDecoded,
                        modifier = Modifier
                            .weight(1.08f)
                            .minimumInteractiveComponentSize()
                            .then(
                                if (isDecoded) {
                                    Modifier.astroTouchEffect(glowColor = EmeraldNovaGlow, cornerRadius = 14.dp)
                                } else {
                                    Modifier
                                }
                            )
                            .testTag("resume_after_direct_text_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldNova.copy(alpha = 0.25f),
                            contentColor = EmeraldNovaGlow,
                            disabledContainerColor = SpaceSurfaceVariant.copy(alpha = 0.45f),
                            disabledContentColor = NebulaGray.copy(alpha = 0.45f)
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isDecoded) EmeraldNovaGlow else SpaceCardBorder.copy(alpha = 0.45f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = if (isDecoded) EmeraldNovaGlow else NebulaGray.copy(alpha = 0.45f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reprendre",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDecoded) StarlightWhite else NebulaGray.copy(alpha = 0.45f),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

internal data class LevelUnlockedBriefingItem(
    val categoryLabel: String,
    val isNewlyUnlockedThisLevel: Boolean,
    val emoji: String,
    val title: String,
    val badge: String,
    val badgeColor: Color,
    val coreColor: Color = Color(0xFF1E1B4B),
    val description: String,
    val visualType: String
)

private fun simpleVariantToBriefingItem(
    variant: SimpleObstacleVariant,
    currentLevel: Int
): LevelUnlockedBriefingItem {
    val visualType = when (variant.baseArchetype) {
        ObstacleType.ASTEROID -> "PARASITE"
        ObstacleType.LASER_DRONE -> "ANGOISSE"
        ObstacleType.PULSAR_MINE -> "PANIQUE"
        else -> "PARASITE"
    }
    val isNew = variant.unlockLevel == currentLevel
    val description = when (variant) {
        SimpleObstacleVariant.PENSEE_PARASITE ->
            "Masse organique sombre et toxique aux lobes vénéneux qui dérive lourdement dans l'espace mental. Écrase les Pensées Pures sur son passage.\n• Remède / Bonus : 🌿 Dissipateur Parasite (+60 pts)."
        SimpleObstacleVariant.FOYER_ANGOISSE ->
            "Noyau de haute tension nerveuse projetant des aiguilles et faisceaux rotatifs d'anxiété (Pics de Stress) qui balaient l'espace.\n• Remède / Bonus : 💠 Sérénité Anti-Angoisse (+80 pts)."
        SimpleObstacleVariant.CRISE_PANIQUE ->
            "Cœur émotionnel instable qui émet des ondes de choc électriques concentriques (Décharges Émotives) à haute fréquence.\n• Remède / Bonus : 🌊 Calme Anti-Panique (+100 pts)."
        else ->
            "${variant.definition}\n• Catégorie : ${variant.category.title}\n• Menace directe contre les boucliers par collision physique (-1 ❤️).\n• Neutralisation : ${variant.counterPowerUpLabel} ou 💥 Onde Nova."
    }
    return LevelUnlockedBriefingItem(
        categoryLabel = if (variant.unlockLevel >= 10) {
            variant.category.title
        } else {
            "⚠️ Obstacle Mental Simple"
        },
        isNewlyUnlockedThisLevel = isNew,
        emoji = variant.emoji,
        title = "Niv. ${variant.unlockLevel} • ${variant.displayName}",
        badge = "Niv. ${variant.unlockLevel}+ • Collision directe (-1 ❤️) • ${variant.counterPowerUpLabel}",
        badgeColor = variant.category.accentColor,
        coreColor = variant.category.coreColor,
        description = description,
        visualType = visualType
    )
}

private fun specialObstacleToBriefingItem(
    type: ObstacleType,
    currentLevel: Int
): LevelUnlockedBriefingItem? {
    val isNew = type.unlockLevel == currentLevel
    return when (type) {
        ObstacleType.CONFORMISM_SWARM -> LevelUnlockedBriefingItem(
            categoryLabel = "⚠️ Obstacle Spécial (Palier Niv. 20)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🐑",
            title = "Conformisme (L'esprit « Mouton ») : La Nuée Grégaire",
            badge = "Niv. 20+ • Absorbe les bonus & ralentit",
            badgeColor = Color(0xFF94A3B8),
            coreColor = Color(0xFF334155),
            description = "Un essaim d'entités grises, ternes et parfaitement identiques qui se déplacent en rangs serrés, bloquant de larges zones de la carte. Elles n'infligent pas de dégâts directs, mais si votre sphère les traverse, elles absorbent vos capacités spéciales (Hyper-Propulsion, Aimant Cosmique...) et vous ralentissent pour vous forcer à « rentrer dans le rang ».",
            visualType = "CONFORMISME"
        )
        ObstacleType.DOGMATIC_PRISM -> LevelUnlockedBriefingItem(
            categoryLabel = "⚠️ Obstacle Spécial (Palier Niv. 40)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "💎",
            title = "Endoctrinement : Le Prisme Dogmatique",
            badge = "Niv. 40+ • Reflète les vertus classiques",
            badgeColor = Color(0xFF38BDF8),
            coreColor = Color(0xFF0F172A),
            description = "Structure géométrique froide, rigide et inébranlable (monolithe aux arêtes tranchantes) qui contraste avec les formes organiques des Pensées Parasites. Il reflète ou dévie les projectiles de vertu classiques (Paix, Joie, Amour).\n• Pour le briser : utilisez une 💥 Onde Nova (EMP) ou projetez la vertu « Vérité & Discernement » 🔍 (+90 pts).",
            visualType = "PRISME"
        )
        ObstacleType.OBSCURANTISM_VEIL -> LevelUnlockedBriefingItem(
            categoryLabel = "⚠️ Obstacle Spécial (Palier Niv. 60)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🌑",
            title = "L'Ignorance : Le Voile d'Obscurantisme",
            badge = "Niv. 60+ • Brouille la mini-carte & cache les Pensées",
            badgeColor = Color(0xFF64748B),
            coreColor = Color(0xFF020617),
            description = "Une nébuleuse d'encre noire et un brouillard statique très dense qui s'étend sur l'espace mental. Lorsqu'il est présent, il brouille les capteurs (masque les éléments de la mini-carte) et dissimule visuellement les Étincelles de Lucidité et Fragments de Savoir situés à l'intérieur.\n• Pour le dissiper : tirez un mot de vertu dedans (+70 pts) ou activez le 🏮 Phare de Métacognition.",
            visualType = "OBSCURANTISME"
        )
        ObstacleType.MENTAL_MIRE -> LevelUnlockedBriefingItem(
            categoryLabel = "⚠️ Obstacle Spécial (Palier Niv. 80)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🕸️",
            title = "Incapacité : Le Bourbier Mental",
            badge = "Niv. 80+ • Divise la vitesse par 3",
            badgeColor = Color(0xFF94A3B8),
            coreColor = Color(0xFF334155),
            description = "Un champ de force poisseux et une anomalie gravitationnelle visqueuse aux teintes grisâtres. Entrer dans ce champ divise la vitesse de votre sphère par trois, rendant l'esquive presque impossible.\n• Pour s'en arracher sans pénalité : utilisez un bonus ⚡ Hyper-Propulsion ou le remède 🔥 Élan Vital.",
            visualType = "BOURBIER"
        )
        ObstacleType.NARCISSUS_MIRROR -> LevelUnlockedBriefingItem(
            categoryLabel = "⚠️ Obstacle Spécial (Palier Niv. 100)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🪞",
            title = "L'Ego : Le Miroir de Narcisse",
            badge = "Niv. 100+ • Aimant inversé & faux bonus",
            badgeColor = SolarGoldGlow,
            coreColor = PlasmaRose,
            description = "Un astre artificiellement brillant et scintillant qui imite l'apparence des bonus ou des cœurs. Il agit comme un Aimant Cosmique inversé : il attire inexorablement votre sphère vers lui. Si vous le touchez, au lieu de vous soigner, il draine un de vos boucliers (-1 ❤️) ou consomme vos points d'énergie de purification.",
            visualType = "NARCISSE"
        )
        else -> null
    }
}

private fun powerUpToBriefingItem(
    type: PowerUpType,
    currentLevel: Int
): LevelUnlockedBriefingItem {
    val isNew = type.unlockLevel == currentLevel
    return when (type) {
        PowerUpType.FREE_WILL_AEGIS -> LevelUnlockedBriefingItem(
            categoryLabel = "🛡️ Moyen Défensif (Palier Niv. 40)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🔷",
            title = "Égide du Libre Arbitre (Bouclier Réfractaire)",
            badge = "Niv. 40+ • Durée 6s • +5 énergie/choc",
            badgeColor = Color(0xFFE0F2FE),
            coreColor = Color(0xFF0284C7),
            description = "Enveloppe la sphère d'un prisme cristallin opalescent qui immunise vos bonus actifs (Turbo, Aimant) contre la Nuée Grégaire. Tout choc contre un Prisme Dogmatique ou un Foyer d'Angoisse fait rebondir l'obstacle sans consommer de bouclier et génère +5 pts d'énergie.",
            visualType = "POWERUP_AEGIS"
        )
        PowerUpType.METACOGNITION_BEACON -> LevelUnlockedBriefingItem(
            categoryLabel = "🛡️ Moyen Défensif (Palier Niv. 60)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🏮",
            title = "Phare de Métacognition (Sonar Anti-Obscurantisme)",
            badge = "Niv. 60+ • Durée 8s • Bulle Pensées Pures",
            badgeColor = Color(0xFFFDE047),
            coreColor = Color(0xFFD97706),
            description = "Émet une impulsion radar dorée qui rétablit immédiatement la mini-carte malgré le brouillage, révèle toutes les cibles cachées sous le Voile d'Obscurantisme et pose une bulle d'invulnérabilité autour des Souvenirs et Étincelles de Lucidité présents à l'écran.",
            visualType = "POWERUP_BEACON"
        )
        PowerUpType.HUMILITY_ANCHOR -> LevelUnlockedBriefingItem(
            categoryLabel = "🛡️ Moyen Défensif (Palier Niv. 100)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "⚓",
            title = "Ancrage d'Humilité (Inversion Gravitationnelle)",
            badge = "Niv. 100+ • Durée 6s • Conversion +1 🛡️",
            badgeColor = Color(0xFFA5B4FC),
            coreColor = Color(0xFF4338CA),
            description = "Verrouille votre réserve d'énergie de purification contre tout drainage et inverse l'aimant du Miroir de Narcisse en zone de répulsion douce. Si un Miroir tente de vous aspirer, le lien s'inverse et vous octroie +1 Bouclier Stellaire temporaire (max 7).",
            visualType = "POWERUP_ANCHOR"
        )
        PowerUpType.MYELIN_SHEATH -> LevelUnlockedBriefingItem(
            categoryLabel = "🛡️ Moyen Défensif (Palier Niv. 110)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🧬",
            title = "Gaine de Myéline (Sillage Isolant & Amortisseur)",
            badge = "Niv. 110+ • Durée 7s • -65% vitesse & 1 choc absorbé",
            badgeColor = Color(0xFF5EEAD4),
            coreColor = Color(0xFF0F766E),
            description = "Transforme votre Sillage Spirituel en barrière apaisante : tout obstacle qui le traverse voit sa vitesse réduite de 65 % et ses pulsations désamorcées pendant 4 s. Le premier impact direct subi est intégralement absorbé avec une micro-onde répulsive.",
            visualType = "POWERUP_MYELIN"
        )
        PowerUpType.CONTEMPLATIVE_PHASE_SHIFT -> LevelUnlockedBriefingItem(
            categoryLabel = "🛡️ Moyen Défensif (Palier Niv. 120)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "👻",
            title = "Déphasage Contemplatif (Intangibilité Thêta)",
            badge = "Niv. 120+ • Durée 4s • Intangibilité & Sanctuaires +25%",
            badgeColor = Color(0xFFDDD6FE),
            coreColor = Color(0xFF6D28D9),
            description = "Rend l'Étincelle de l'Esprit totalement intangible et translucide pour traverser tous les obstacles sans dégâts ni ralentissement. Traverser un obstacle divise sa taille et sa vitesse par deux et accélère de +25 % les Sanctuaires d'Harmonisation proches.",
            visualType = "POWERUP_PHASE"
        )
        PowerUpType.PLENITUDE_ARK -> LevelUnlockedBriefingItem(
            categoryLabel = "🛡️ Moyen Défensif (Palier Niv. 130)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🏛️",
            title = "Arche de Plénitude (Sanctuaire Nomade)",
            badge = "Niv. 130+ • Durée 8s • Dôme répulsif & Harmonisation x2",
            badgeColor = Color(0xFFFDE047),
            coreColor = EmeraldNova,
            description = "Déploie autour de votre sphère un Sanctuaire d'Harmonisation mobile (anneau d'or et d'émeraude) qui repousse automatiquement tous les obstacles mentaux à sa bordure, recueille à distance les Pensées Pures touchées par le dôme et harmonise les reliques du Fruit de l'Esprit 2x plus vite.",
            visualType = "POWERUP_ARK"
        )
        else -> LevelUnlockedBriefingItem(
            categoryLabel = "🛡️ Bonus & Défense Spatiale",
            isNewlyUnlockedThisLevel = isNew,
            emoji = type.emoji,
            title = type.displayName,
            badge = type.description,
            badgeColor = type.glowColor,
            coreColor = type.primaryColor,
            description = when (type) {
                PowerUpType.SHIELD -> "Restaure un cœur de bouclier (+1 ❤️) et enveloppe la sphère d'une aura protectrice temporaire (4s)."
                PowerUpType.EMP_BLAST -> "Libère une onde de clarté qui dissipe instantanément tous les obstacles mentaux dans un large rayon (+50 pts par obstacle)."
                PowerUpType.ANTI_ASTEROID, PowerUpType.ANTI_DRONE, PowerUpType.ANTI_MINE ->
                    "Permet de détruire au simple contact l'obstacle mental correspondant pendant 12 secondes (+60 à +100 pts par obstacle dissipé)."
                else -> "L'Hyper-Propulsion accélère votre sphère de 50 %. L'Aimant Cosmique attire automatiquement les méditations et les Pensées Pures à grande distance."
            },
            visualType = "POWERUP_GENERIC"
        )
    }
}

private fun neuronCorruptionToBriefingItem(
    type: NeuronCorruptionType,
    currentLevel: Int
): LevelUnlockedBriefingItem {
    val isNew = type.unlockLevel == currentLevel
    return when (type) {
        NeuronCorruptionType.COLERE_PEUR -> LevelUnlockedBriefingItem(
            categoryLabel = "🧠 Neurone Infecté & Remède de Vertu",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🔥",
            title = "Neurone de Colère / Peur → Remède : « PAIX » 🕊️",
            badge = "100% Paix • 50% Maîtrise",
            badgeColor = CyberCyanGlow,
            coreColor = type.infectedCoreColor,
            description = "Neurone prisonnier d'une aura rouge sombre abrasive (Colère, Peur, Angoisse, Fureur). Le projectile « Paix » le purifie instantanément (100%), tandis que « Maîtrise de soi » l'apaise partiellement (50%).",
            visualType = "NEURON"
        )
        NeuronCorruptionType.TRISTESSE -> LevelUnlockedBriefingItem(
            categoryLabel = "🧠 Neurone Infecté & Remède de Vertu",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "💧",
            title = "Neurone de Tristesse / Lourdeur → Remède : « JOIE » ☀️",
            badge = "100% Joie • 50% Amour",
            badgeColor = SolarGoldGlow,
            coreColor = type.infectedCoreColor,
            description = "Neurone alourdi par une teinte indigo/violacée terne (Tristesse, Chagrin, Accablement). Le mot « Joie » dissipe sa lourdeur (100%), et « Amour » offre une résonance partielle (50%).",
            visualType = "NEURON"
        )
        NeuronCorruptionType.AGITATION -> LevelUnlockedBriefingItem(
            categoryLabel = "🧠 Neurone Infecté & Remède de Vertu",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🌀",
            title = "Neurone d'Agitation / Égarement → Remède : « MAÎTRISE DE SOI » ⚖️",
            badge = "100% Maîtrise • 50% Paix",
            badgeColor = EmeraldNovaGlow,
            coreColor = type.infectedCoreColor,
            description = "Neurone déchaîné aux mouvements erratiques (Agitation, Égarement, Trouble). « Maîtrise de soi » le purifie totalement (100%), et « Paix » stabilise sa trajectoire à 50%.",
            visualType = "NEURON"
        )
        NeuronCorruptionType.RANCOEUR -> LevelUnlockedBriefingItem(
            categoryLabel = "🧠 Neurone Infecté & Remède de Vertu",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "💔",
            title = "Neurone de Rancœur / Froideur → Remède : « AMOUR » ♥",
            badge = "100% Amour • 50% Paix",
            badgeColor = PlasmaRoseGlow,
            coreColor = type.infectedCoreColor,
            description = "Neurone figé dans une gangue cramoisie glaciale (Rancœur, Haine, Mépris, Froideur). Le mot « Amour » fait fondre sa dureté (100%), et « Paix » l'adoucit partiellement (50%).",
            visualType = "NEURON"
        )
        NeuronCorruptionType.SURTENSION -> LevelUnlockedBriefingItem(
            categoryLabel = "🧠 Neurone & Remède Débloqué (Niv. 30)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🌬️",
            title = "Niv. 30 • Surtension & Hyperventilation → « SOUFFLE VAGAL »",
            badge = "100% Régulation & Souffle • Stase 3s",
            badgeColor = Color(0xFF38BDF8),
            coreColor = type.infectedCoreColor,
            description = "Frein vagal & cohérence parasympathique : purifie à 100 % les neurones de tension aiguë (branches synaptiques contractées) et libère une onde circulaire qui réinitialise les Foyers d'Angoisse et fige les Crises de Panique pendant 3 secondes.",
            visualType = "NEURON"
        )
        NeuronCorruptionType.APATHIE -> LevelUnlockedBriefingItem(
            categoryLabel = "🧠 Neurone & Remède Débloqué (Niv. 50)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🔥",
            title = "Niv. 50 • Apathie & Procrastination → « ÉLAN VITAL »",
            badge = "100% Élan & Action • Anti-Bourbier",
            badgeColor = Color(0xFFFB923C),
            coreColor = type.infectedCoreColor,
            description = "Dopamine & levée de sidération : purifie à 100 % les neurones en état d'apathie, dissout instantanément la viscosité d'un Bourbier Mental traversé et confère un micro-sprint polarisé vers la Pensée Pure la plus proche.",
            visualType = "NEURON"
        )
        NeuronCorruptionType.RIGIDITE_OBSESSION -> LevelUnlockedBriefingItem(
            categoryLabel = "🧠 Neurone & Remède Débloqué (Niv. 60)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🔄",
            title = "Niv. 60 • Rigidité Mentale & Obsession → « RECADRAGE »",
            badge = "100% Plasticité & Recadrage • +1 ❤️ Miroir",
            badgeColor = Color(0xFF818CF8),
            coreColor = type.infectedCoreColor,
            description = "Neuroplasticité préfrontale : purifie à 100 % les neurones d'obsession rigide, inverse l'onde du Miroir de Narcisse pour restaurer +1 bouclier, et se démultiplie en 2 faisceaux à tête chercheuse sur un Prisme Dogmatique.",
            visualType = "NEURON"
        )
        NeuronCorruptionType.BURNOUT_DISSOCIATION -> LevelUnlockedBriefingItem(
            categoryLabel = "🧠 Neurone & Remède Débloqué (Niv. 80)",
            isNewlyUnlockedThisLevel = isNew,
            emoji = "🌿",
            title = "Niv. 80 • Burnout & Dissociation → « ALIGNEMENT »",
            badge = "100% Homéostasie & Présence • Champ 6s",
            badgeColor = Color(0xFF34D399),
            coreColor = type.infectedCoreColor,
            description = "Ancrage intéroceptif : génère un champ harmonique autour de la sphère (6s) qui purifie graduellement tous les neurones touchés sans coût supplémentaire et neutralise tous les ralentissements et brouillages radar.",
            visualType = "NEURON"
        )
    }
}

private fun buildLevelUnlockedBriefingItems(gameState: ActiveGameState): List<LevelUnlockedBriefingItem> {
    val levelNumber = gameState.level.levelNumber
    val items = mutableListOf<LevelUnlockedBriefingItem>()
    val seenTitles = mutableSetOf<String>()

    fun addUnique(item: LevelUnlockedBriefingItem?) {
        if (item != null && seenTitles.add(item.title)) {
            items.add(item)
        }
    }

    // Uniquement les éléments nouvellement débloqués à ce niveau précis
    // (ne jamais re-présenter les éléments qui étaient déjà présents dans le niveau inférieur)
    SimpleObstacleVariant.entries
        .filter { it.unlockLevel == levelNumber }
        .forEach { addUnique(simpleVariantToBriefingItem(it, levelNumber)) }

    ObstacleType.entries
        .filter { it.unlockLevel >= 20 && it.unlockLevel == levelNumber }
        .forEach { addUnique(specialObstacleToBriefingItem(it, levelNumber)) }

    PowerUpType.entries
        .filter { it.unlockLevel >= 40 && it.unlockLevel == levelNumber }
        .forEach { addUnique(powerUpToBriefingItem(it, levelNumber)) }

    NeuronCorruptionType.entries
        .filter { it.unlockLevel > 1 && it.unlockLevel == levelNumber }
        .forEach { addUnique(neuronCorruptionToBriefingItem(it, levelNumber)) }

    if (levelNumber == 100) {
        addUnique(
            LevelUnlockedBriefingItem(
                categoryLabel = "⚡ Remède de Vertu Débloqué (Niv. 100)",
                isNewlyUnlockedThisLevel = true,
                emoji = "⚡",
                title = "Niv. 100 • Influx Bioélectrique → « POTENTIEL D'ACTION »",
                badge = "Arc en chaîne (3 neurones) • Court-circuit 4s",
                badgeColor = Color(0xFF38BDF8),
                coreColor = Color(0xFF0369A1),
                description = "Dépolarisation synaptique : libère un arc bioélectrique qui saute instantanément aux 2 ou 3 neurones infectés les plus proches (purification 100 % en cascade) et surcharge les Prismes ou Foyers d'Angoisse pendant 4 secondes.",
                visualType = "NEURON"
            )
        )
    }
    if (levelNumber == 120) {
        addUnique(
            LevelUnlockedBriefingItem(
                categoryLabel = "🌊 Remède de Vertu Débloqué (Niv. 120)",
                isNewlyUnlockedThisLevel = true,
                emoji = "🌊",
                title = "Niv. 120 • Résonance Gamma → « SYNCHRONIE »",
                badge = "Onde concentrique (1.8x) • +50% & Dissipation",
                badgeColor = Color(0xFFFDE047),
                coreColor = Color(0xFF7E22CE),
                description = "Onde cérébrale Gamma & marée glymphatique : déploie une vague sinusoïdale concentrique à grand rayon (1.8x écran) qui confère +50 % de purification à chaque neurone traversé et dissipe immédiatement les Voiles d'Obscurantisme et Pensées Parasites.",
                visualType = "NEURON"
            )
        )
    }

    return items
}

@Composable
private fun BriefingElementVisualPreview(
    item: LevelUnlockedBriefingItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(92.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SpaceBackground),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension * 0.34f
            // Halo de fond
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        item.badgeColor.copy(alpha = 0.34f),
                        item.coreColor.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = c,
                    radius = r * 1.45f
                ),
                radius = r * 1.45f,
                center = c
            )

            when (item.visualType) {
                "PARASITE" -> {
                    drawCircle(color = item.badgeColor.copy(alpha = 0.3f), radius = r * 1.35f, center = c)
                    for (i in 0 until 6) {
                        val ang = i * 1.047f
                        val lx = c.x + kotlin.math.cos(ang) * r * 0.48f
                        val ly = c.y + kotlin.math.sin(ang) * r * 0.48f
                        drawCircle(color = item.coreColor, radius = r * 0.62f, center = Offset(lx, ly))
                        drawCircle(color = item.badgeColor, radius = r * 0.62f, center = Offset(lx, ly), style = Stroke(width = 2.dp.toPx()))
                    }
                    drawCircle(color = Color.White.copy(alpha = 0.85f), radius = r * 0.24f, center = c)
                }
                "ANGOISSE" -> {
                    drawCircle(color = item.badgeColor.copy(alpha = 0.25f), radius = r * 1.4f, center = c)
                    for (i in 0 until 4) {
                        val ang = i * 0.785f
                        val dx = kotlin.math.cos(ang) * r * 1.35f
                        val dy = kotlin.math.sin(ang) * r * 1.35f
                        drawLine(
                            color = item.badgeColor,
                            start = Offset(c.x - dx, c.y - dy),
                            end = Offset(c.x + dx, c.y + dy),
                            strokeWidth = 2.5.dp.toPx()
                        )
                    }
                    drawCircle(color = item.coreColor, radius = r * 0.68f, center = c)
                    drawCircle(color = item.badgeColor, radius = r * 0.68f, center = c, style = Stroke(width = 2.dp.toPx()))
                    drawCircle(color = Color.White, radius = r * 0.26f, center = c)
                }
                "PANIQUE" -> {
                    drawCircle(
                        color = item.badgeColor.copy(alpha = 0.55f),
                        radius = r * 1.35f,
                        center = c,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = SolarGoldGlow.copy(alpha = 0.8f),
                        radius = r * 0.98f,
                        center = c,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    drawCircle(color = item.coreColor, radius = r * 0.58f, center = c)
                    drawCircle(color = item.badgeColor, radius = r * 0.58f, center = c, style = Stroke(width = 2.dp.toPx()))
                }
                "CONFORMISME" -> {
                    val step = r * 0.65f
                    for (row in -1..1) {
                        for (col in -1..1) {
                            val ux = c.x + col * step
                            val uy = c.y + row * step
                            drawCircle(color = Color(0xFF475569), radius = r * 0.26f, center = Offset(ux, uy))
                            drawCircle(color = Color(0xFF94A3B8), radius = r * 0.26f, center = Offset(ux, uy), style = Stroke(width = 1.5.dp.toPx()))
                        }
                    }
                }
                "PRISME" -> {
                    drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.28f), radius = r * 1.3f, center = c)
                    drawCircle(color = Color(0xFF0F172A), radius = r * 0.95f, center = c)
                    drawCircle(color = Color(0xFFE0F2FE), radius = r * 0.95f, center = c, style = Stroke(width = 2.5.dp.toPx()))
                    drawLine(color = Color(0xFF38BDF8), start = Offset(c.x - r, c.y), end = Offset(c.x + r, c.y), strokeWidth = 2.dp.toPx())
                    drawLine(color = Color(0xFF38BDF8), start = Offset(c.x, c.y - r), end = Offset(c.x, c.y + r), strokeWidth = 2.dp.toPx())
                }
                "OBSCURANTISME" -> {
                    drawCircle(color = Color(0xFF020617), radius = r * 1.35f, center = c)
                    drawCircle(color = Color(0xFF475569), radius = r * 1.15f, center = c, style = Stroke(width = 2.dp.toPx()))
                    drawCircle(color = Color(0xFF64748B), radius = r * 0.28f, center = Offset(c.x - r * 0.35f, c.y - r * 0.2f))
                    drawCircle(color = Color(0xFF64748B), radius = r * 0.22f, center = Offset(c.x + r * 0.35f, c.y + r * 0.25f))
                }
                "BOURBIER" -> {
                    drawCircle(color = Color(0xFF334155).copy(alpha = 0.75f), radius = r * 1.3f, center = c)
                    drawCircle(color = Color(0xFF94A3B8), radius = r * 1.1f, center = c, style = Stroke(width = 2.dp.toPx()))
                    drawCircle(color = Color(0xFF64748B), radius = r * 0.65f, center = c, style = Stroke(width = 2.dp.toPx()))
                    drawCircle(color = Color(0xFFCBD5E1), radius = r * 0.25f, center = c)
                }
                "NARCISSE" -> {
                    drawCircle(color = SolarGoldGlow.copy(alpha = 0.38f), radius = r * 1.35f, center = c)
                    drawCircle(color = PlasmaRose, radius = r * 0.78f, center = c)
                    drawCircle(color = SolarGoldGlow, radius = r * 0.78f, center = c, style = Stroke(width = 2.5.dp.toPx()))
                    drawCircle(color = Color.White, radius = r * 0.28f, center = c)
                }
                "NEURON" -> {
                    // Dendrites synaptiques + soma neuronal
                    for (i in 0 until 6) {
                        val ang = i * 1.047f + 0.3f
                        val ex = c.x + kotlin.math.cos(ang) * r * 1.32f
                        val ey = c.y + kotlin.math.sin(ang) * r * 1.32f
                        drawLine(
                            color = item.badgeColor.copy(alpha = 0.85f),
                            start = c,
                            end = Offset(ex, ey),
                            strokeWidth = 2.2.dp.toPx()
                        )
                        drawCircle(color = item.badgeColor, radius = r * 0.16f, center = Offset(ex, ey))
                    }
                    drawCircle(color = item.coreColor, radius = r * 0.68f, center = c)
                    drawCircle(color = item.badgeColor, radius = r * 0.68f, center = c, style = Stroke(width = 2.2.dp.toPx()))
                }
                else -> {
                    // Moyens défensifs & Power-Ups (anneaux de bouclier/dôme lumineux)
                    drawCircle(
                        color = item.badgeColor.copy(alpha = 0.85f),
                        radius = r * 1.25f,
                        center = c,
                        style = Stroke(width = 2.5.dp.toPx())
                    )
                    drawCircle(
                        color = item.coreColor.copy(alpha = 0.75f),
                        radius = r * 0.85f,
                        center = c
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.9f),
                        radius = r * 0.85f,
                        center = c,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
            }
        }
        // Emoji central bien lisible par-dessus le rendu graphique
        Surface(
            color = SpaceBackground.copy(alpha = 0.68f),
            shape = CircleShape,
            border = BorderStroke(1.dp, item.badgeColor.copy(alpha = 0.75f))
        ) {
            Text(
                text = item.emoji,
                fontSize = 24.sp,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

@Composable
internal fun MissionStartOverlay(
    gameState: ActiveGameState,
    levelName: String,
    situationTitle: String = "",
    sphereColor: SphereColorOption,
    onStart: () -> Unit,
    onOpenGlossary: () -> Unit = {}
) {
    val briefingItems = remember(gameState.level.levelNumber, gameState.obstacles.size) {
        buildLevelUnlockedBriefingItems(gameState)
    }
    var currentStepIndex by remember(gameState.level.levelNumber) { mutableStateOf(0) }
    val safeIndex = currentStepIndex.coerceIn(0, (briefingItems.size - 1).coerceAtLeast(0))
    val currentItem = briefingItems.getOrNull(safeIndex)
    val isLastStep = briefingItems.isEmpty() || safeIndex >= briefingItems.lastIndex

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground.copy(alpha = 0.82f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 440.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(1.5.dp, currentItem?.badgeColor ?: sphereColor.primaryColor)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = levelName,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = StarlightWhite,
                    textAlign = TextAlign.Center
                )

                if (situationTitle.isNotBlank()) {
                    Surface(
                        color = sphereColor.primaryColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, sphereColor.glowColor.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🕊️", fontSize = 15.sp)
                            Text(
                                text = "Traversée : $situationTitle",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                color = sphereColor.glowColor
                            )
                        }
                    }
                }

                // Présentation un à un des obstacles et éléments débloqués (Image + Glossaire)
                if (currentItem != null) {
                    Surface(
                        color = SpaceSurfaceVariant.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.5.dp, currentItem.badgeColor.copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // En-tête de progression : Élément X / N + badge Nouveau ou Débloqué
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (currentItem.isNewlyUnlockedThisLevel) {
                                        SolarGoldGlow.copy(alpha = 0.22f)
                                    } else {
                                        currentItem.badgeColor.copy(alpha = 0.18f)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (currentItem.isNewlyUnlockedThisLevel) SolarGoldGlow else currentItem.badgeColor
                                    )
                                ) {
                                    Text(
                                        text = if (currentItem.isNewlyUnlockedThisLevel) {
                                            "🆕 NOUVEAU DÉBLOCAGE • NIV. ${gameState.level.levelNumber}"
                                        } else {
                                            "🔓 ÉLÉMENT DÉBLOQUÉ & PRÉSENT"
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 10.sp
                                        ),
                                        color = if (currentItem.isNewlyUnlockedThisLevel) SolarGoldGlow else currentItem.badgeColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }

                                Surface(
                                    color = SpaceBackground,
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, SpaceCardBorder)
                                ) {
                                    Text(
                                        text = "${safeIndex + 1} / ${briefingItems.size}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = OrbitronFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = StarlightWhite,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Points de progression visuels (un à un)
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                briefingItems.forEachIndexed { idx, item ->
                                    Box(
                                        modifier = Modifier
                                            .height(6.dp)
                                            .width(if (idx == safeIndex) 24.dp else 8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (idx == safeIndex) item.badgeColor
                                                else if (idx < safeIndex) item.badgeColor.copy(alpha = 0.5f)
                                                else NebulaGray.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }

                            Text(
                                text = currentItem.categoryLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = currentItem.badgeColor,
                                textAlign = TextAlign.Center
                            )

                            // Image graphique Canvas de l'obstacle / élément débloqué
                            Surface(
                                color = SpaceBackground,
                                shape = RoundedCornerShape(22.dp),
                                border = BorderStroke(1.5.dp, currentItem.badgeColor.copy(alpha = 0.65f))
                            ) {
                                BriefingElementVisualPreview(
                                    item = currentItem,
                                    modifier = Modifier.padding(6.dp)
                                )
                            }

                            // Titre du Glossaire
                            Text(
                                text = currentItem.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = StarlightWhite,
                                textAlign = TextAlign.Center
                            )

                            // Badge d'effet du Glossaire
                            Surface(
                                color = currentItem.badgeColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, currentItem.badgeColor.copy(alpha = 0.55f))
                            ) {
                                Text(
                                    text = currentItem.badge,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = currentItem.badgeColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            // Ce que dit le Glossaire
                            Surface(
                                color = SpaceBackground.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SpaceCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "📖 CE QUE DIT LE GLOSSAIRE :",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = OrbitronFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        ),
                                        color = SolarGoldGlow
                                    )
                                    Text(
                                        text = currentItem.description,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = StarlightWhite.copy(alpha = 0.92f)
                                    )
                                }
                            }
                        }
                    }

                    // Boutons de navigation "Un à un, le joueur cliquera sur Suivant"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (safeIndex > 0) {
                            OutlinedButton(
                                onClick = {
                                    AstroSoundManager.playUiTap()
                                    currentStepIndex = (safeIndex - 1).coerceAtLeast(0)
                                },
                                modifier = Modifier
                                    .weight(0.42f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("mission_briefing_prev_button"),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, NebulaGray.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "⬅ Préc.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = StarlightWhite
                                )
                            }
                        }

                        if (!isLastStep) {
                            Button(
                                onClick = {
                                    AstroSoundManager.playUiTap()
                                    currentStepIndex = (safeIndex + 1).coerceAtMost(briefingItems.lastIndex)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .astroTouchEffect(glowColor = currentItem.badgeColor, cornerRadius = 14.dp)
                                    .testTag("mission_briefing_next_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = currentItem.badgeColor,
                                    contentColor = SpaceBackground
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    text = "SUIVANT (${safeIndex + 1}/${briefingItems.size}) ➔",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        } else {
                            Button(
                                onClick = onStart,
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .astroTouchEffect(glowColor = sphereColor.glowColor, cornerRadius = 14.dp)
                                    .testTag("start_mission_overlay_button"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = sphereColor.primaryColor,
                                    contentColor = SpaceBackground
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ENTRER DANS L'ESPACE",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Conditions pour passer au niveau suivant : récupérer tous les fragments, le Texte Bonus 🧠 et purifier au moins la moitié des neurones infectés.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NebulaGray,
                        textAlign = TextAlign.Center
                    )

                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = sphereColor.glowColor, cornerRadius = 14.dp)
                            .testTag("start_mission_overlay_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = sphereColor.primaryColor,
                            contentColor = SpaceBackground
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ENTRER DANS L'ESPACE",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                }

                // Si le joueur n'est pas encore au dernier élément, bouton secondaire pour lancer directement (et compatibilité testTag)
                if (!isLastStep) {
                    OutlinedButton(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize()
                            .testTag("start_mission_overlay_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, sphereColor.primaryColor.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = sphereColor.glowColor
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Entrer directement dans l'espace (Niv. ${gameState.level.levelNumber})",
                            style = MaterialTheme.typography.labelMedium,
                            color = sphereColor.glowColor
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenGlossary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = VioletPulsarGlow, cornerRadius = 14.dp)
                        .testTag("mission_start_glossary_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, VioletPulsarGlow.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = "📖 Glossaire complet de tous les éléments",
                        style = MaterialTheme.typography.labelMedium,
                        color = StarlightWhite
                    )
                }
            }
        }
    }
}

@Composable
internal fun PauseOverlay(
    levelNumber: Int,
    runScore: Int,
    sphereColor: SphereColorOption,
    onResume: () -> Unit,
    onOpenQuickInventory: () -> Unit,
    onOpenSettings: () -> Unit,
    onBackToHangar: () -> Unit,
    onOpenGlossary: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground.copy(alpha = 0.8f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 400.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(1.dp, SpaceCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "MISSION EN PAUSE",
                    style = MaterialTheme.typography.headlineMedium,
                    color = StarlightWhite
                )
                Text(
                    text = "Niveau $levelNumber • Score actuel : $runScore pts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SolarGoldGlow
                )

                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = sphereColor.glowColor, cornerRadius = 14.dp)
                        .testTag("resume_game_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sphereColor.primaryColor,
                        contentColor = SpaceBackground
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("REPRENDRE LA MISSION", style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = onOpenGlossary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = VioletPulsarGlow, cornerRadius = 14.dp)
                        .testTag("pause_glossary_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, VioletPulsarGlow)
                ) {
                    Text("📖 Glossaire des Éléments de la Carte", color = StarlightWhite)
                }

                OutlinedButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = sphereColor.glowColor, cornerRadius = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, sphereColor.primaryColor)
                ) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = sphereColor.glowColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Paramètres (Joystick)", color = StarlightWhite)
                }

                OutlinedButton(
                    onClick = onOpenQuickInventory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CyberCyan)
                ) {
                    Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = CyberCyanGlow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Inventaire des Méditations", color = StarlightWhite)
                }

                OutlinedButton(
                    onClick = onBackToHangar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = StarlightWhite, cornerRadius = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SpaceCardBorder)
                ) {
                    Text(stringResource(R.string.back_to_hangar_btn), color = NebulaGray)
                }
            }
        }
    }
}

@Composable
internal fun QuickInventoryOverlay(
    collectedInventory: List<CollectedTextEntity>,
    currentLevel: Int,
    onCloseAndResume: () -> Unit,
    onOpenFullInventory: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground.copy(alpha = 0.88f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .heightIn(max = 520.dp)
                .testTag("quick_inventory_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(1.5.dp, CyberCyan)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MÉDITATIONS DU FRUIT DE L'ESPRIT",
                            style = MaterialTheme.typography.titleLarge,
                            color = CyberCyanGlow
                        )
                        Text(
                            text = "${collectedInventory.size} méditation(s) sauvegardée(s)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NebulaGray
                        )
                    }
                    IconButton(
                        onClick = onCloseAndResume,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer l'inventaire rapide",
                            tint = StarlightWhite
                        )
                    }
                }

                if (collectedInventory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune méditation récupérée pour l'instant.\nApprochez votre boule des orbes du Fruit de l'Esprit dans l'espace !",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NebulaGray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(collectedInventory, key = { it.id }) { item ->
                            val fruitColor = MeditationsMatrix.getFruitGlowColor(item.letter)
                            Surface(
                                color = SpaceSurfaceVariant,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (item.levelNumber == currentLevel) fruitColor else SpaceCardBorder
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "[${MeditationsMatrix.getFruitEmoji(item.letter)} ${item.letter}] ${item.title}",
                                            style = MaterialTheme.typography.labelLarge,
                                            color = fruitColor
                                        )
                                        Text(
                                            text = "+${item.pointsAwarded} pts",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = SolarGoldGlow
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = item.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = NebulaGray
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenFullInventory,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 12.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Vue Complète", color = CyberCyanGlow)
                    }
                    Button(
                        onClick = onCloseAndResume,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = SpaceBackground
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reprendre", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
internal fun LevelCompleteOverlay(
    levelNumber: Int,
    sectorName: String,
    runScore: Int,
    bonusPoints: Int,
    relics: List<FloatingTextRelic>,
    brainRelic: FloatingTextRelic? = null,
    sphereColor: SphereColorOption,
    onNextLevel: () -> Unit,
    onConsultInventory: () -> Unit
) {
    LaunchedEffect(Unit) {
        AstroSoundManager.updateAmbientState(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .widthIn(max = 440.dp)
                .testTag("level_complete_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(2.dp, EmeraldNova)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.level_complete_title, levelNumber),
                    style = MaterialTheme.typography.headlineMedium,
                    color = EmeraldNovaGlow,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "$sectorName • Tous les fragments + Texte Bonus 🧠 + Neurones purifiés ! +$bonusPoints pts",
                    style = MaterialTheme.typography.labelMedium,
                    color = SolarGoldGlow,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Score de la mission : $runScore PTS",
                    style = MaterialTheme.typography.titleMedium,
                    color = StarlightWhite
                )

                var expandedLetter by remember { mutableStateOf<String?>(null) }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    relics.forEach { relic ->
                        val isExpanded = expandedLetter == relic.letter
                        val fruitColor = MeditationsMatrix.getFruitGlowColor(relic.fruitId)
                        Surface(
                            onClick = { expandedLetter = if (isExpanded) null else relic.letter },
                            color = SpaceSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isExpanded) fruitColor else SpaceCardBorder),
                            modifier = Modifier.astroTouchEffect(glowColor = fruitColor, cornerRadius = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✓ [${relic.fruitEmoji} ${relic.letter}] ${relic.title}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = fruitColor,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "+${relic.points} pts",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SolarGoldGlow
                                    )
                                }
                                if (isExpanded) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = relic.content,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NebulaGray
                                    )
                                }
                            }
                        }
                    }

                    // Brain Bonus fragment discovery status
                    brainRelic?.let { brain ->
                        val isCollected = brain.isCollected
                        Surface(
                            color = if (isCollected) PlasmaRoseGlow.copy(alpha = 0.22f) else SpaceSurfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isCollected) PlasmaRoseGlow else SpaceCardBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isCollected) "✓ 🧠 Fragment #${brain.brainLevel} recueilli !" else "🧠 Fragment #${brain.brainLevel} (non recueilli)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isCollected) PlasmaRoseGlow else NebulaGray
                                )
                                if (isCollected) {
                                    Text(
                                        text = "+${brain.points} pts",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = SolarGoldGlow
                                    )
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onNextLevel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = sphereColor.glowColor, cornerRadius = 14.dp)
                        .testTag("next_level_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sphereColor.primaryColor,
                        contentColor = SpaceBackground
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "${stringResource(R.string.next_level_btn)} (${levelNumber + 1})",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onConsultInventory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, CyberCyan)
                ) {
                    Icon(imageVector = Icons.Default.Inventory2, contentDescription = null, tint = CyberCyanGlow)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.consult_inventory_btn), color = StarlightWhite)
                }
            }
        }
    }
}

@Composable
internal fun GameOverOverlay(
    levelNumber: Int,
    runScore: Int,
    collectedCountInRun: Int,
    onRetry: () -> Unit,
    onBackToHangar: () -> Unit
) {
    LaunchedEffect(Unit) {
        AstroSoundManager.updateAmbientState(false)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBackground.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 400.dp)
                .testTag("game_over_modal"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(2.dp, DangerRed)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.game_over_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = DangerRed
                )
                Text(
                    text = "Niveau $levelNumber • Méditations récupérées : $collectedCountInRun / 3\n(Les méditations déjà touchées sont conservées dans votre inventaire !)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NebulaGray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Score : $runScore PTS",
                    style = MaterialTheme.typography.titleLarge,
                    color = SolarGoldGlow
                )

                Button(
                    onClick = onRetry,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = DangerRed, cornerRadius = 14.dp)
                        .testTag("retry_level_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.retry_level_btn), style = MaterialTheme.typography.labelLarge)
                }

                OutlinedButton(
                    onClick = onBackToHangar,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = StarlightWhite, cornerRadius = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, SpaceCardBorder)
                ) {
                    Text(stringResource(R.string.back_to_hangar_btn), color = StarlightWhite)
                }
            }
        }
    }
}

@Composable
internal fun EndMissionTargetsScoreDialog(
    levelNumber: Int,
    isVictory: Boolean,
    savedCount: Int,
    unsavedOnMapCount: Int,
    destroyedCount: Int,
    savedPoints: Int,
    lostPoints: Int,
    playerTotalBefore: Int,
    playerTotalAfter: Int,
    missionRunScore: Int,
    sphereColor: SphereColorOption,
    onDismiss: () -> Unit
) {
    val totalUnsavedCount = unsavedOnMapCount + destroyedCount
    val netPoints = savedPoints - lostPoints

    var displayedSavedPts by remember { mutableStateOf(0) }
    var displayedLostPts by remember { mutableStateOf(0) }
    var displayedNetPts by remember { mutableStateOf(0) }
    var displayedPlayerTotal by remember { mutableStateOf(playerTotalBefore) }
    var animStep by remember { mutableStateOf(0) } // 0 = rolling saved, 1 = rolling lost, 2 = done

    LaunchedEffect(savedPoints, lostPoints, playerTotalBefore, playerTotalAfter) {
        AstroSoundManager.updateAmbientState(false)
        displayedSavedPts = 0
        displayedLostPts = 0
        displayedNetPts = 0
        displayedPlayerTotal = playerTotalBefore
        animStep = 0

        delay(120L)

        // Phase 1: Fast-rolling counter for saved targets (+10 pts per saved target)
        if (savedPoints > 0) {
            val steps = 22
            for (s in 1..steps) {
                val frac = s.toFloat() / steps
                val curSaved = (savedPoints * frac).toInt()
                displayedSavedPts = curSaved
                displayedNetPts = curSaved
                displayedPlayerTotal = playerTotalBefore + curSaved
                AstroSoundManager.playScoreTick(isPositive = true)
                delay(18L)
            }
        }
        displayedSavedPts = savedPoints
        displayedNetPts = savedPoints
        displayedPlayerTotal = playerTotalBefore + savedPoints

        delay(180L)
        animStep = 1

        // Phase 2: Fast-rolling counter for unsaved targets (-20 pts per unsaved target)
        if (lostPoints > 0) {
            val steps = 26
            for (s in 1..steps) {
                val frac = s.toFloat() / steps
                val curLost = (lostPoints * frac).toInt()
                displayedLostPts = curLost
                displayedNetPts = savedPoints - curLost
                displayedPlayerTotal = (playerTotalBefore + savedPoints - curLost).coerceAtLeast(0)
                AstroSoundManager.playScoreTick(isPositive = false)
                delay(18L)
            }
        }
        displayedLostPts = lostPoints
        displayedNetPts = netPoints
        displayedPlayerTotal = playerTotalAfter
        animStep = 2
    }

    val borderColor = when {
        animStep == 1 && lostPoints > 0 -> DangerRed
        netPoints < 0 -> DangerRed
        else -> EmeraldNovaGlow
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 20.dp)
                .fillMaxWidth()
                .widthIn(max = 440.dp)
                .testTag("end_mission_targets_popup"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(2.dp, borderColor)
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header Badge
                Surface(
                    color = borderColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, borderColor.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = if (isVictory) "SECTEUR $levelNumber COMPLÉTÉ • BILAN DES PENSÉES PURES" else "FIN DE MISSION • BILAN DES PENSÉES PURES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = borderColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }

                Text(
                    text = if (totalUnsavedCount > 0) {
                        "Pensée pure non récupérée"
                    } else {
                        "Toutes les Pensées Pures récupérées !"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = OrbitronFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = if (totalUnsavedCount > 0) DangerRed else EmeraldNovaGlow,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "✨ +10 pts par Pensée Pure  •  ⚠️ -20 pts par Pensée pure non récupérée",
                    style = MaterialTheme.typography.labelSmall,
                    color = NebulaGray,
                    textAlign = TextAlign.Center
                )

                // 1. Pensées Pures Row (+10 pts / cible)
                Surface(
                    color = EmeraldNovaGlow.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (animStep == 0) EmeraldNovaGlow else EmeraldNovaGlow.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "✨ Pensées Pures ($savedCount)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = EmeraldNovaGlow
                            )
                            Text(
                                text = "$savedCount × +10 pts (Étincelles, Savoirs, Souvenirs)",
                                style = MaterialTheme.typography.labelSmall,
                                color = NebulaGray
                            )
                        }
                        Text(
                            text = "+$displayedSavedPts PTS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = EmeraldNovaGlow
                        )
                    }
                }

                // 2. Unsaved Pensées Pures Row (-20 pts)
                Surface(
                    color = if (totalUnsavedCount > 0) DangerRed.copy(alpha = 0.12f) else SpaceSurfaceVariant,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        when {
                            animStep == 1 && totalUnsavedCount > 0 -> DangerRed
                            totalUnsavedCount > 0 -> DangerRed.copy(alpha = 0.55f)
                            else -> SpaceCardBorder
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "💔 Pensée pure non récupérée ($totalUnsavedCount)",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (totalUnsavedCount > 0) DangerRed else NebulaGray
                            )
                            Text(
                                text = "$totalUnsavedCount × -20 pts ($unsavedOnMapCount sur la map • $destroyedCount détruite(s))",
                                style = MaterialTheme.typography.labelSmall,
                                color = NebulaGray
                            )
                        }
                        Text(
                            text = "-$displayedLostPts PTS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = if (totalUnsavedCount > 0) DangerRed else NebulaGray
                        )
                    }
                }

                // 3. Net Target Balance Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Impact net sur vos points :",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = StarlightWhite
                    )
                    val netText = if (displayedNetPts >= 0) "+$displayedNetPts PTS" else "$displayedNetPts PTS"
                    val netColor = when {
                        displayedNetPts > 0 -> EmeraldNovaGlow
                        displayedNetPts < 0 -> DangerRed
                        else -> SolarGoldGlow
                    }
                    Text(
                        text = netText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = OrbitronFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = netColor
                    )
                }

                // 4. Player Total Score Animated Counter Box
                Surface(
                    color = SpaceBackground.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.5.dp, SolarGoldGlow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "POINT TOTAL DU JOUEUR",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = SolarGoldGlow
                        )
                        Text(
                            text = "$displayedPlayerTotal PTS",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            ),
                            color = when {
                                animStep == 0 && savedPoints > 0 -> EmeraldNovaGlow
                                animStep == 1 && lostPoints > 0 -> DangerRed
                                else -> StarlightWhite
                            }
                        )
                        Text(
                            text = "Score de la mission ajusté : $missionRunScore PTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyanGlow
                        )
                    }
                }

                // 5. Bouton Continuer
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = sphereColor.glowColor, cornerRadius = 14.dp)
                        .testTag("end_mission_continue_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sphereColor.primaryColor,
                        contentColor = SpaceBackground
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Continuer",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun MapGlossaryDialog(
    sphereColor: SphereColorOption,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(0) }
    val categories = listOf(
        "Tous",
        "Obstacles",
        "Pensées & Neurones",
        "Reliques & Sphère",
        "Bonus"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 18.dp)
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .heightIn(max = 680.dp)
                .testTag("map_glossary_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface),
            border = BorderStroke(1.5.dp, VioletPulsarGlow)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "📖 GLOSSAIRE DE LA CARTE",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = StarlightWhite
                        )
                        Text(
                            text = "Guide complet de tous les éléments présents dans l'espace mental",
                            style = MaterialTheme.typography.bodySmall,
                            color = NebulaGray
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("close_glossary_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer le glossaire",
                            tint = StarlightWhite
                        )
                    }
                }

                // Category Filter Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories.size) { index ->
                        val isSelected = selectedCategory == index
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedCategory = index }
                                .testTag("glossary_tab_$index"),
                            color = if (isSelected) VioletPulsarGlow.copy(alpha = 0.25f) else SpaceSurfaceVariant,
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) VioletPulsarGlow else SpaceCardBorder
                            )
                        ) {
                            Text(
                                text = categories[index],
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) StarlightWhite else NebulaGray,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SECTION 1: LES OBSTACLES MENTAUX (Niv. 1..9 : 3 de base • Niv. 10+ : +1 obstacle simple tous les 5 niveaux • Tous les 20 niveaux : +1 obstacle spécial)
                    if (selectedCategory == 0 || selectedCategory == 1) {
                        item {
                            GlossarySectionHeader(
                                title = "⚠️ LES OBSTACLES MENTAUX (NIV. 1 À 135)",
                                subtitle = "Niv. 1 à 9 : 3 obstacles simples de base • Dès le Niv. 10 : +1 nouvel obstacle simple tous les 5 niveaux • Tous les 20 niveaux : +1 obstacle spécial",
                                accentColor = DangerRed
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🦠",
                                title = "Pensée Parasite (ou Toxine Cérébrale)",
                                badge = "Niv. 1+ • Obstacle lourd & erratique",
                                badgeColor = Color(0xFFA855F7),
                                description = "Masse organique sombre et toxique aux lobes vénéneux qui dérive lourdement dans l'espace mental. Écrase les Pensées Pures sur son passage.\n• Remède / Bonus : 🌿 Dissipateur Parasite (+60 pts).",
                                visualType = "PARASITE"
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "⚡",
                                title = "Foyer d'Angoisse (ou Pic de Stress)",
                                badge = "Niv. 1+ • Obstacle traqueur à faisceau",
                                badgeColor = DangerRed,
                                description = "Noyau de haute tension nerveuse projetant des aiguilles et faisceaux rotatifs d'anxiété (Pics de Stress) qui balaient l'espace.\n• Remède / Bonus : 💠 Sérénité Anti-Angoisse (+80 pts).",
                                visualType = "ANGOISSE"
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "💥",
                                title = "Crise de Panique (ou Décharge Émotive)",
                                badge = "Niv. 1+ • Obstacle explosif à pulsations",
                                badgeColor = Color(0xFFFB7185),
                                description = "Cœur émotionnel instable qui émet des ondes de choc électriques concentriques (Décharges Émotives) à haute fréquence.\n• Remède / Bonus : 🌊 Calme Anti-Panique (+100 pts).",
                                visualType = "PANIQUE"
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🐑",
                                title = "Conformisme (L'esprit « Mouton ») : La Nuée Grégaire",
                                badge = "Niv. 20+ • Absorbe les bonus & ralentit",
                                badgeColor = Color(0xFF94A3B8),
                                description = "Un essaim d'entités grises, ternes et parfaitement identiques qui se déplacent en rangs serrés, bloquant de larges zones de la carte. Elles n'infligent pas de dégâts directs, mais si votre sphère les traverse, elles absorbent vos capacités spéciales (Hyper-Propulsion, Aimant Cosmique...) et vous ralentissent pour vous forcer à « rentrer dans le rang ».",
                                visualType = "CONFORMISME"
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "💎",
                                title = "Endoctrinement : Le Prisme Dogmatique",
                                badge = "Niv. 40+ • Reflète les vertus classiques",
                                badgeColor = Color(0xFF38BDF8),
                                description = "Structure géométrique froide, rigide et inébranlable (monolithe aux arêtes tranchantes) qui contraste avec les formes organiques des Pensées Parasites. Il reflète ou dévie les projectiles de vertu classiques (Paix, Joie, Amour).\n• Pour le briser : utilisez une 💥 Onde Nova (EMP) ou projetez la vertu « Vérité & Discernement » 🔍 (+90 pts).",
                                visualType = "PRISME"
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🌑",
                                title = "L'Ignorance : Le Voile d'Obscurantisme",
                                badge = "Niv. 60+ • Brouille la mini-carte & cache les Pensées",
                                badgeColor = Color(0xFF64748B),
                                description = "Une nébuleuse d'encre noire et un brouillard statique très dense qui s'étend sur l'espace mental. Lorsqu'il est présent, il brouille les capteurs (masque les éléments de la mini-carte) et dissimule visuellement les Étincelles de Lucidité et Fragments de Savoir situés à l'intérieur.\n• Pour le dissiper : tirez un mot de vertu dedans (+70 pts).",
                                visualType = "OBSCURANTISME"
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🕸️",
                                title = "Incapacité : Le Bourbier Mental",
                                badge = "Niv. 80+ • Divise la vitesse par 3",
                                badgeColor = Color(0xFF94A3B8),
                                description = "Un champ de force poisseux et une anomalie gravitationnelle visqueuse aux teintes grisâtres. Entrer dans ce champ divise la vitesse de votre sphère par trois, rendant l'esquive presque impossible.\n• Pour s'en arracher sans pénalité : utilisez un bonus ⚡ Hyper-Propulsion.",
                                visualType = "BOURBIER"
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🪞",
                                title = "L'Ego : Le Miroir de Narcisse",
                                badge = "Niv. 100+ • Aimant inversé & faux bonus",
                                badgeColor = SolarGoldGlow,
                                description = "Un astre artificiellement brillant et scintillant qui imite l'apparence des bonus ou des cœurs. Il agit comme un Aimant Cosmique inversé : il attire inexorablement votre sphère vers lui. Si vous le touchez, au lieu de vous soigner, il draine un de vos boucliers (-1 ❤️) ou consomme vos points d'énergie de purification.",
                                visualType = "NARCISSE"
                            )
                        }

                        // Les 4 familles d'obstacles simples débloqués tous les 5 niveaux à partir du Niveau 10 (Niv. 10 à 135)
                        val thematicCategories = SimpleObstacleCategory.entries.filter {
                            it != SimpleObstacleCategory.BASE_NEURO
                        }
                        thematicCategories.forEach { cat ->
                            item(key = "cat_header_${cat.name}") {
                                GlossarySectionHeader(
                                    title = cat.title,
                                    subtitle = cat.subtitle,
                                    accentColor = cat.accentColor
                                )
                            }
                            val variantsInCat = SimpleObstacleVariant.entries.filter { it.category == cat }
                            items(
                                items = variantsInCat,
                                key = { "simple_obs_${it.name}" }
                            ) { variant ->
                                val previewType = when (variant.baseArchetype) {
                                    ObstacleType.ASTEROID -> "PARASITE"
                                    ObstacleType.LASER_DRONE -> "ANGOISSE"
                                    ObstacleType.PULSAR_MINE -> "PANIQUE"
                                    else -> null
                                }
                                GlossaryElementCard(
                                    emoji = variant.emoji,
                                    title = "Niv. ${variant.unlockLevel} • ${variant.displayName}",
                                    badge = "Niv. ${variant.unlockLevel}+ • Collision directe (-1 ❤️) • ${variant.counterPowerUpLabel}",
                                    badgeColor = cat.accentColor,
                                    description = "${variant.definition}\n• Menace directe contre les boucliers par collision physique.\n• Neutralisation : ${variant.counterPowerUpLabel} ou 💥 Onde Nova.",
                                    visualType = previewType
                                )
                            }
                        }
                    }

                    // SECTION 2: PENSÉES PURES & NEURONES INFECTÉS
                    if (selectedCategory == 0 || selectedCategory == 2) {
                        item {
                            GlossarySectionHeader(
                                title = "✨ LES PENSÉES PURES À SAUVER",
                                subtitle = "+10 pts & +10 énergie de purification par Pensée Pure • -20 pts si non récupérée en fin de mission • 50 sauvées = +1 ❤️",
                                accentColor = SolarGoldGlow
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "✨",
                                title = "Étincelle de Lucidité",
                                badge = "+10 pts • Énergie +10",
                                badgeColor = SolarGoldGlow,
                                description = "Petite orbe lumineuse très vive représentant une idée brillante sur le point d'éclore (Intuition, Clarté, Éveil). Sauvez-la avant qu'un obstacle ne la percute !"
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🔶",
                                title = "Fragment de Savoir",
                                badge = "+10 pts • Énergie +10",
                                badgeColor = CyberCyanGlow,
                                description = "Cristal géométrique doré et cyan contenant l'essence de la connaissance pure (Sagesse, Vérité, Discernement)."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🫧",
                                title = "Bulle de Souvenir",
                                badge = "+10 pts • Fragile",
                                badgeColor = VioletPulsarGlow,
                                description = "Bulle irisée fragile abritant un souvenir précieux (Promesse, Tendresse, Gratitude). Sa destruction par une Pensée Parasite est particulièrement tragique."
                            )
                        }

                        item {
                            GlossarySectionHeader(
                                title = "🧠 LES NEURONES INFECTÉS & PURIFICATION PAR RÉSONANCE",
                                subtitle = "Purifiés progressivement par les Mots de Vertu projetés grâce aux points des Pensées Pures (+10 pts, 50 purifiés = +1 ❤️)",
                                accentColor = EmeraldNovaGlow
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🔥",
                                title = "Neurone de Colère / Peur → Remède : « PAIX » 🕊️",
                                badge = "100% Paix • 50% Maîtrise",
                                badgeColor = CyberCyanGlow,
                                description = "Neurone prisonnier d'une aura rouge sombre abrasive (Colère, Peur, Angoisse, Fureur). Le projectile « Paix » le purifie instantanément (100%), tandis que « Maîtrise de soi » l'apaise partiellement (50%)."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "💧",
                                title = "Neurone de Tristesse / Lourdeur → Remède : « JOIE » ☀️",
                                badge = "100% Joie • 50% Amour",
                                badgeColor = SolarGoldGlow,
                                description = "Neurone alourdi par une teinte indigo/violacée terne (Tristesse, Chagrin, Accablement). Le mot « Joie » dissipe sa lourdeur (100%), et « Amour » offre une résonance partielle (50%)."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🌀",
                                title = "Neurone d'Agitation / Égarement → Remède : « MAÎTRISE DE SOI » ⚖️",
                                badge = "100% Maîtrise • 50% Paix",
                                badgeColor = EmeraldNovaGlow,
                                description = "Neurone déchaîné aux mouvements erratiques (Agitation, Égarement, Trouble). « Maîtrise de soi » le purifie totalement (100%), et « Paix » stabilise sa trajectoire à 50%."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "💔",
                                title = "Neurone de Rancœur / Froideur → Remède : « AMOUR » ♥",
                                badge = "100% Amour • 50% Paix",
                                badgeColor = PlasmaRoseGlow,
                                description = "Neurone figé dans une gangue cramoisie glaciale (Rancœur, Haine, Mépris, Froideur). Le mot « Amour » fait fondre sa dureté (100%), et « Paix » l'adoucit partiellement (50%)."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🌬️",
                                title = "Niv. 30 • Surtension & Hyperventilation → « SOUFFLE VAGAL »",
                                badge = "100% Régulation & Souffle • Stase 3s",
                                badgeColor = Color(0xFF38BDF8),
                                description = "Frein vagal & cohérence parasympathique : purifie à 100 % les neurones de tension aiguë (branches synaptiques contractées) et libère une onde circulaire qui réinitialise les Foyers d'Angoisse et fige les Crises de Panique pendant 3 secondes."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🔥",
                                title = "Niv. 50 • Apathie & Procrastination → « ÉLAN VITAL »",
                                badge = "100% Élan & Action • Anti-Bourbier",
                                badgeColor = Color(0xFFFB923C),
                                description = "Dopamine & levée de sidération : purifie à 100 % les neurones en état d'apathie, dissout instantanément la viscosité d'un Bourbier Mental traversé et confère un micro-sprint polarisé vers la Pensée Pure la plus proche."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🔄",
                                title = "Niv. 60 • Rigidité Mentale & Obsession → « RECADRAGE »",
                                badge = "100% Plasticité & Recadrage • +1 ❤️ Miroir",
                                badgeColor = Color(0xFF818CF8),
                                description = "Neuroplasticité préfrontale : purifie à 100 % les neurones d'obsession rigide, inverse l'onde du Miroir de Narcisse pour restaurer +1 bouclier, et se démultiplie en 2 faisceaux à tête chercheuse sur un Prisme Dogmatique."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🌿",
                                title = "Niv. 80 • Burnout & Dissociation → « ALIGNEMENT »",
                                badge = "100% Homéostasie & Présence • Champ 6s",
                                badgeColor = Color(0xFF34D399),
                                description = "Ancrage intéroceptif : génère un champ harmonique autour de la sphère (6s) qui purifie graduellement tous les neurones touchés sans coût supplémentaire et neutralise tous les ralentissements et brouillages radar."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "⚡",
                                title = "Niv. 100 • Influx Bioélectrique → « POTENTIEL D'ACTION »",
                                badge = "Arc en chaîne (3 neurones) • Court-circuit 4s",
                                badgeColor = Color(0xFF38BDF8),
                                description = "Dépolarisation synaptique : libère un arc bioélectrique qui saute instantanément aux 2 ou 3 neurones infectés les plus proches (purification 100 % en cascade) et surcharge les Prismes ou Foyers d'Angoisse pendant 4 secondes."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🌊",
                                title = "Niv. 120 • Résonance Gamma → « SYNCHRONIE »",
                                badge = "Onde concentrique (1.8x) • +50% & Dissipation",
                                badgeColor = Color(0xFFFDE047),
                                description = "Onde cérébrale Gamma & marée glymphatique : déploie une vague sinusoïdale concentrique à grand rayon (1.8x écran) qui confère +50 % de purification à chaque neurone traversé et dissipe immédiatement les Voiles d'Obscurantisme et Pensées Parasites."
                            )
                        }
                    }

                    // SECTION 3: L'ÉTINCELLE DE L'ESPRIT & RELIQUES DU FRUIT DE L'ESPRIT
                    if (selectedCategory == 0 || selectedCategory == 3) {
                        item {
                            GlossarySectionHeader(
                                title = "✨ L'ÉTINCELLE DE L'ESPRIT & LES RELIQUES DE MÉDITATION",
                                subtitle = "Feu follet cosmique de conscience et méditations à harmoniser",
                                accentColor = sphereColor.glowColor
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "✨",
                                title = "L'Étincelle de l'Esprit (Noyau de Conscience & Couronne Éthérée)",
                                badge = "Joueur • 3 à 7 ❤️",
                                badgeColor = sphereColor.glowColor,
                                description = "Source d'énergie pure au noyau blanc incandescent qui respire au rythme du cosmos, entourée d'une couronne éthérée et d'un sillage cométaire. Elle s'embrase et s'étire en Hyper-Propulsion, déploie des anneaux d'or célestes sous l'Aura de Paix, et vacille lorsqu'elle traverse un Bourbier Mental."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🧠",
                                title = "Fragment Bonus Cerveau (N.1 à N.72)",
                                badge = "+150 pts • Secret Cérébral",
                                badgeColor = PlasmaRoseGlow,
                                description = "Relique cérébrale spéciale présente dans les niveaux 1 à 72. Révèle le fonctionnement des régions du cerveau face aux émotions et à la méditation."
                            )
                        }
                    }

                    // SECTION 4: BONUS & POWER-UPS SPATIAUX
                    if (selectedCategory == 0 || selectedCategory == 4) {
                        item {
                            GlossarySectionHeader(
                                title = "🛡️ BONUS & POWER-UPS SPATIAUX",
                                subtitle = "Aides lumineuses apparaissant dans le cosmos",
                                accentColor = CyberCyanGlow
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🛡️",
                                title = "Bouclier Stellaire",
                                badge = "+1 ❤️ & Invulnérabilité 4s",
                                badgeColor = CyberCyanGlow,
                                description = "Restaure un cœur de bouclier et enveloppe la sphère d'une aura protectrice temporaire."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "⚡",
                                title = "Hyper-Propulsion & 🧲 Aimant Cosmique",
                                badge = "Vitesse x1.5 (8s) • Attraction (10s)",
                                badgeColor = SolarGoldGlow,
                                description = "L'Hyper-Propulsion accélère votre sphère de 50 %. L'Aimant Cosmique attire automatiquement les méditations et les Pensées Pures à grande distance."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🌿",
                                title = "Dissipateur Parasite • 💠 Sérénité Anti-Angoisse • 🌊 Calme Anti-Panique",
                                badge = "Immunité ciblée 12s",
                                badgeColor = EmeraldNovaGlow,
                                description = "Permet de détruire au simple contact l'obstacle mental correspondant pendant 12 secondes (+60 à +100 pts par obstacle dissipé)."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "💥",
                                title = "Onde Nova",
                                badge = "Purification de zone",
                                badgeColor = PlasmaRoseGlow,
                                description = "Libère une onde de clarté qui dissipe instantanément tous les obstacles mentaux dans un large rayon (+50 pts par obstacle)."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🔷",
                                title = "Égide du Libre Arbitre (Bouclier Réfractaire)",
                                badge = "Niv. 40+ • Durée 6s • +5 énergie/choc",
                                badgeColor = Color(0xFFE0F2FE),
                                description = "Enveloppe la sphère d'un prisme cristallin opalescent qui immunise vos bonus actifs (Turbo, Aimant) contre la Nuée Grégaire. Tout choc contre un Prisme Dogmatique ou un Foyer d'Angoisse fait rebondir l'obstacle sans consommer de bouclier et génère +5 pts d'énergie."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🏮",
                                title = "Phare de Métacognition (Sonar Anti-Obscurantisme)",
                                badge = "Niv. 60+ • Durée 8s • Bulle Pensées Pures",
                                badgeColor = Color(0xFFFDE047),
                                description = "Émet une impulsion radar dorée qui rétablit immédiatement la mini-carte malgré le brouillage, révèle toutes les cibles cachées sous le Voile d'Obscurantisme et pose une bulle d'invulnérabilité autour des Souvenirs et Étincelles de Lucidité présents à l'écran."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "⚓",
                                title = "Ancrage d'Humilité (Inversion Gravitationnelle)",
                                badge = "Niv. 100+ • Durée 6s • Conversion +1 🛡️",
                                badgeColor = Color(0xFFA5B4FC),
                                description = "Verrouille votre réserve d'énergie de purification contre tout drainage et inverse l'aimant du Miroir de Narcisse en zone de répulsion douce. Si un Miroir tente de vous aspirer, le lien s'inverse et vous octroie +1 Bouclier Stellaire temporaire (max 7)."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🧬",
                                title = "Gaine de Myéline (Sillage Isolant & Amortisseur)",
                                badge = "Niv. 110+ • Durée 7s • -65% vitesse & 1 choc absorbé",
                                badgeColor = Color(0xFF5EEAD4),
                                description = "Transforme votre Sillage Spirituel en barrière apaisante : tout obstacle qui le traverse voit sa vitesse réduite de 65 % et ses pulsations désamorcées pendant 4 s. Le premier impact direct subi est intégralement absorbé avec une micro-onde répulsive."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "👻",
                                title = "Déphasage Contemplatif (Intangibilité Thêta)",
                                badge = "Niv. 120+ • Durée 4s • Intangibilité & Sanctuaires +25%",
                                badgeColor = Color(0xFFDDD6FE),
                                description = "Rend l'Étincelle de l'Esprit totalement intangible et translucide pour traverser tous les obstacles sans dégâts ni ralentissement. Traverser un obstacle divise sa taille et sa vitesse par deux et accélère de +25 % les Sanctuaires d'Harmonisation proches."
                            )
                        }
                        item {
                            GlossaryElementCard(
                                emoji = "🏛️",
                                title = "Arche de Plénitude (Sanctuaire Nomade)",
                                badge = "Niv. 130+ • Durée 8s • Dôme répulsif & Harmonisation x2",
                                badgeColor = Color(0xFFFDE047),
                                description = "Déploie autour de votre sphère un Sanctuaire d'Harmonisation mobile (anneau d'or et d'émeraude) qui repousse automatiquement tous les obstacles mentaux à sa bordure, recueille à distance les Pensées Pures touchées par le dôme et harmonise les reliques du Fruit de l'Esprit 2x plus vite."
                            )
                        }
                    }
                }

                // Bottom Close Button
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = sphereColor.glowColor, cornerRadius = 14.dp)
                        .testTag("glossary_bottom_close_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sphereColor.primaryColor,
                        contentColor = SpaceBackground
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Fermer le Glossaire",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun GlossarySectionHeader(
    title: String,
    subtitle: String,
    accentColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = OrbitronFontFamily,
                fontWeight = FontWeight.ExtraBold
            ),
            color = accentColor
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = NebulaGray
        )
    }
}

@Composable
private fun GlossaryElementCard(
    emoji: String,
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    visualType: String? = null
) {
    Surface(
        color = SpaceSurfaceVariant.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.45f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Custom mini-canvas preview for redesigned obstacles or emoji badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SpaceBackground),
                contentAlignment = Alignment.Center
            ) {
                if (visualType != null) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val c = Offset(size.width / 2f, size.height / 2f)
                        val r = size.minDimension * 0.34f
                        when (visualType) {
                            "PARASITE" -> {
                                // Pensée Parasite / Toxine Cérébrale preview
                                drawCircle(color = Color(0xFF7E22CE).copy(alpha = 0.3f), radius = r * 1.35f, center = c)
                                for (i in 0 until 6) {
                                    val ang = i * 1.047f
                                    val lx = c.x + kotlin.math.cos(ang) * r * 0.48f
                                    val ly = c.y + kotlin.math.sin(ang) * r * 0.48f
                                    drawCircle(color = Color(0xFF3B0764), radius = r * 0.62f, center = Offset(lx, ly))
                                    drawCircle(color = Color(0xFFA855F7), radius = r * 0.62f, center = Offset(lx, ly), style = Stroke(width = 1.5.dp.toPx()))
                                }
                                drawCircle(color = Color(0xFF84CC16), radius = r * 0.28f, center = c)
                            }
                            "ANGOISSE" -> {
                                // Foyer d'Angoisse / Pic de Stress preview
                                drawCircle(color = DangerRed.copy(alpha = 0.25f), radius = r * 1.4f, center = c)
                                for (i in 0 until 4) {
                                    val ang = i * 0.785f
                                    val dx = kotlin.math.cos(ang) * r * 1.35f
                                    val dy = kotlin.math.sin(ang) * r * 1.35f
                                    drawLine(
                                        color = DangerRed,
                                        start = Offset(c.x - dx, c.y - dy),
                                        end = Offset(c.x + dx, c.y + dy),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                                drawCircle(color = Color(0xFF4C0519), radius = r * 0.65f, center = c)
                                drawCircle(color = Color(0xFFF43F5E), radius = r * 0.3f, center = c)
                            }
                            "PANIQUE" -> {
                                // Crise de Panique / Décharge Émotive preview
                                drawCircle(
                                    color = DangerRed.copy(alpha = 0.5f),
                                    radius = r * 1.35f,
                                    center = c,
                                    style = Stroke(width = 1.5.dp.toPx())
                                )
                                drawCircle(
                                    color = SolarGoldGlow.copy(alpha = 0.75f),
                                    radius = r * 0.95f,
                                    center = c,
                                    style = Stroke(width = 2.dp.toPx())
                                )
                                drawCircle(color = DangerRed, radius = r * 0.55f, center = c)
                                drawCircle(color = Color.White, radius = r * 0.24f, center = c)
                            }
                            "CONFORMISME" -> {
                                // La Nuée Grégaire preview (3x3 grey ranks)
                                val step = r * 0.65f
                                for (row in -1..1) {
                                    for (col in -1..1) {
                                        val ux = c.x + col * step
                                        val uy = c.y + row * step
                                        drawCircle(color = Color(0xFF475569), radius = r * 0.25f, center = Offset(ux, uy))
                                        drawCircle(color = Color(0xFF94A3B8), radius = r * 0.25f, center = Offset(ux, uy), style = Stroke(width = 1.dp.toPx()))
                                    }
                                }
                            }
                            "PRISME" -> {
                                // Le Prisme Dogmatique preview
                                drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.25f), radius = r * 1.3f, center = c)
                                drawCircle(color = Color(0xFF0F172A), radius = r * 0.95f, center = c)
                                drawCircle(color = Color(0xFFE0F2FE), radius = r * 0.95f, center = c, style = Stroke(width = 2.dp.toPx()))
                                drawLine(color = Color(0xFF38BDF8), start = Offset(c.x - r, c.y), end = Offset(c.x + r, c.y), strokeWidth = 1.5.dp.toPx())
                                drawLine(color = Color(0xFF38BDF8), start = Offset(c.x, c.y - r), end = Offset(c.x, c.y + r), strokeWidth = 1.5.dp.toPx())
                            }
                            "OBSCURANTISME" -> {
                                // Le Voile d'Obscurantisme preview
                                drawCircle(color = Color(0xFF020617), radius = r * 1.35f, center = c)
                                drawCircle(color = Color(0xFF475569), radius = r * 1.15f, center = c, style = Stroke(width = 1.5.dp.toPx()))
                                drawCircle(color = Color(0xFF64748B), radius = r * 0.25f, center = Offset(c.x - r * 0.35f, c.y - r * 0.2f))
                                drawCircle(color = Color(0xFF64748B), radius = r * 0.20f, center = Offset(c.x + r * 0.35f, c.y + r * 0.25f))
                            }
                            "BOURBIER" -> {
                                // Le Bourbier Mental preview
                                drawCircle(color = Color(0xFF334155).copy(alpha = 0.7f), radius = r * 1.3f, center = c)
                                drawCircle(color = Color(0xFF94A3B8), radius = r * 1.1f, center = c, style = Stroke(width = 1.5.dp.toPx()))
                                drawCircle(color = Color(0xFF64748B), radius = r * 0.65f, center = c, style = Stroke(width = 1.5.dp.toPx()))
                                drawCircle(color = Color(0xFFCBD5E1), radius = r * 0.25f, center = c)
                            }
                            "NARCISSE" -> {
                                // Le Miroir de Narcisse preview
                                drawCircle(color = SolarGoldGlow.copy(alpha = 0.35f), radius = r * 1.35f, center = c)
                                drawCircle(color = PlasmaRose, radius = r * 0.75f, center = c)
                                drawCircle(color = SolarGoldGlow, radius = r * 0.75f, center = c, style = Stroke(width = 2.dp.toPx()))
                                drawCircle(color = Color.White, radius = r * 0.28f, center = c)
                            }
                        }
                    }
                } else {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = StarlightWhite,
                        modifier = Modifier.weight(1f)
                    )
                }
                Surface(
                    color = badgeColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                ) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        color = badgeColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = NebulaGray
                )
            }
        }
    }
}

/**
 * Menu des 4 Remèdes proposé en bas de l'écran lorsque le joueur clique sur sa sphère
 * alors qu'un neurone infecté est à proximité et visible sur la carte (sans masquer la carte).
 */
@Composable
internal fun SphereRemedyChoicesOverlay(
    sphereColor: SphereColorOption,
    purificationEnergyPoints: Int,
    currentLevel: Int = 1,
    nearestNeuronAffect: String?,
    nearestNeuronCategory: String?,
    placeAtTop: Boolean = false,
    onChooseRemedy: (VirtueRemedyType) -> Unit,
    onDismiss: () -> Unit
) {
    var showLockedMilestones by remember { mutableStateOf(false) }
    val allRemedies = VirtueRemedyType.values().toList()
    val unlockedRemedies = remember(currentLevel) {
        allRemedies.filter { currentLevel >= it.unlockLevel }
    }
    val lockedRemedies = remember(currentLevel) {
        allRemedies.filter { currentLevel < it.unlockLevel }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onDismiss)
            .testTag("sphere_remedy_choices_overlay"),
        contentAlignment = if (placeAtTop) Alignment.TopCenter else Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = if (placeAtTop) 8.dp else 10.dp,
                    bottom = if (placeAtTop) 10.dp else 10.dp
                )
                .widthIn(max = 480.dp)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SpaceSurface.copy(alpha = 0.90f)),
            border = BorderStroke(1.5.dp, sphereColor.glowColor)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (nearestNeuronAffect != null) {
                                "🎯 Cible à proximité : « $nearestNeuronAffect »"
                            } else {
                                "🔮 Choisissez le remède à projeter (-10 pts)"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = OrbitronFontFamily,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = StarlightWhite
                        )
                        if (nearestNeuronCategory != null) {
                            Text(
                                text = "État : $nearestNeuronCategory • ✨ Énergie : $purificationEnergyPoints pts",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolarGoldGlow
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer l'arsenal des remèdes",
                            tint = NebulaGray
                        )
                    }
                }

                // Grille compacte des remèdes débloqués (ne cache plus la cible sur la carte et affiche le rôle complet sur 2 lignes)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    unlockedRemedies.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { virtue ->
                                Surface(
                                    onClick = { onChooseRemedy(virtue) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .astroTouchEffect(glowColor = virtue.glowColor, cornerRadius = 12.dp)
                                        .testTag("sphere_remedy_choice_${virtue.name.lowercase()}"),
                                    color = virtue.primaryColor.copy(alpha = 0.24f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = virtue.glowColor
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp, horizontal = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = virtue.emoji,
                                            fontSize = 18.sp
                                        )
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = virtue.displayName,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontFamily = OrbitronFontFamily,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.sp
                                                ),
                                                color = virtue.glowColor,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = virtue.shortRole,
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 9.sp,
                                                    lineHeight = 11.sp
                                                ),
                                                color = StarlightWhite.copy(alpha = 0.90f),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    // Bouton pliable optionnel pour voir les futurs paliers verrouillés sans encombrer la vue
                    if (lockedRemedies.isNotEmpty()) {
                        Surface(
                            onClick = { showLockedMilestones = !showLockedMilestones },
                            color = SpaceBackground.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, NebulaGray.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (showLockedMilestones) {
                                    "▲ Masquer les ${lockedRemedies.size} remèdes de paliers (Niv. 30–120)"
                                } else {
                                    "🔒 Voir les ${lockedRemedies.size} remèdes à débloquer (Niv. 30–120) ▼"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = NebulaGray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp)
                            )
                        }

                        if (showLockedMilestones) {
                            lockedRemedies.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { virtue ->
                                        Surface(
                                            onClick = { onChooseRemedy(virtue) },
                                            modifier = Modifier
                                                .weight(1f)
                                                .minimumInteractiveComponentSize()
                                                .testTag("sphere_remedy_choice_${virtue.name.lowercase()}"),
                                            color = SpaceBackground.copy(alpha = 0.65f),
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(1.dp, NebulaGray.copy(alpha = 0.35f))
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 7.dp, horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(text = "🔒", fontSize = 15.sp)
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "Niv.${virtue.unlockLevel} • ${virtue.displayName}",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 10.sp
                                                        ),
                                                        color = NebulaGray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = virtue.shortRole,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 8.5.sp,
                                                            lineHeight = 10.5.sp
                                                        ),
                                                        color = NebulaGray.copy(alpha = 0.75f),
                                                        maxLines = 2,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

