package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AstroDatabase
import com.example.data.repository.AstroRepository
import com.example.domain.model.getSphereColorById
import com.example.ui.components.astroTouchEffect
import com.example.ui.screens.GameSettingsDialog
import com.example.ui.screens.HangarScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.SpaceGameScreen
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NebulaGray
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SpaceBackground
import com.example.ui.theme.SpaceCardBorder
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.SpaceSurfaceVariant
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.AstroViewModel
import com.example.ui.viewmodel.GameStatus

import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.domain.service.AstroSoundManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        com.example.domain.model.MeditationsMatrix.ensureLoaded(applicationContext)
        enableEdgeToEdge()
        applyImmersiveFullscreen()
        setContent {
            MyApplicationTheme {
                AstroSphereApp()
            }
        }
    }

    private fun applyImmersiveFullscreen() {
        runCatching {
            val controller = WindowCompat.getInsetsController(window, window.decorView)
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            applyImmersiveFullscreen()
        }
    }

    override fun onResume() {
        super.onResume()
        applyImmersiveFullscreen()
        AstroSoundManager.onAppForegrounded()
    }

    override fun onPause() {
        super.onPause()
        AstroSoundManager.onAppBackgrounded()
    }

    override fun onStop() {
        super.onStop()
        AstroSoundManager.onAppBackgrounded()
    }

    override fun onDestroy() {
        if (isFinishing) {
            AstroSoundManager.onAppBackgrounded()
        }
        super.onDestroy()
    }
}

