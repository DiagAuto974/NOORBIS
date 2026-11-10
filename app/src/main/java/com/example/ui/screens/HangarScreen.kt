package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.astroTouchEffect
import com.example.data.local.CollectedTextEntity
import com.example.data.local.LevelRecordEntity
import com.example.data.local.PlayerProfileEntity
import com.example.domain.model.AvailableSphereColors
import com.example.domain.model.MeditationsMatrix
import com.example.domain.model.SpaceSectorCatalog
import com.example.domain.model.SpaceSectorLevel
import com.example.domain.model.SphereColorOption
import com.example.domain.model.getSphereColorById
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.EmeraldNova
import com.example.ui.theme.EmeraldNovaGlow
import com.example.ui.theme.NebulaGray
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SolarGoldGlow
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.SpaceSurfaceVariant
import com.example.ui.theme.StarlightWhite
import com.example.ui.theme.VioletPulsarGlow

@Composable
fun HangarScreen(
    profile: PlayerProfileEntity,
    collectedTexts: List<CollectedTextEntity>,
    levelRecords: List<LevelRecordEntity>,
    onSelectColor: (String) -> Unit,
    onLaunchLevel: (Int) -> Unit,
    onOpenInventory: () -> Unit,
    isRoundJoystickEnabled: Boolean = true,
    onToggleRoundJoystick: (Boolean) -> Unit = {},
    isDirectTextEnabled: Boolean = true,
    onToggleDirectText: (Boolean) -> Unit = {},
    isGridEnabled: Boolean = false,
    onToggleGrid: (Boolean) -> Unit = {},
    isMiniMapEnabled: Boolean = true,
    onToggleMiniMap: (Boolean) -> Unit = {},
    isSoundEnabled: Boolean = true,
    onToggleSound: (Boolean) -> Unit = {},
    isInventoryAscending: Boolean = false,
    onToggleInventoryAscending: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedSphereColor = remember(profile.selectedColorId) {
        getSphereColorById(profile.selectedColorId)
    }
    val recordMap = remember(levelRecords) {
        levelRecords.associateBy { it.levelNumber }
    }
    val (brainCount, scriptureCount, collectedBrainIds) = remember(collectedTexts) {
        var bCount = 0
        var sCount = 0
        val brainIds = HashSet<String>()
        for (item in collectedTexts) {
            if (item.letter == "🧠" || item.id.startsWith("BRAIN_")) {
                bCount++
                brainIds.add(item.id)
            } else {
                sCount++
            }
        }
        Triple(bCount, sCount, brainIds)
    }
    val sectorList = remember(profile.unlockedLevel) {
        if (profile.unlockedLevel <= SpaceSectorCatalog.levels.size) {
            SpaceSectorCatalog.levels
        } else {
            SpaceSectorCatalog.levels + ((SpaceSectorCatalog.levels.size + 1)..profile.unlockedLevel).map { lvl ->
                SpaceSectorCatalog.getSector(lvl, seed = lvl * 1337L)
            }
        }
    }
    var showGlossaryDialog by remember { mutableStateOf(false) }

    val pulseTransition = rememberInfiniteTransition(label = "sphere_pulse")
    val glowScale by pulseTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("hangar_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. Hero Banner + Live Selected Sphere Showcase
            item {
                HeroSphereBanner(
                    selectedSphereColor = selectedSphereColor,
                    glowScale = glowScale,
                    unlockedLevel = profile.unlockedLevel,
                    totalScore = profile.totalScore,
                    collectedCount = scriptureCount,
                    brainCount = brainCount,
                    onQuickPlay = { onLaunchLevel(profile.unlockedLevel) },
                    onOpenInventory = onOpenInventory,
                    onOpenGlossary = { showGlossaryDialog = true }
                )
            }

            // 2. Sphere Color Picker Section
            item {
                SphereColorPickerSection(
                    selectedColor = selectedSphereColor,
                    onSelectColor = onSelectColor
                )
            }

            // 3. Space Levels Progression Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.select_level_title),
                        style = MaterialTheme.typography.headlineMedium,
                        color = StarlightWhite
                    )
                    Text(
                        text = "Niveau max : ${profile.unlockedLevel} / 135",
                        style = MaterialTheme.typography.labelMedium,
                        color = CyberCyanGlow
                    )
                }
            }

            items(sectorList, key = { it.levelNumber }) { sector ->
                val isUnlocked = sector.levelNumber <= profile.unlockedLevel
                val record = recordMap[sector.levelNumber]
                val hasBrainBonus = collectedBrainIds.contains("BRAIN_L${sector.levelNumber}")
                SectorLevelCard(
                    sector = sector,
                    isUnlocked = isUnlocked,
                    record = record,
                    hasBrainBonus = hasBrainBonus,
                    sphereColor = selectedSphereColor,
                    onLaunch = { onLaunchLevel(sector.levelNumber) }
                )
            }
        }

        if (showGlossaryDialog) {
            MapGlossaryDialog(
                sphereColor = selectedSphereColor,
                onDismiss = { showGlossaryDialog = false }
            )
        }
    }
}