@Composable
fun AstroSphereApp() {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val lifecycleOwner = LocalLifecycleOwner.current
    val database = remember(appContext) { AstroDatabase.getInstance(appContext) }
    val repository = remember(database) { AstroRepository(database.astroDao()) }
    val viewModelFactory = remember(repository) { AstroViewModel.Factory(repository) }
    val viewModel: AstroViewModel = viewModel(
        factory = viewModelFactory
    )

    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> {
                    viewModel.onAppBackgrounded()
                }
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.onAppForegrounded()
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val collectedTexts by viewModel.collectedTexts.collectAsStateWithLifecycle()
    val levelRecords by viewModel.levelRecords.collectAsStateWithLifecycle()
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val gameState by viewModel.gameState.collectAsStateWithLifecycle()
    val filterLetter by viewModel.inventoryFilterLetter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.inventorySearchQuery.collectAsStateWithLifecycle()
    val inspectedText by viewModel.inspectedText.collectAsStateWithLifecycle()
    val isRoundJoystickEnabled by viewModel.isRoundJoystickEnabled.collectAsStateWithLifecycle()
    val isDirectTextEnabled by viewModel.isDirectTextEnabled.collectAsStateWithLifecycle()
    val isGridEnabled by viewModel.isGridEnabled.collectAsStateWithLifecycle()
    val isMiniMapEnabled by viewModel.isMiniMapEnabled.collectAsStateWithLifecycle()
    val isSoundEnabled by viewModel.isSoundEnabled.collectAsStateWithLifecycle()
    val isEffectsSoundEnabled by viewModel.isEffectsSoundEnabled.collectAsStateWithLifecycle()
    val isInventoryAscending by viewModel.isInventoryAscending.collectAsStateWithLifecycle()
    val resumeCountdown by viewModel.resumeCountdown.collectAsStateWithLifecycle()
    val directCollectedText by viewModel.directCollectedText.collectAsStateWithLifecycle()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsStateWithLifecycle()

    val selectedSphereColor = remember(profile.selectedColorId) {
        getSphereColorById(profile.selectedColorId)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = SpaceBackground,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isWideScreen) {
                    Surface(
                        color = SpaceSurface,
                        border = BorderStroke(1.dp, SpaceCardBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .testTag("bottom_nav_bar")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Hangar
                            val isHangarSelected = !showSettingsDialog && currentTab == AppTab.HANGAR
                            Surface(
                                color = if (isHangarSelected) SpaceSurfaceVariant else SpaceBackground.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .astroTouchEffect(glowColor = selectedSphereColor.glowColor, cornerRadius = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.selectTab(AppTab.HANGAR) }
                                    .testTag("nav_tab_hangar")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = stringResource(R.string.nav_hangar),
                                        tint = if (isHangarSelected) selectedSphereColor.glowColor else NebulaGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // 2. Espace (Jeu)
                            val isGameSelected = !showSettingsDialog && currentTab == AppTab.GAME
                            Surface(
                                color = if (isGameSelected) SpaceSurfaceVariant else SpaceBackground.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .astroTouchEffect(glowColor = selectedSphereColor.glowColor, cornerRadius = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.selectTab(AppTab.GAME) }
                                    .testTag("nav_tab_game")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.RocketLaunch,
                                        contentDescription = stringResource(R.string.nav_play),
                                        tint = if (isGameSelected) selectedSphereColor.glowColor else NebulaGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // 3. Inventaire
                            val isInventorySelected = !showSettingsDialog && currentTab == AppTab.INVENTORY
                            Surface(
                                color = if (isInventorySelected) SpaceSurfaceVariant else SpaceBackground.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .astroTouchEffect(glowColor = CyberCyanGlow, cornerRadius = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.selectTab(AppTab.INVENTORY) }
                                    .testTag("nav_tab_inventory")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    BadgedBox(
                                        badge = {
                                            if (collectedTexts.isNotEmpty()) {
                                                Badge(
                                                    containerColor = SolarGold,
                                                    contentColor = SpaceBackground
                                                ) {
                                                    Text(
                                                        text = "${collectedTexts.size}",
                                                        fontSize = 9.sp
                                                    )
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Inventory2,
                                            contentDescription = stringResource(R.string.nav_inventory),
                                            tint = if (isInventorySelected) CyberCyanGlow else NebulaGray,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }

                            // 4. Paramètres
                            Surface(
                                color = if (showSettingsDialog) SpaceSurfaceVariant else SpaceBackground.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                                    .astroTouchEffect(glowColor = selectedSphereColor.glowColor, cornerRadius = 10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setShowSettingsDialog(true) }
                                    .testTag("nav_tab_settings")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Paramètres",
                                        tint = if (showSettingsDialog) selectedSphereColor.glowColor else NebulaGray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = SpaceSurface,
                        modifier = Modifier.testTag("side_nav_rail")
                    ) {
                        NavigationRailItem(
                            selected = !showSettingsDialog && currentTab == AppTab.HANGAR,
                            onClick = { viewModel.selectTab(AppTab.HANGAR) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = stringResource(R.string.nav_hangar)
                                )
                            },
                            alwaysShowLabel = false,
                            modifier = Modifier.testTag("nav_rail_hangar")
                        )
                        NavigationRailItem(
                            selected = !showSettingsDialog && currentTab == AppTab.GAME,
                            onClick = { viewModel.selectTab(AppTab.GAME) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.RocketLaunch,
                                    contentDescription = stringResource(R.string.nav_play)
                                )
                            },
                            alwaysShowLabel = false,
                            modifier = Modifier.testTag("nav_rail_game")
                        )
                        NavigationRailItem(
                            selected = !showSettingsDialog && currentTab == AppTab.INVENTORY,
                            onClick = { viewModel.selectTab(AppTab.INVENTORY) },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (collectedTexts.isNotEmpty()) {
                                            Badge(containerColor = SolarGold) {
                                                Text("${collectedTexts.size}")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Inventory2,
                                        contentDescription = stringResource(R.string.nav_inventory)
                                    )
                                }
                            },
                            alwaysShowLabel = false,
                            modifier = Modifier.testTag("nav_rail_inventory")
                        )
                        NavigationRailItem(
                            selected = showSettingsDialog,
                            onClick = { viewModel.setShowSettingsDialog(true) },
                            icon = {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Paramètres"
                                )
                            },
                            alwaysShowLabel = false,
                            modifier = Modifier.testTag("nav_rail_settings")
                        )
                    }
                }

                when (currentTab) {
                    AppTab.HANGAR -> {
                        HangarScreen(
                            profile = profile,
                            collectedTexts = collectedTexts,
                            levelRecords = levelRecords,
                            onSelectColor = { viewModel.selectSphereColor(it) },
                            onLaunchLevel = { viewModel.startLevelFromHangar(it) },
                            onOpenInventory = { viewModel.selectTab(AppTab.INVENTORY) },
                            isRoundJoystickEnabled = isRoundJoystickEnabled,
                            onToggleRoundJoystick = { viewModel.setRoundJoystickEnabled(it) },
                            isDirectTextEnabled = isDirectTextEnabled,
                            onToggleDirectText = { viewModel.setDirectTextEnabled(it) },
                            isGridEnabled = isGridEnabled,
                            onToggleGrid = { viewModel.setGridEnabled(it) },
                            isMiniMapEnabled = isMiniMapEnabled,
                            onToggleMiniMap = { viewModel.setMiniMapEnabled(it) },
                            isSoundEnabled = isSoundEnabled,
                            onToggleSound = { viewModel.setSoundEnabled(it) },
                            isInventoryAscending = isInventoryAscending,
                            onToggleInventoryAscending = { viewModel.setInventoryAscending(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AppTab.GAME -> {
                        SpaceGameScreen(
                            gameState = gameState,
                            sphereColor = selectedSphereColor,
                            collectedInventory = collectedTexts,
                            onTouchTarget = { x, y -> viewModel.updateTouchTarget(x, y) },
                            onNudgePlayer = { dx, dy -> viewModel.nudgePlayer(dx, dy) },
                            onSetDirectionalInput = { dx, dy -> viewModel.setDirectionalInput(dx, dy) },
                            isRoundJoystickEnabled = isRoundJoystickEnabled,
                            isGridEnabled = isGridEnabled,
                            isMiniMapEnabled = isMiniMapEnabled,
                            resumeCountdown = resumeCountdown,
                            directCollectedText = directCollectedText,
                            onDismissDirectCollectedText = { viewModel.dismissDirectCollectedText(andResume = true) },
                            onDismissEndMissionTargetsPopup = { viewModel.dismissEndMissionTargetsPopup() },
                            onOpenSettings = { viewModel.setShowSettingsDialog(true) },
                            onAnchorThought = { viewModel.anchorThought(it) },
                            onShareTextReward = {
                                directCollectedText?.let { viewModel.rewardRelicShared(it) }
                            },
                            onSelectVirtue = { viewModel.selectOrProjectVirtue(it) },
                            onTapPlayerSphere = { viewModel.onPlayerSphereTapped() },
                            onDismissSphereRemedyMenu = { viewModel.dismissSphereRemedyMenu() },
                            onTogglePause = {
                                if (gameState.status == GameStatus.PLAYING) {
                                    viewModel.pauseGame()
                                } else {
                                    viewModel.startOrResumeGame()
                                }
                            },
                            onToggleQuickInventory = { viewModel.toggleQuickInventoryInGame() },
                            onStartOrNext = { viewModel.startOrResumeGame() },
                            onRetryLevel = {
                                viewModel.prepareLevel(gameState.level.levelNumber, autoStart = true)
                            },
                            onBackToHangar = {
                                viewModel.pauseGame()
                                viewModel.selectTab(AppTab.HANGAR)
                            },
                            onOpenFullInventory = {
                                viewModel.pauseGame()
                                viewModel.selectTab(AppTab.INVENTORY)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    AppTab.INVENTORY -> {
                        InventoryScreen(
                            collectedTexts = collectedTexts,
                            selectedFilterLetter = filterLetter,
                            inspectedText = inspectedText,
                            onSelectFilter = { viewModel.setInventoryFilter(it) },
                            onInspectText = { viewModel.inspectCollectedText(it) },
                            onGoToPlay = { viewModel.startLevelFromHangar(profile.unlockedLevel) },
                            onBack = { viewModel.selectTab(AppTab.HANGAR) },
                            isInventoryAscending = isInventoryAscending,
                            searchQuery = searchQuery,
                            onSearchQueryChange = { viewModel.setInventorySearchQuery(it) },
                            onShareTextReward = { textId -> viewModel.rewardTextShared(textId) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        if (showSettingsDialog) {
            GameSettingsDialog(
                isRoundJoystickEnabled = isRoundJoystickEnabled,
                isDirectTextEnabled = isDirectTextEnabled,
                isGridEnabled = isGridEnabled,
                isMiniMapEnabled = isMiniMapEnabled,
                isSoundEnabled = isSoundEnabled,
                isEffectsSoundEnabled = isEffectsSoundEnabled,
                isInventoryAscending = isInventoryAscending,
                sphereColor = selectedSphereColor,
                onToggleRoundJoystick = { viewModel.setRoundJoystickEnabled(it) },
                onToggleDirectText = { viewModel.setDirectTextEnabled(it) },
                onToggleGrid = { viewModel.setGridEnabled(it) },
                onToggleMiniMap = { viewModel.setMiniMapEnabled(it) },
                onToggleSound = { viewModel.setSoundEnabled(it) },
                onToggleEffectsSound = { viewModel.setEffectsSoundEnabled(it) },
                onToggleInventoryAscending = { viewModel.setInventoryAscending(it) },
                onExportSaveJson = { ctx -> viewModel.exportSaveJson(ctx) },
                onImportSaveJson = { ctx, json, cb -> viewModel.importSaveJson(ctx, json, cb) },
                onDismiss = { viewModel.setShowSettingsDialog(false) }
            )
        }
    }
}