@Composable
private fun HeroSphereBanner(
    selectedSphereColor: SphereColorOption,
    glowScale: Float,
    unlockedLevel: Int,
    totalScore: Int,
    collectedCount: Int,
    brainCount: Int,
    onQuickPlay: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenGlossary: () -> Unit = {}
) {
    val context = LocalContext.current
    var offlineStatusMessage by remember { mutableStateOf<String?>(null) }
    val saveHtmlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        if (uri != null) {
            runCatching {
                val htmlContent = buildStandaloneHtmlFromAssets(context)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(htmlContent.toByteArray(Charsets.UTF_8))
                }
                offlineStatusMessage = "✅ Fichier index.html hors ligne téléchargé avec succès !"
            }.onFailure {
                offlineStatusMessage = "⚠️ Erreur lors de l'enregistrement de index.html."
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hero_banner_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SpaceSurface),
        border = BorderStroke(1.dp, selectedSphereColor.primaryColor.copy(alpha = 0.5f))
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.ic_hero_nebula),
                contentDescription = "Bannière spatiale Noôrbis",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                SpaceBackground.copy(alpha = 0.35f),
                                SpaceSurface.copy(alpha = 0.94f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Live Animated "Étincelle de l'Esprit" Canvas Preview (Noyau de Conscience & Couronne Éthérée)
                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val baseRadius = size.minDimension * 0.34f
                            // Couronne Éthérée externe
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        selectedSphereColor.glowColor.copy(alpha = 0.52f),
                                        selectedSphereColor.primaryColor.copy(alpha = 0.24f),
                                        selectedSphereColor.primaryColor.copy(alpha = 0.06f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = baseRadius * 1.95f * glowScale
                                ),
                                radius = baseRadius * 1.95f * glowScale,
                                center = center
                            )
                            // Couronne Éthérée médiane
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.80f),
                                        selectedSphereColor.glowColor.copy(alpha = 0.70f),
                                        selectedSphereColor.primaryColor.copy(alpha = 0.22f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = baseRadius * 1.28f * glowScale
                                ),
                                radius = baseRadius * 1.28f * glowScale,
                                center = center
                            )
                            // Noyau de Conscience (blanc incandescent sans bordure stricte qui respire)
                            val coreScale = 0.90f + (glowScale - 0.92f) * 0.8f
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White,
                                        Color.White.copy(alpha = 0.92f),
                                        selectedSphereColor.glowColor.copy(alpha = 0.55f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = baseRadius * 0.72f * coreScale
                                ),
                                radius = baseRadius * 0.72f * coreScale,
                                center = center
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_noorbis_logo_1793798239084),
                                contentDescription = "Logo officiel Noôrbis",
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .border(1.2.dp, SolarGoldGlow.copy(alpha = 0.85f), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = "NOÖRBIS",
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 3.sp
                                ),
                                color = StarlightWhite
                            )
                        }
                        Text(
                            text = "Sphère : ${selectedSphereColor.name}",
                            style = MaterialTheme.typography.labelMedium,
                            color = selectedSphereColor.glowColor
                        )
                    }
                }

                // Stats Pill Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBadge(
                        label = "POINTS",
                        value = "$totalScore",
                        accent = SolarGoldGlow,
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        label = "MÉDITATIONS",
                        value = "$collectedCount",
                        accent = CyberCyanGlow,
                        modifier = Modifier.weight(1f)
                    )
                    StatBadge(
                        label = "CERVEAU",
                        value = "🧠 $brainCount/72",
                        accent = VioletPulsarGlow,
                        modifier = Modifier.weight(1.1f)
                    )
                    StatBadge(
                        label = "NIVEAU",
                        value = "#$unlockedLevel",
                        accent = EmeraldNovaGlow,
                        modifier = Modifier.weight(0.9f)
                    )
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onQuickPlay,
                        modifier = Modifier
                            .weight(1.2f)
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = selectedSphereColor.glowColor, cornerRadius = 14.dp)
                            .testTag("quick_launch_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedSphereColor.primaryColor,
                            contentColor = SpaceBackground
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "Lancer le jeu"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.launch_level, unlockedLevel),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    OutlinedButton(
                        onClick = onOpenInventory,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 14.dp)
                            .testTag("open_inventory_hangar_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory2,
                            contentDescription = "Ouvrir l'inventaire",
                            tint = CyberCyanGlow
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Inventaire ($collectedCount)",
                            style = MaterialTheme.typography.labelMedium,
                            color = StarlightWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenGlossary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = VioletPulsarGlow, cornerRadius = 14.dp)
                        .testTag("open_glossary_hangar_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, VioletPulsarGlow.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = "📖 Glossaire des Éléments de la Carte",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = StarlightWhite
                    )
                }

                OutlinedButton(
                    onClick = {
                        runCatching {
                            saveHtmlLauncher.launch("index.html")
                        }.onFailure {
                            val htmlContent = buildStandaloneHtmlFromAssets(context)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/html"
                                putExtra(Intent.EXTRA_SUBJECT, "index.html")
                                putExtra(Intent.EXTRA_TEXT, htmlContent)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Télécharger index.html (Hors ligne)"))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = EmeraldNovaGlow, cornerRadius = 14.dp)
                        .testTag("download_offline_index_hangar_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, EmeraldNovaGlow.copy(alpha = 0.75f))
                ) {
                    Text(
                        text = "📥 Télécharger index.html (Jouer hors ligne)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = EmeraldNovaGlow
                    )
                }

                if (offlineStatusMessage != null) {
                    Text(
                        text = offlineStatusMessage!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldNovaGlow
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = SpaceSurfaceVariant.copy(alpha = 0.85f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, SpaceCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                color = NebulaGray
            )
        }
    }
}

@Composable
private fun SphereColorPickerSection(
    selectedColor: SphereColorOption,
    onSelectColor: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("color_picker_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SpaceSurface),
        border = BorderStroke(1.dp, SpaceCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.choose_sphere_color),
                    style = MaterialTheme.typography.titleLarge,
                    color = StarlightWhite
                )
                Text(
                    text = selectedColor.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = selectedColor.glowColor
                )
            }

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                items(AvailableSphereColors, key = { it.id }) { option ->
                    val isSelected = option.id == selectedColor.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .astroTouchEffect(glowColor = option.glowColor, cornerRadius = 16.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectColor(option.id) }
                            .background(
                                if (isSelected) option.primaryColor.copy(alpha = 0.18f)
                                else SpaceSurfaceVariant.copy(alpha = 0.55f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) option.glowColor else SpaceCardBorder,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("sphere_color_${option.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color.White,
                                            option.glowColor,
                                            option.primaryColor
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Couleur sélectionnée",
                                    tint = SpaceBackground,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = option.name.substringBefore(" "),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) option.glowColor else StarlightWhite
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SectorLevelCard(
    sector: SpaceSectorLevel,
    isUnlocked: Boolean,
    record: LevelRecordEntity?,
    hasBrainBonus: Boolean,
    sphereColor: SphereColorOption,
    onLaunch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isUnlocked) Modifier.astroTouchEffect(glowColor = sphereColor.glowColor, pressScale = 0.96f, cornerRadius = 18.dp)
                else Modifier
            )
            .clickable(enabled = isUnlocked) { onLaunch() }
            .testTag("sector_card_${sector.levelNumber}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) SpaceSurface else SpaceSurface.copy(alpha = 0.55f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (record?.isCompleted == true) EmeraldNova.copy(alpha = 0.6f)
            else if (isUnlocked) SpaceCardBorder
            else SpaceCardBorder.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Level Badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (isUnlocked) sphereColor.primaryColor.copy(alpha = 0.2f)
                        else SpaceSurfaceVariant
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) sphereColor.primaryColor else SpaceCardBorder,
                        RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(
                        text = "${sector.levelNumber}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = sphereColor.glowColor
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Niveau verrouillé",
                        tint = NebulaGray
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Niveau ${sector.levelNumber} • ${sector.name}",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isUnlocked) StarlightWhite else NebulaGray
                    )
                }

                // Badges des 3 fruits du secteur
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    sector.texts.forEachIndexed { slotIndex, blueprint ->
                        val isSlotCollected = when (slotIndex) {
                            0 -> record?.hasTextA == true
                            1 -> record?.hasTextB == true
                            else -> record?.hasTextC == true
                        }
                        FruitBadgeChip(
                            emoji = blueprint.fruitEmoji,
                            name = blueprint.letter,
                            collected = isSlotCollected,
                            color = MeditationsMatrix.getFruitGlowColor(blueprint.fruitId),
                            modifier = Modifier.testTag("level_${sector.levelNumber}_fruit_badge_${slotIndex + 1}")
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    if ((record?.bestScore ?: 0) > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = SolarGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${record?.bestScore} pts",
                                style = MaterialTheme.typography.labelMedium,
                                color = SolarGoldGlow
                            )
                        }
                    }

                    if (sector.levelNumber <= 72) {
                        if (hasBrainBonus) {
                            Text(
                                text = "✓ 🧠 Bonus recueilli",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = VioletPulsarGlow
                            )
                        } else if (isUnlocked) {
                            Text(
                                text = "🧠 Bonus disponible",
                                style = MaterialTheme.typography.labelSmall,
                                color = NebulaGray
                            )
                        }
                    }
                }
            }

            if (isUnlocked) {
                Button(
                    onClick = onLaunch,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .astroTouchEffect(glowColor = sphereColor.glowColor, cornerRadius = 12.dp)
                        .testTag("play_level_${sector.levelNumber}_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = sphereColor.primaryColor,
                        contentColor = SpaceBackground
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Jouer le niveau ${sector.levelNumber}"
                    )
                }
            }
        }
    }
}

@Composable
fun FruitBadgeChip(
    emoji: String,
    name: String,
    collected: Boolean,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (collected) color.copy(alpha = 0.22f)
                else SpaceSurfaceVariant
            )
            .border(
                width = 1.dp,
                color = if (collected) color else SpaceCardBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (collected) "✓ $emoji $name" else "$emoji $name",
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 11.sp,
                fontWeight = if (collected) FontWeight.Bold else FontWeight.Normal
            ),
            color = if (collected) color else NebulaGray
        )
    }
}
