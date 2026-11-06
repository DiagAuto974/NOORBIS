package com.example.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CollectedTextEntity
import com.example.data.local.LevelRecordEntity
import com.example.data.local.PlayerProfileEntity
import com.example.data.repository.AstroRepository
import com.example.domain.model.BioelectricArcEffect
import com.example.domain.model.BrainBonusCatalog
import com.example.domain.model.BrainBonusFragment
import com.example.domain.model.BurstParticle
import com.example.domain.model.FloatingTextRelic
import com.example.domain.model.GammaWaveEffect
import com.example.domain.model.InfectedNeuron
import com.example.domain.model.MeditationsMatrix
import com.example.domain.model.NeuronCorruptionType
import com.example.domain.model.ObstacleType
import com.example.domain.model.PowerUpType
import com.example.domain.model.PurificationFeedbackEffect
import com.example.domain.model.RescueTarget
import com.example.domain.model.RescueTargetType
import com.example.domain.model.SimpleObstacleVariant
import com.example.domain.model.SpaceObstacle
import com.example.domain.model.SpacePowerUp
import com.example.domain.model.SpaceSectorCatalog
import com.example.domain.model.SpaceSectorLevel
import com.example.domain.model.SphereColorOption
import com.example.domain.model.TrailPoint
import com.example.domain.model.VIEWPORT_HEIGHT
import com.example.domain.model.VIEWPORT_WIDTH
import com.example.domain.model.VirtueRemedyType
import com.example.domain.model.VirtueWordProjectile
import com.example.domain.model.WORLD_HEIGHT
import com.example.domain.model.WORLD_WIDTH
import com.example.domain.model.getSphereColorById
import com.example.domain.service.AstroSoundManager
import com.example.domain.service.ContemplativeService
import com.example.domain.service.FeelingType
import com.example.ui.theme.CoralHyperGlow
import com.example.ui.theme.CyberCyanGlow
import com.example.ui.theme.EmeraldNovaGlow
import com.example.ui.theme.PlasmaRoseGlow
import com.example.ui.theme.SolarGoldGlow
import com.example.ui.theme.VioletPulsarGlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

enum class AppTab {
    HANGAR,
    GAME,
    INVENTORY
}

enum class GameStatus {
    READY,
    PLAYING,
    PAUSED,
    QUICK_INVENTORY,
    LEVEL_COMPLETE,
    GAME_OVER
}

@Immutable
data class ActiveGameState(
    val level: SpaceSectorLevel = SpaceSectorCatalog.getSector(1),
    val status: GameStatus = GameStatus.READY,
    // Player position in world space
    val playerX: Float = WORLD_WIDTH * 0.5f,
    val playerY: Float = WORLD_HEIGHT * 0.5f,
    val playerVx: Float = 0f,
    val playerVy: Float = 0f,
    // Camera top-left position in world space
    val cameraX: Float = (WORLD_WIDTH * 0.5f - VIEWPORT_WIDTH * 0.5f),
    val cameraY: Float = (WORLD_HEIGHT * 0.5f - VIEWPORT_HEIGHT * 0.5f),
    // Touch/drag position in screen-normalized coordinates (0f..1f)
    val touchScreenX: Float? = null,
    val touchScreenY: Float? = null,
    val dpadDx: Float = 0f,
    val dpadDy: Float = 0f,
    val shields: Int = 3,
    val maxShields: Int = 3,
    val invulnerableTimer: Float = 0f,
    val peaceAuraTimer: Float = 0f,
    val pacificationLevel: Float = 0f,
    val activeAnchoredThought: String? = null,
    val runScore: Int = 0,
    val obstacles: List<SpaceObstacle> = emptyList(),
    val relics: List<FloatingTextRelic> = emptyList(),
    val brainRelic: FloatingTextRelic? = null,
    val powerUps: List<SpacePowerUp> = emptyList(),
    val rescueTargets: List<RescueTarget> = emptyList(),
    val infectedNeurons: List<InfectedNeuron> = emptyList(),
    val virtueProjectiles: List<VirtueWordProjectile> = emptyList(),
    val selectedVirtueRemedy: VirtueRemedyType = VirtueRemedyType.PAIX,
    val purificationEnergyPoints: Int = 0,
    val showSphereRemedyMenu: Boolean = false,
    val virtueFireCooldown: Float = 0f,
    val neuronsPurifiedInLevel: Int = 0,
    val totalNeuronsPurified: Int = 0,
    val totalTargetsSaved: Int = 0,
    val targetsSavedInLevel: Int = 0,
    val targetsDestroyedInLevel: Int = 0,
    val showEndMissionTargetsPopup: Boolean = false,
    val endMissionSavedCount: Int = 0,
    val endMissionUnsavedOnMapCount: Int = 0,
    val endMissionDestroyedCount: Int = 0,
    val endMissionSavedPoints: Int = 0,
    val endMissionLostPoints: Int = 0,
    val endMissionPlayerTotalBefore: Int = 0,
    val endMissionPlayerTotalAfter: Int = 0,
    val speedBoostTimer: Float = 0f,
    val magnetTimer: Float = 0f,
    val antiAsteroidTimer: Float = 0f,
    val antiDroneTimer: Float = 0f,
    val antiMineTimer: Float = 0f,
    val conformismSlowTimer: Float = 0f,
    val homeostasisFieldTimer: Float = 0f,
    val elanDashTimer: Float = 0f,
    val freeWillAegisTimer: Float = 0f,
    val metacognitionBeaconTimer: Float = 0f,
    val humilityAnchorTimer: Float = 0f,
    val myelinSheathTimer: Float = 0f,
    val myelinShockAbsorberAvailable: Boolean = false,
    val phaseShiftTimer: Float = 0f,
    val plenitudeArkTimer: Float = 0f,
    val borderWrapTimer: Float = 0f,
    val borderWrapEdge: String = "",
    val bioelectricArcs: List<BioelectricArcEffect> = emptyList(),
    val gammaWaves: List<GammaWaveEffect> = emptyList(),
    val purificationFeedbacks: List<PurificationFeedbackEffect> = emptyList(),
    val trail: List<TrailPoint> = emptyList(),
    val particles: List<BurstParticle> = emptyList(),
    val bannerMessage: String? = null,
    val bannerTimer: Float = 0f,
    val elapsedTime: Float = 0f
) {
    val collectedLettersInRun: Set<String>
        get() = relics.filter { it.isCollected }.map { it.letter }.toSet()

    val allFragmentsCollected: Boolean
        get() = relics.isNotEmpty() && relics.all { it.isCollected }

    val isBonusTextCollected: Boolean
        get() = brainRelic?.isCollected ?: true

    val requiredPurifiedNeurons: Int
        get() = if (infectedNeurons.isEmpty()) 0 else (infectedNeurons.size + 1) / 2

    val isHalfNeuronsPurified: Boolean
        get() = neuronsPurifiedInLevel >= requiredPurifiedNeurons

    val isMiniMapJammedByObscurantism: Boolean
        get() = homeostasisFieldTimer <= 0f &&
            metacognitionBeaconTimer <= 0f &&
            obstacles.any { it.type == ObstacleType.OBSCURANTISM_VEIL }

    fun isTargetHiddenByObscurantism(target: RescueTarget): Boolean {
        if (homeostasisFieldTimer > 0f || metacognitionBeaconTimer > 0f) return false
        return obstacles.any { obs ->
            obs.type == ObstacleType.OBSCURANTISM_VEIL &&
                hypot(target.x - obs.x, target.y - obs.y) <= (obs.radius * 1.18f)
        }
    }

    fun isNeuronVisibleAndNearby(neuron: InfectedNeuron): Boolean {
        if (neuron.isPurified) return false
        val relX = neuron.x - cameraX
        val relY = neuron.y - cameraY
        val isVisibleOnMap = relX in 0.03f..(VIEWPORT_WIDTH - 0.03f) &&
            relY in 0.03f..(VIEWPORT_HEIGHT - 0.03f)
        val distToPlayer = hypot(neuron.x - playerX, neuron.y - playerY)
        return isVisibleOnMap && distToPlayer <= NEURON_PURIFICATION_PROXIMITY
    }

    fun nearestVisibleNearbyUnpurifiedNeuron(): InfectedNeuron? {
        var best: InfectedNeuron? = null
        var bestDist = Float.MAX_VALUE
        for (i in 0 until infectedNeurons.size) {
            val n = infectedNeurons[i]
            if (isNeuronVisibleAndNearby(n)) {
                val d = hypot(n.x - playerX, n.y - playerY)
                if (d < bestDist) {
                    bestDist = d
                    best = n
                }
            }
        }
        return best
    }

    fun allVisibleNearbyUnpurifiedNeurons(): List<InfectedNeuron> {
        return infectedNeurons
            .filter { isNeuronVisibleAndNearby(it) }
            .sortedBy { hypot(it.x - playerX, it.y - playerY) }
    }

    fun bestMatchingVisibleNearbyNeuron(virtue: VirtueRemedyType): InfectedNeuron? {
        val connected = allVisibleNearbyUnpurifiedNeurons()
        if (connected.isEmpty()) return null
        val primaryMatch = connected.firstOrNull { n ->
            virtue == n.corruptionType.remedyVirtue ||
                virtue == VirtueRemedyType.INFLUX_BIOELECTRIQUE
        }
        if (primaryMatch != null) return primaryMatch
        val secondaryMatch = connected.firstOrNull { n ->
            virtue == n.corruptionType.secondaryVirtue ||
                virtue == VirtueRemedyType.RESONANCE_GAMMA
        }
        if (secondaryMatch != null) return secondaryMatch
        return connected.first()
    }

    fun isObstacleVisibleAndNearbyForVirtue(obs: SpaceObstacle): Boolean {
        val isTargetableType = obs.type == ObstacleType.DOGMATIC_PRISM ||
            obs.type == ObstacleType.OBSCURANTISM_VEIL ||
            obs.type == ObstacleType.MENTAL_MIRE ||
            obs.type == ObstacleType.NARCISSUS_MIRROR ||
            obs.type == ObstacleType.LASER_DRONE ||
            obs.type == ObstacleType.PULSAR_MINE
        if (!isTargetableType) {
            return false
        }
        val relX = obs.x - cameraX
        val relY = obs.y - cameraY
        val isVisibleOnMap = relX in 0.03f..(VIEWPORT_WIDTH - 0.03f) &&
            relY in 0.03f..(VIEWPORT_HEIGHT - 0.03f)
        val distToPlayer = hypot(obs.x - playerX, obs.y - playerY)
        return isVisibleOnMap && distToPlayer <= (NEURON_PURIFICATION_PROXIMITY + obs.radius)
    }

    fun nearestVisibleNearbyVirtueObstacle(): SpaceObstacle? {
        var best: SpaceObstacle? = null
        var bestDist = Float.MAX_VALUE
        for (i in 0 until obstacles.size) {
            val obs = obstacles[i]
            if (isObstacleVisibleAndNearbyForVirtue(obs)) {
                val d = hypot(obs.x - playerX, obs.y - playerY)
                if (d < bestDist) {
                    bestDist = d
                    best = obs
                }
            }
        }
        return best
    }

    companion object {
        const val NEURON_PURIFICATION_PROXIMITY = 0.48f
    }
}

class AstroViewModel(
    private val repository: AstroRepository
) : ViewModel() {

    val profile: StateFlow<PlayerProfileEntity> = repository.playerProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerProfileEntity()
        )

    val collectedTexts: StateFlow<List<CollectedTextEntity>> = repository.collectedTexts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val levelRecords: StateFlow<List<LevelRecordEntity>> = repository.levelRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentTab = MutableStateFlow(AppTab.HANGAR)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _selectedLevelNumber = MutableStateFlow(1)
    val selectedLevelNumber: StateFlow<Int> = _selectedLevelNumber.asStateFlow()

    private val _inventoryFilterLetter = MutableStateFlow<String?>(null)
    val inventoryFilterLetter: StateFlow<String?> = _inventoryFilterLetter.asStateFlow()

    private val _inventorySearchQuery = MutableStateFlow("")
    val inventorySearchQuery: StateFlow<String> = _inventorySearchQuery.asStateFlow()

    private val _inspectedText = MutableStateFlow<CollectedTextEntity?>(null)
    val inspectedText: StateFlow<CollectedTextEntity?> = _inspectedText.asStateFlow()

    private val _isRoundJoystickEnabled = MutableStateFlow(true)
    val isRoundJoystickEnabled: StateFlow<Boolean> = _isRoundJoystickEnabled.asStateFlow()

    private val _isDirectTextEnabled = MutableStateFlow(true)
    val isDirectTextEnabled: StateFlow<Boolean> = _isDirectTextEnabled.asStateFlow()

    private val _isGridEnabled = MutableStateFlow(false)
    val isGridEnabled: StateFlow<Boolean> = _isGridEnabled.asStateFlow()

    private val _isMiniMapEnabled = MutableStateFlow(true)
    val isMiniMapEnabled: StateFlow<Boolean> = _isMiniMapEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _isEffectsSoundEnabled = MutableStateFlow(true)
    val isEffectsSoundEnabled: StateFlow<Boolean> = _isEffectsSoundEnabled.asStateFlow()

    private val _isInventoryAscending = MutableStateFlow(false)
    val isInventoryAscending: StateFlow<Boolean> = _isInventoryAscending.asStateFlow()

    private val _directCollectedText = MutableStateFlow<FloatingTextRelic?>(null)
    val directCollectedText: StateFlow<FloatingTextRelic?> = _directCollectedText.asStateFlow()

    private val _resumeCountdown = MutableStateFlow<Int?>(null)
    val resumeCountdown: StateFlow<Int?> = _resumeCountdown.asStateFlow()

    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog.asStateFlow()

    private val _gameState = MutableStateFlow(ActiveGameState())
    val gameState: StateFlow<ActiveGameState> = _gameState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var resumeCountdownJob: Job? = null
    private var cumulativeTargetsSaved: Int = 0
    private var cumulativeNeuronsPurified: Int = 0
    private var nextVirtueProjectileId: Int = 1
    private var nextPurificationFeedbackId: Int = 1
    private var earnedMaxShields: Int = 3
    private var pendingSharedPurificationPoints: Int = 0
    private val claimedShareTextIdsInMemory = HashSet<String>()
    private val activeZoneElementKeys = HashSet<String>(32)
    private val currentFrameZoneKeysBuffer = HashSet<String>(32)
    private val phaseShiftedObstacleIds = HashSet<Int>(32)
    private val humilityConvertedMirrorIds = HashSet<Int>(16)

    // Pre-allocated staging buffers reused during each 16ms physics tick before taking immutable snapshots
    private val obstaclesBuffer = ArrayList<SpaceObstacle>(48)
    private val relicsBuffer = ArrayList<FloatingTextRelic>(4)
    private val powerUpsBuffer = ArrayList<SpacePowerUp>(16)
    private val rescueTargetsBuffer = ArrayList<RescueTarget>(80)
    private val neuronsBuffer = ArrayList<InfectedNeuron>(24)
    private val projectilesBuffer = ArrayList<VirtueWordProjectile>(16)
    private val bioelectricArcsBuffer = ArrayList<BioelectricArcEffect>(8)
    private val gammaWavesBuffer = ArrayList<GammaWaveEffect>(4)
    private val purificationFeedbacksBuffer = ArrayList<PurificationFeedbackEffect>(8)
    private val trailBuffer = ArrayList<TrailPoint>(20)
    private val particlesBuffer = ArrayList<BurstParticle>(160)
    private val newlyCollectedRelicsBuffer = ArrayList<FloatingTextRelic>(4)
    private val newlyCollectedPowerUpsBuffer = ArrayList<SpacePowerUp>(8)
    private val newlySavedTargetsBuffer = ArrayList<RescueTarget>(16)
    private val newlyDestroyedTargetsBuffer = ArrayList<RescueTarget>(16)

    private fun spawnParticle(x: Float, y: Float, vx: Float, vy: Float, color: Color, life: Float) {
        particlesBuffer.add(BurstParticle(x = x, y = y, vx = vx, vy = vy, color = color, life = life))
    }

    private fun obtainTrailPoint(x: Float, y: Float, alpha: Float): TrailPoint {
        return TrailPoint(x = x, y = y, alpha = alpha)
    }

    private fun clearAndRecycleTrail() {
        trailBuffer.clear()
    }

    private fun clearAndRecycleParticles() {
        particlesBuffer.clear()
    }

    init {
        viewModelScope.launch {
            repository.ensureInitialized()
            val persisted = repository.getProfileOnce()
            cumulativeTargetsSaved = maxOf(cumulativeTargetsSaved, persisted.totalTargetsSaved)
            cumulativeNeuronsPurified = maxOf(cumulativeNeuronsPurified, persisted.totalNeuronsPurified)
            earnedMaxShields = maxOf(earnedMaxShields, persisted.maxShields).coerceIn(3, 7)
            _isRoundJoystickEnabled.value = persisted.isRoundJoystickEnabled
            _isDirectTextEnabled.value = persisted.isDirectTextEnabled
            _isGridEnabled.value = persisted.isGridEnabled
            _isMiniMapEnabled.value = persisted.isMiniMapEnabled
            _isSoundEnabled.value = persisted.isSoundEnabled
            _isEffectsSoundEnabled.value = persisted.isEffectsSoundEnabled
            _isInventoryAscending.value = persisted.isInventoryAscending
            AstroSoundManager.setMusicEnabled(persisted.isSoundEnabled)
            AstroSoundManager.setEffectsSoundEnabled(persisted.isEffectsSoundEnabled)
            if (persisted.isSoundEnabled) {
                AstroSoundManager.ensureAmbientPlaying()
            }
            _gameState.update { state ->
                val maxSh = earnedMaxShields.coerceIn(3, 7)
                state.copy(
                    totalTargetsSaved = cumulativeTargetsSaved,
                    totalNeuronsPurified = cumulativeNeuronsPurified,
                    maxShields = maxSh,
                    shields = if (state.status == GameStatus.READY) maxSh else state.shields.coerceIn(1, maxSh)
                )
            }
        }
        prepareLevel(1, autoStart = false)
        AstroSoundManager.ensureAmbientPlaying()
    }

    fun onPlayerSphereTapped() {
        AstroSoundManager.playUiTap()
        _gameState.update { state ->
            val nearbyVisibleNeuron = state.nearestVisibleNearbyUnpurifiedNeuron()
            val nearbyVirtueObstacle = state.nearestVisibleNearbyVirtueObstacle()
            val hasRemainingTargets = state.infectedNeurons.any { !it.isPurified } ||
                state.obstacles.isNotEmpty()

            if (state.showSphereRemedyMenu) {
                state.copy(showSphereRemedyMenu = false)
            } else if (state.purificationEnergyPoints < 10) {
                state.copy(
                    showSphereRemedyMenu = false,
                    bannerMessage = "✨ Récupérez d'abord une Pensée Pure (+10 pts) pour ouvrir les remèdes de votre sphère !",
                    bannerTimer = 2.6f
                )
            } else if (!hasRemainingTargets) {
                state.copy(
                    showSphereRemedyMenu = false,
                    bannerMessage = "✨ Tous les neurones de cette mission sont déjà purifiés !",
                    bannerTimer = 2.2f
                )
            } else if (nearbyVisibleNeuron == null && nearbyVirtueObstacle == null) {
                state.copy(
                    showSphereRemedyMenu = false,
                    bannerMessage = "🧭 Approchez-vous d'un neurone infecté ou d'un obstacle visible sur la carte pour choisir un remède !",
                    bannerTimer = 2.8f
                )
            } else {
                val targetTitle = when {
                    nearbyVisibleNeuron != null -> "Neurone « ${nearbyVisibleNeuron.negativeAffectLabel} »"
                    nearbyVirtueObstacle?.type == ObstacleType.DOGMATIC_PRISM -> "💎 Le Prisme Dogmatique"
                    nearbyVirtueObstacle?.type == ObstacleType.OBSCURANTISM_VEIL -> "🌑 Le Voile d'Obscurantisme"
                    nearbyVirtueObstacle?.type == ObstacleType.MENTAL_MIRE -> "🕸️ Le Bourbier Mental"
                    nearbyVirtueObstacle?.type == ObstacleType.NARCISSUS_MIRROR -> "🪞 Le Miroir de Narcisse"
                    else -> nearbyVirtueObstacle?.type?.displayName ?: "Obstacle mental"
                }
                state.copy(
                    showSphereRemedyMenu = true,
                    playerVx = 0f,
                    playerVy = 0f,
                    touchScreenX = null,
                    touchScreenY = null,
                    dpadDx = 0f,
                    dpadDy = 0f,
                    bannerMessage = "🔮 $targetTitle à proximité ! Choisissez la vertu à projeter !",
                    bannerTimer = 2.8f
                )
            }
        }
    }

    fun dismissSphereRemedyMenu() {
        _gameState.update { it.copy(showSphereRemedyMenu = false) }
    }

    fun selectOrProjectVirtue(virtue: VirtueRemedyType) {
        _gameState.update { state ->
            val maxUnlockedLvl = maxOf(state.level.levelNumber, profile.value.unlockedLevel)
            if (virtue.unlockLevel > maxUnlockedLvl) {
                AstroSoundManager.playUiTap()
                return@update state.copy(
                    bannerMessage = "🔒 « ${virtue.displayName} » (${virtue.projectileWord}) se débloque au Niveau ${virtue.unlockLevel} !",
                    bannerTimer = 2.6f
                )
            }

            val nearestNeuron = state.bestMatchingVisibleNearbyNeuron(virtue)
            val nearestVirtueObs = state.nearestVisibleNearbyVirtueObstacle()

            val neuronMatchesChosenVirtue = nearestNeuron != null && (
                virtue == nearestNeuron.corruptionType.remedyVirtue ||
                    virtue == nearestNeuron.corruptionType.secondaryVirtue ||
                    virtue == VirtueRemedyType.INFLUX_BIOELECTRIQUE ||
                    virtue == VirtueRemedyType.RESONANCE_GAMMA
                )

            val targetIsNeuron = when {
                nearestNeuron != null && nearestVirtueObs != null -> {
                    val dNeuron = hypot(nearestNeuron.x - state.playerX, nearestNeuron.y - state.playerY)
                    val dObs = hypot(nearestVirtueObs.x - state.playerX, nearestVirtueObs.y - state.playerY)
                    when {
                        virtue == VirtueRemedyType.VERITE_DISCERNEMENT && nearestVirtueObs.type == ObstacleType.DOGMATIC_PRISM -> false
                        neuronMatchesChosenVirtue -> true
                        virtue == VirtueRemedyType.ELAN_ACTION && nearestVirtueObs.type == ObstacleType.MENTAL_MIRE -> false
                        virtue == VirtueRemedyType.PLASTICITE_RECADRAGE &&
                            (nearestVirtueObs.type == ObstacleType.NARCISSUS_MIRROR || nearestVirtueObs.type == ObstacleType.DOGMATIC_PRISM) -> false
                        else -> dNeuron <= dObs
                    }
                }
                nearestNeuron != null -> true
                else -> false
            }

            if ((nearestNeuron != null || nearestVirtueObs != null) &&
                state.purificationEnergyPoints >= 10 &&
                state.status == GameStatus.PLAYING
            ) {
                val targetX = if (targetIsNeuron && nearestNeuron != null) nearestNeuron.x else nearestVirtueObs!!.x
                val targetY = if (targetIsNeuron && nearestNeuron != null) nearestNeuron.y else nearestVirtueObs!!.y
                val dx = targetX - state.playerX
                val dy = targetY - state.playerY
                val dist = hypot(dx, dy).coerceAtLeast(0.001f)
                val projSpeed = 1.15f
                val proj = VirtueWordProjectile(
                    id = nextVirtueProjectileId++,
                    virtue = virtue,
                    wordLabel = virtue.projectileWord,
                    targetNeuronId = if (targetIsNeuron && nearestNeuron != null) nearestNeuron.id else -1,
                    targetObstacleId = if (!targetIsNeuron && nearestVirtueObs != null) nearestVirtueObs.id else -1,
                    x = state.playerX,
                    y = state.playerY,
                    vx = (dx / dist) * projSpeed,
                    vy = (dy / dist) * projSpeed,
                    primaryColor = virtue.primaryColor,
                    glowColor = virtue.glowColor,
                    life = 3.2f
                )

                var nextVx = state.playerVx
                var nextVy = state.playerVy
                var nextElanDashTimer = state.elanDashTimer
                var nextHomeostasisTimer = state.homeostasisFieldTimer
                var nextConformismSlowTimer = state.conformismSlowTimer
                var bonusScore = 0
                var extraEffectNote = ""

                // Niveau 50 (ÉLAN & ACTION) : Micro-sprint directionnel polarisé vers la Pensée Pure la plus proche + dissolution immédiate du Bourbier Mental traversé
                if (virtue == VirtueRemedyType.ELAN_ACTION) {
                    nextElanDashTimer = 2.2f
                    nextConformismSlowTimer = 0f
                    val nearestUnsavedTarget = rescueTargetsBuffer
                        .filter { !it.isSaved && !it.isDestroyed }
                        .minByOrNull { t ->
                            hypot(
                                shortestToroidalDelta(state.playerX, t.x, WORLD_WIDTH),
                                shortestToroidalDelta(state.playerY, t.y, WORLD_HEIGHT)
                            )
                        }
                    if (nearestUnsavedTarget != null) {
                        val tdx = shortestToroidalDelta(state.playerX, nearestUnsavedTarget.x, WORLD_WIDTH)
                        val tdy = shortestToroidalDelta(state.playerY, nearestUnsavedTarget.y, WORLD_HEIGHT)
                        val tDist = hypot(tdx, tdy).coerceAtLeast(0.001f)
                        nextVx = (tdx / tDist) * 0.76f
                        nextVy = (tdy / tDist) * 0.76f
                        extraEffectNote = " • 🚀 Micro-sprint vers « ${nearestUnsavedTarget.conceptLabel} »"
                    }
                    for (oIdx in obstaclesBuffer.lastIndex downTo 0) {
                        val obs = obstaclesBuffer[oIdx]
                        if (obs.type == ObstacleType.MENTAL_MIRE &&
                            hypot(
                                shortestToroidalDelta(state.playerX, obs.x, WORLD_WIDTH),
                                shortestToroidalDelta(state.playerY, obs.y, WORLD_HEIGHT)
                            ) <= (obs.radius + 0.25f)
                        ) {
                            obstaclesBuffer.removeAt(oIdx)
                            bonusScore += 85
                            extraEffectNote = " • 🔥 Bourbier Mental dissous (+85 pts)"
                        }
                    }
                }

                // Niveau 80 (HOMÉOSTASIE & PRÉSENCE) : Champ harmonique stationnaire (6s) + levée des ralentissements et brouillages radar
                if (virtue == VirtueRemedyType.HOMEOSTASIE_PRESENCE) {
                    nextHomeostasisTimer = 6.0f
                    nextConformismSlowTimer = 0f
                    extraEffectNote = " • 🌿 Champ Harmonique actif (6s, radar & vitesse rétablis)"
                }

                val targetDesc = if (targetIsNeuron && nearestNeuron != null) {
                    "le neurone (${nearestNeuron.negativeAffectLabel})"
                } else {
                    nearestVirtueObs?.type?.displayName ?: "l'obstacle"
                }
                AstroSoundManager.playVirtueProjected(virtue)
                projectilesBuffer.add(proj)
                state.copy(
                    selectedVirtueRemedy = virtue,
                    showSphereRemedyMenu = false,
                    playerVx = nextVx,
                    playerVy = nextVy,
                    elanDashTimer = nextElanDashTimer,
                    homeostasisFieldTimer = nextHomeostasisTimer,
                    conformismSlowTimer = nextConformismSlowTimer,
                    runScore = state.runScore + bonusScore,
                    obstacles = obstaclesBuffer.toList(),
                    purificationEnergyPoints = (state.purificationEnergyPoints - 10).coerceAtLeast(0),
                    virtueProjectiles = projectilesBuffer.toList(),
                    virtueFireCooldown = 0.35f,
                    bannerMessage = "${virtue.emoji} Projection de « ${virtue.displayName} » (${virtue.projectileWord}) vers $targetDesc$extraEffectNote !",
                    bannerTimer = 2.8f
                )
            } else {
                AstroSoundManager.playUiTap()
                state.copy(
                    selectedVirtueRemedy = virtue,
                    showSphereRemedyMenu = false,
                    bannerMessage = when {
                        state.purificationEnergyPoints < 10 ->
                            "✨ Récupérez une Pensée Pure (+10 pts) puis cliquez sur votre sphère !"
                        else ->
                            "🧭 Approchez-vous d'un neurone infecté ou d'un obstacle visible sur la carte pour projeter « ${virtue.displayName} » !"
                    },
                    bannerTimer = 2.4f
                )
            }
        }
    }

    fun setRoundJoystickEnabled(enabled: Boolean) {
        _isRoundJoystickEnabled.value = enabled
        if (!enabled) {
            _gameState.update { it.copy(dpadDx = 0f, dpadDy = 0f) }
        }
        viewModelScope.launch {
            repository.updateSettings(isRoundJoystickEnabled = enabled)
        }
    }

    fun setDirectTextEnabled(enabled: Boolean) {
        _isDirectTextEnabled.value = enabled
        viewModelScope.launch {
            repository.updateSettings(isDirectTextEnabled = enabled)
        }
    }

    fun setGridEnabled(enabled: Boolean) {
        _isGridEnabled.value = enabled
        viewModelScope.launch {
            repository.updateSettings(isGridEnabled = enabled)
        }
    }

    fun setMiniMapEnabled(enabled: Boolean) {
        _isMiniMapEnabled.value = enabled
        viewModelScope.launch {
            repository.updateSettings(isMiniMapEnabled = enabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        AstroSoundManager.setMusicEnabled(enabled)
        if (enabled) {
            AstroSoundManager.ensureAmbientPlaying()
        }
        viewModelScope.launch {
            repository.updateSettings(isSoundEnabled = enabled)
        }
    }

    fun setEffectsSoundEnabled(enabled: Boolean) {
        _isEffectsSoundEnabled.value = enabled
        AstroSoundManager.setEffectsSoundEnabled(enabled)
        if (enabled) {
            AstroSoundManager.playUiTap()
        }
        viewModelScope.launch {
            repository.updateSettings(isEffectsSoundEnabled = enabled)
        }
    }

    fun setInventoryAscending(ascending: Boolean) {
        _isInventoryAscending.value = ascending
        viewModelScope.launch {
            repository.updateSettings(isInventoryAscending = ascending)
        }
    }

    fun dismissDirectCollectedText(andResume: Boolean = true) {
        _directCollectedText.value = null
        resumeCountdownJob?.cancel()
        if (andResume && _gameState.value.status == GameStatus.PAUSED) {
            resumeCountdownJob = viewModelScope.launch {
                _gameState.update { it.copy(invulnerableTimer = it.invulnerableTimer.coerceAtLeast(3.0f)) }
                _resumeCountdown.value = 2
                delay(1000L)
                _resumeCountdown.value = 1
                delay(1000L)
                _resumeCountdown.value = null
                startOrResumeGame()
            }
        } else {
            _resumeCountdown.value = null
        }
    }

    fun dismissEndMissionTargetsPopup() {
        AstroSoundManager.playUiTap()
        _gameState.update { it.copy(showEndMissionTargetsPopup = false) }
    }

    /**
     * Anchors a meditation thought into the player's heart, granting an Aura de Paix
     * that slows nearby obstacles, protects the ship, and rewards contemplative play.
     */
    fun anchorThought(thought: String) {
        AstroSoundManager.playRelicCollected()
        _gameState.update { current ->
            current.copy(
                activeAnchoredThought = thought,
                peaceAuraTimer = 12.0f,
                invulnerableTimer = current.invulnerableTimer.coerceAtLeast(12.0f),
                runScore = current.runScore + 100,
                bannerMessage = "🕊️ Pensée ancrée dans votre cœur ! Aura de Paix activée (12s)",
                bannerTimer = 3.5f
            )
        }
    }

    /**
     * Récompense le joueur lorsqu'il partage un texte de méditation ou un fragment bonus :
     * +3 Pensées Positives (+3 au compteur de Pensées Pures et +30 pts d'énergie de purification & de score).
     * Limité à UNE SEULE FOIS par texte pour empêcher le gain infini dans l'inventaire.
     */
    fun rewardRelicShared(relic: FloatingTextRelic) {
        val textId = repository.computeCollectedTextId(_gameState.value.level.levelNumber, relic)
        rewardTextShared(textId)
    }

    fun rewardTextShared(textId: String) {
        if (textId.isBlank() || !claimedShareTextIdsInMemory.add(textId)) {
            return
        }
        viewModelScope.launch {
            val (awarded, updatedProfile) = repository.claimSharedTextRewardOnce(
                textId = textId,
                bonusPoints = 30
            )
            if (!awarded) {
                return@launch
            }
            val prevSaved = cumulativeTargetsSaved
            cumulativeTargetsSaved = maxOf(cumulativeTargetsSaved + 3, updatedProfile.totalTargetsSaved)
            val crossedFiftyMilestone = (prevSaved / 50) < (cumulativeTargetsSaved / 50)
            if (crossedFiftyMilestone) {
                earnedMaxShields = maxOf(earnedMaxShields + 1, updatedProfile.maxShields).coerceIn(3, 7)
                AstroSoundManager.playExtraHeartEarned()
            } else {
                AstroSoundManager.playTargetSaved()
            }

            // Si le joueur partage depuis l'inventaire avant de lancer une mission, on garde ces +30 pts pour le début de la mission
            if (_gameState.value.status != GameStatus.PLAYING) {
                pendingSharedPurificationPoints += 30
            }

            val savedSnapshot = cumulativeTargetsSaved
            val shieldsSnapshot = earnedMaxShields
            _gameState.update { current ->
                val updatedMaxShields = maxOf(current.maxShields, shieldsSnapshot).coerceAtMost(7)
                val updatedShields = if (crossedFiftyMilestone) {
                    (current.shields + 1).coerceAtMost(updatedMaxShields)
                } else {
                    current.shields
                }
                current.copy(
                    purificationEnergyPoints = current.purificationEnergyPoints + 30,
                    targetsSavedInLevel = current.targetsSavedInLevel + 3,
                    totalTargetsSaved = savedSnapshot,
                    runScore = current.runScore + 30,
                    maxShields = updatedMaxShields,
                    shields = updatedShields,
                    bannerMessage = if (crossedFiftyMilestone) {
                        "💖 Partage inspirant : +3 Pensées Positives (+30 pts) & +1 Cœur supplémentaire ($updatedShields/$updatedMaxShields ❤️) !"
                    } else {
                        "✨ Partage inspirant : +3 Pensées Positives gagnées (+30 pts d'énergie) !"
                    },
                    bannerTimer = 3.8f
                )
            }

            // Met à jour la fiche inspectée si elle est ouverte pour refléter `hasClaimedShareReward = true`
            val currentInspected = _inspectedText.value
            if (currentInspected != null && currentInspected.id == textId) {
                _inspectedText.value = currentInspected.copy(hasClaimedShareReward = true)
            }
        }
    }

    suspend fun getPersonalReflection(
        verse: String,
        fruit: String,
        situation: String,
        feeling: FeelingType
    ): String {
        return ContemplativeService.generatePersonalReflection(verse, fruit, situation, feeling)
    }

    fun setShowSettingsDialog(show: Boolean) {
        if (show && _gameState.value.status == GameStatus.PLAYING) {
            pauseGame()
        }
        _showSettingsDialog.value = show
    }

    fun selectTab(tab: AppTab) {
        if (tab != AppTab.GAME && _gameState.value.status == GameStatus.PLAYING) {
            pauseGame()
        }
        _currentTab.value = tab
        val status = _gameState.value.status
        if (_isSoundEnabled.value && status != GameStatus.LEVEL_COMPLETE && status != GameStatus.GAME_OVER && status != GameStatus.PAUSED) {
            AstroSoundManager.ensureAmbientPlaying()
        }
    }

    fun selectSphereColor(colorId: String) {
        viewModelScope.launch {
            repository.updateSelectedSphereColor(colorId)
        }
    }

    fun setInventoryFilter(letter: String?) {
        _inventoryFilterLetter.value = letter
    }

    fun setInventorySearchQuery(query: String) {
        _inventorySearchQuery.value = query
    }

    fun inspectCollectedText(entity: CollectedTextEntity?) {
        _inspectedText.value = entity
    }

    fun startLevelFromHangar(levelNumber: Int) {
        _selectedLevelNumber.value = levelNumber
        _currentTab.value = AppTab.GAME
        prepareLevel(levelNumber, autoStart = false, preserveRunState = false)
    }

    fun prepareLevel(
        levelNumber: Int,
        autoStart: Boolean = true,
        preserveRunState: Boolean = autoStart
    ) {
        gameLoopJob?.cancel()
        resumeCountdownJob?.cancel()
        _resumeCountdown.value = null
        activeZoneElementKeys.clear()
        val sector = SpaceSectorCatalog.getSector(levelNumber)
        _selectedLevelNumber.value = levelNumber
        AstroSoundManager.setLevelForMusic(levelNumber)
        if (_isSoundEnabled.value && autoStart) {
            AstroSoundManager.ensureAmbientPlaying()
        }

        val startPlayerX = WORLD_WIDTH * 0.5f  // 1.0f (center of 2.0x2.0 map)
        val startPlayerY = WORLD_HEIGHT * 0.5f // 1.0f
        val startCamX = (startPlayerX - VIEWPORT_WIDTH * 0.5f).coerceIn(0f, WORLD_WIDTH - VIEWPORT_WIDTH)
        val startCamY = (startPlayerY - VIEWPORT_HEIGHT * 0.5f).coerceIn(0f, WORLD_HEIGHT - VIEWPORT_HEIGHT)

        val random = Random(System.currentTimeMillis() + levelNumber * 1337L)

        // Règle de progression des obstacles mentaux :
        // - Niveaux 1 à 9 : les 3 obstacles simples de base (Pensée Parasite, Foyer d'Angoisse, Crise de Panique).
        // - À partir du niveau 10, tous les 5 niveaux (10, 15, 20, ..., 135), un nouvel obstacle simple est débloqué
        //   (Droit et vices du consentement, Rhétorique et manipulation, Psychologie et emprise, Atteintes aux libertés).
        // - Tous les 20 niveaux (20, 40, 60, 80, 100), un obstacle spécial est débloqué :
        //   * Niv. 20+ : La Nuée Grégaire (Conformisme)
        //   * Niv. 40+ : Le Prisme Dogmatique (Endoctrinement)
        //   * Niv. 60+ : Le Voile d'Obscurantisme (L'Ignorance)
        //   * Niv. 80+ : Le Bourbier Mental (Incapacité)
        //   * Niv. 100+ : Le Miroir de Narcisse (L'Ego)
        val activeMissionObstacleTypes: List<ObstacleType> = if (levelNumber <= 19) {
            listOf(
                ObstacleType.ASTEROID,
                ObstacleType.LASER_DRONE,
                ObstacleType.PULSAR_MINE
            )
        } else {
            ObstacleType.values().filter { levelNumber >= it.unlockLevel }
        }

        val unlockedSimpleVariants = SimpleObstacleVariant.entries.filter { levelNumber >= it.unlockLevel }
            .ifEmpty {
                listOf(
                    SimpleObstacleVariant.PENSEE_PARASITE,
                    SimpleObstacleVariant.FOYER_ANGOISSE,
                    SimpleObstacleVariant.CRISE_PANIQUE
                )
            }
        val newlyUnlockedSimpleVariant = SimpleObstacleVariant.entries.find {
            it.unlockLevel >= 10 && it.unlockLevel == levelNumber
        }
        val latestMilestoneSimpleVariant = unlockedSimpleVariants
            .filter { it.unlockLevel >= 10 }
            .maxByOrNull { it.unlockLevel }

        val totalObstaclesToSpawn = maxOf(sector.obstacleCount, activeMissionObstacleTypes.size)
        var simpleSpawnIndex = 0
        val obstacles = buildList {
            for (i in 0 until totalObstaclesToSpawn) {
                val rawType = activeMissionObstacleTypes[i % activeMissionObstacleTypes.size]
                val isSimpleType = rawType == ObstacleType.ASTEROID ||
                    rawType == ObstacleType.LASER_DRONE ||
                    rawType == ObstacleType.PULSAR_MINE

                val chosenSimpleVariant: SimpleObstacleVariant? = if (isSimpleType) {
                    val v = when {
                        newlyUnlockedSimpleVariant != null && simpleSpawnIndex < 3 -> newlyUnlockedSimpleVariant
                        latestMilestoneSimpleVariant != null && simpleSpawnIndex % 3 == 0 -> latestMilestoneSimpleVariant
                        else -> unlockedSimpleVariants[(simpleSpawnIndex + levelNumber) % unlockedSimpleVariants.size]
                    }
                    simpleSpawnIndex++
                    v
                } else {
                    null
                }
                val type = chosenSimpleVariant?.baseArchetype ?: rawType

                // Spawn across the expanded 4.0 x 4.0 map, keeping a safe zone around the player's start center
                var ox = 0.15f + random.nextFloat() * (WORLD_WIDTH - 0.30f)
                var oy = 0.15f + random.nextFloat() * (WORLD_HEIGHT - 0.30f)
                var obsAttempts = 0
                while (obsAttempts < 30 && hypot(ox - startPlayerX, oy - startPlayerY) < 0.48f) {
                    ox = 0.15f + random.nextFloat() * (WORLD_WIDTH - 0.30f)
                    oy = 0.15f + random.nextFloat() * (WORLD_HEIGHT - 0.30f)
                    obsAttempts++
                }
                val speedScale = when (type) {
                    ObstacleType.CONFORMISM_SWARM -> 0.72f
                    ObstacleType.DOGMATIC_PRISM -> 0.48f
                    ObstacleType.OBSCURANTISM_VEIL -> 0.25f
                    ObstacleType.MENTAL_MIRE -> 0.18f
                    ObstacleType.NARCISSUS_MIRROR -> 0.55f
                    else -> 1.0f
                }
                val baseSpeed = (0.095f + random.nextFloat() * 0.08f) * sector.obstacleSpeedMultiplier * speedScale
                val angle = random.nextFloat() * (Math.PI.toFloat() * 2f)
                add(
                    SpaceObstacle(
                        id = i + 1,
                        type = type,
                        x = ox,
                        y = oy,
                        vx = cos(angle) * baseSpeed,
                        vy = sin(angle) * baseSpeed * 0.85f,
                        radius = when (type) {
                            ObstacleType.ASTEROID -> 0.042f + (i % 3) * 0.007f
                            ObstacleType.LASER_DRONE -> 0.038f
                            ObstacleType.PULSAR_MINE -> 0.044f
                            ObstacleType.CONFORMISM_SWARM -> 0.088f // Essaim compact en rangs serrés entouré d'une grande zone d'action rectangulaire
                            ObstacleType.DOGMATIC_PRISM -> 0.056f // Monolithe géométrique rigide
                            ObstacleType.OBSCURANTISM_VEIL -> 0.195f // Vaste nébuleuse d'encre noire
                            ObstacleType.MENTAL_MIRE -> 0.145f // Champ gravitationnel poisseux
                            ObstacleType.NARCISSUS_MIRROR -> 0.044f // Astre scintillant trompeur
                        },
                        rotation = random.nextFloat() * 360f,
                        rotationSpeed = when (type) {
                            ObstacleType.CONFORMISM_SWARM -> 0f // Rangs serrés parfaitement alignés
                            ObstacleType.DOGMATIC_PRISM -> (if (i % 2 == 0) 1f else -1f) * 18f
                            ObstacleType.OBSCURANTISM_VEIL -> 12f
                            ObstacleType.MENTAL_MIRE -> -22f
                            else -> (if (i % 2 == 0) 1f else -1f) * (45f + random.nextFloat() * 80f)
                        },
                        pulsePhase = random.nextFloat() * 6.28f,
                        simpleVariant = chosenSimpleVariant
                    )
                )
            }
        }

        // Randomly distribute the 3 Fruit of the Spirit relics across the expanded map (4.0 x 4.0)
        // Each playthrough generates completely different positions, spaced away from the player
        // and generously separated from each other across different regions of the galaxy.
        val margin = 0.35f
        val minPlayerDist = 0.70f
        val minRelicSeparation = 1.30f
        val relicCoordinates = mutableListOf<Pair<Float, Float>>()

        for (slot in 0 until sector.texts.size) {
            var rx = margin + random.nextFloat() * (WORLD_WIDTH - 2f * margin)
            var ry = margin + random.nextFloat() * (WORLD_HEIGHT - 2f * margin)
            var attempts = 0
            while (attempts < 80) {
                val distPlayer = hypot(rx - startPlayerX, ry - startPlayerY)
                val distOtherRelics = relicCoordinates.all { (ox, oy) ->
                    hypot(rx - ox, ry - oy) >= minRelicSeparation
                }
                if (distPlayer >= minPlayerDist && distOtherRelics) {
                    break
                }
                rx = margin + random.nextFloat() * (WORLD_WIDTH - 2f * margin)
                ry = margin + random.nextFloat() * (WORLD_HEIGHT - 2f * margin)
                attempts++
            }
            relicCoordinates.add(Pair(rx, ry))
        }

        val relics = sector.texts.mapIndexed { index, blueprint ->
            val (rx, ry) = relicCoordinates.getOrElse(index) { Pair(WORLD_WIDTH * 0.3f, WORLD_HEIGHT * 0.3f) }
            val driftAngle = random.nextFloat() * (Math.PI.toFloat() * 2f)
            val driftSpeed = 0.018f * sector.obstacleSpeedMultiplier.coerceAtMost(1.4f)

            FloatingTextRelic(
                letter = blueprint.letter,
                title = blueprint.title,
                content = blueprint.content,
                points = blueprint.basePoints,
                x = rx,
                y = ry,
                vx = cos(driftAngle) * driftSpeed,
                vy = sin(driftAngle) * driftSpeed,
                isCollected = false,
                phase = index * 1.8f,
                spawnTime = 0f,
                isSpawned = true,
                fruitId = blueprint.fruitId,
                fruitName = blueprint.letter,
                fruitEmoji = blueprint.fruitEmoji,
                situationTitle = blueprint.situationTitle,
                scriptureRef = blueprint.scriptureRef,
                verse = blueprint.verse,
                explain = blueprint.explain,
                keep = blueprint.keep,
                prayer = blueprint.prayer,
                slotIndex = index
            )
        }

        // Spawn 1 Brain Bonus Fragment (Texte Bonus 🧠) in every level so the bonus text condition can always be fulfilled
        val brainFragment = BrainBonusCatalog.getFragmentForLevel(levelNumber)
            ?: BrainBonusCatalog.fragments.getOrNull((levelNumber - 1) % BrainBonusCatalog.fragments.size)?.copy(levelNumber = levelNumber)
        val brainRelic = brainFragment?.let { frag ->
            var bx = margin + random.nextFloat() * (WORLD_WIDTH - 2f * margin)
            var by = margin + random.nextFloat() * (WORLD_HEIGHT - 2f * margin)
            var attempts = 0
            while (attempts < 60) {
                val distPlayer = hypot(bx - startPlayerX, by - startPlayerY)
                val distRelics = relicCoordinates.all { (ox, oy) -> hypot(bx - ox, by - oy) >= 1.0f }
                if (distPlayer >= 0.8f && distRelics) break
                bx = margin + random.nextFloat() * (WORLD_WIDTH - 2f * margin)
                by = margin + random.nextFloat() * (WORLD_HEIGHT - 2f * margin)
                attempts++
            }
            val driftAngle = random.nextFloat() * 6.28f
            val driftSpeed = 0.014f
            FloatingTextRelic(
                letter = "🧠",
                title = "Fragment #${frag.levelNumber} • ${frag.title}",
                content = frag.fullDisplayText,
                points = frag.points,
                x = bx,
                y = by,
                vx = cos(driftAngle) * driftSpeed,
                vy = sin(driftAngle) * driftSpeed,
                isCollected = false,
                phase = 0.5f,
                spawnTime = 0f,
                isSpawned = true,
                fruitId = "brain",
                fruitName = "Fragment Cerveau #${frag.levelNumber}",
                fruitEmoji = "🧠",
                situationKey = "bonus",
                situationTitle = frag.category,
                scriptureRef = frag.source,
                verse = frag.content,
                explain = "Fragment bonus de connaissance et d'éveil juridique / philosophique.",
                keep = frag.title,
                prayer = frag.source,
                slotIndex = 3,
                isBrainBonus = true,
                brainLevel = frag.levelNumber
            )
        }

        // Spawn initial power-ups scattered across the 4.0 x 4.0 space map (including unlocked defensive means)
        val powerUpTypes = buildList {
            add(PowerUpType.ANTI_ASTEROID)
            add(PowerUpType.SHIELD)
            add(PowerUpType.ANTI_DRONE)
            add(PowerUpType.SPEED_BOOST)
            add(PowerUpType.ANTI_MINE)
            add(PowerUpType.MAGNET)
            add(PowerUpType.EMP_BLAST)
            if (levelNumber >= 40) add(PowerUpType.FREE_WILL_AEGIS)
            if (levelNumber >= 60) add(PowerUpType.METACOGNITION_BEACON)
            if (levelNumber >= 100) add(PowerUpType.HUMILITY_ANCHOR)
            if (levelNumber >= 110) add(PowerUpType.MYELIN_SHEATH)
            if (levelNumber >= 120) add(PowerUpType.CONTEMPLATIVE_PHASE_SHIFT)
            if (levelNumber >= 130) add(PowerUpType.PLENITUDE_ARK)
        }
        val initialPowerUps = powerUpTypes.mapIndexed { idx, type ->
            val px = margin + random.nextFloat() * (WORLD_WIDTH - 2f * margin)
            val py = margin + random.nextFloat() * (WORLD_HEIGHT - 2f * margin)
            val driftAngle = random.nextFloat() * 6.28f
            val driftSpeed = 0.012f + random.nextFloat() * 0.012f
            SpacePowerUp(
                id = idx + 1,
                type = type,
                x = px,
                y = py,
                vx = cos(driftAngle) * driftSpeed,
                vy = sin(driftAngle) * driftSpeed,
                radius = 0.038f,
                phase = idx * 1.5f,
                isCollected = false
            )
        }

        // Spawn rescue targets: 3 base targets (Étincelle de Lucidité, Fragment de Savoir, Souvenir)
        // + 1 additional target every 2 levels!
        val rescueTargetCount = 3 + ((levelNumber - 1) / 2)
        val targetTypes = RescueTargetType.values()
        val initialRescueTargets = (0 until rescueTargetCount).map { idx ->
            val tType = targetTypes[idx % targetTypes.size]
            val concept = tType.concepts[(levelNumber + idx) % tType.concepts.size]
            var tx = margin + random.nextFloat() * (WORLD_WIDTH - 2f * margin)
            var ty = margin + random.nextFloat() * (WORLD_HEIGHT - 2f * margin)
            var attempts = 0
            while (attempts < 50) {
                val distPlayer = hypot(tx - startPlayerX, ty - startPlayerY)
                val safeFromObstacles = obstacles.all { obs ->
                    hypot(tx - obs.x, ty - obs.y) >= (obs.radius + 0.22f)
                }
                if (distPlayer >= 0.42f && safeFromObstacles) break
                tx = margin + random.nextFloat() * (WORLD_WIDTH - 2f * margin)
                ty = margin + random.nextFloat() * (WORLD_HEIGHT - 2f * margin)
                attempts++
            }
            val driftAngle = random.nextFloat() * 6.28f
            val driftSpeed = 0.012f + random.nextFloat() * 0.010f
            RescueTarget(
                id = idx + 1,
                type = tType,
                conceptLabel = concept,
                x = tx,
                y = ty,
                vx = cos(driftAngle) * driftSpeed,
                vy = sin(driftAngle) * driftSpeed,
                radius = 0.036f,
                phase = idx * 1.1f
            )
        }

        // Commence la mission 1 avec 2 neurones infectés puis ajoute 1 de plus toutes les 10 missions
        val neuronCount = 2 + ((levelNumber.coerceAtLeast(1) - 1) / 10)
        val corruptionTypes = NeuronCorruptionType.entries.filter { levelNumber >= it.unlockLevel }
            .ifEmpty { NeuronCorruptionType.entries }
        val initialInfectedNeurons = (0 until neuronCount).map { idx ->
            val cType = corruptionTypes[idx % corruptionTypes.size]
            val affectLabel = cType.negativeAffects[(levelNumber + idx) % cType.negativeAffects.size]
            var nx = margin + random.nextFloat() * (WORLD_WIDTH - 2f * margin)
            var ny = margin + random.nextFloat() * (WORLD_HEIGHT - 2f * margin)
            var attempts = 0
            while (attempts < 50) {
                val distPlayer = hypot(nx - startPlayerX, ny - startPlayerY)
                val safeFromRelics = relics.all { r -> hypot(nx - r.x, ny - r.y) >= 0.25f }
                if (distPlayer >= 0.45f && safeFromRelics) break
                nx = margin + random.nextFloat() * (WORLD_WIDTH - 2f * margin)
                ny = margin + random.nextFloat() * (WORLD_HEIGHT - 2f * margin)
                attempts++
            }
            val driftAngle = random.nextFloat() * 6.28f
            val baseSpeed = when (cType) {
                NeuronCorruptionType.TRISTESSE -> 0.007f // Alourdi par la tristesse
                NeuronCorruptionType.AGITATION -> 0.024f // Déchaîné / erratique
                NeuronCorruptionType.COLERE_PEUR -> 0.014f
                NeuronCorruptionType.RANCOEUR -> 0.011f // Froid et rigide
                NeuronCorruptionType.SURTENSION -> 0.026f // Suractivation sympathique contractée et rapide
                NeuronCorruptionType.APATHIE -> 0.004f // Sidération dopaminergique quasi-immobile
                NeuronCorruptionType.RIGIDITE_OBSESSION -> 0.012f // Boucle rigide obsessionnelle
                NeuronCorruptionType.BURNOUT_DISSOCIATION -> 0.009f // Épuisement et déconnexion somatique
            }
            InfectedNeuron(
                id = idx + 1,
                corruptionType = cType,
                negativeAffectLabel = affectLabel,
                x = nx,
                y = ny,
                vx = cos(driftAngle) * baseSpeed,
                vy = sin(driftAngle) * baseSpeed,
                radius = 0.044f,
                phase = idx * 1.3f,
                purificationProgress = 0f,
                isPurified = false
            )
        }

        val currentMaxShields = earnedMaxShields.coerceIn(3, 7)
        val startingShields = if (levelNumber > 1 && preserveRunState) {
            _gameState.value.shields.coerceIn(1, currentMaxShields)
        } else {
            currentMaxShields
        }
        val carriedSharePoints = pendingSharedPurificationPoints
        if (autoStart || preserveRunState) {
            pendingSharedPurificationPoints = 0
        }
        val startingPurificationPoints = if (levelNumber > 1 && preserveRunState) {
            _gameState.value.purificationEnergyPoints + carriedSharePoints
        } else {
            carriedSharePoints
        }

        obstaclesBuffer.clear()
        obstaclesBuffer.addAll(obstacles)
        relicsBuffer.clear()
        relicsBuffer.addAll(relics)
        powerUpsBuffer.clear()
        powerUpsBuffer.addAll(initialPowerUps)
        rescueTargetsBuffer.clear()
        rescueTargetsBuffer.addAll(initialRescueTargets)
        neuronsBuffer.clear()
        neuronsBuffer.addAll(initialInfectedNeurons)
        projectilesBuffer.clear()
        bioelectricArcsBuffer.clear()
        gammaWavesBuffer.clear()
        purificationFeedbacksBuffer.clear()
        phaseShiftedObstacleIds.clear()
        humilityConvertedMirrorIds.clear()
        clearAndRecycleTrail()
        clearAndRecycleParticles()

        _gameState.value = ActiveGameState(
            level = sector,
            status = if (autoStart) GameStatus.PLAYING else GameStatus.READY,
            playerX = startPlayerX,
            playerY = startPlayerY,
            cameraX = startCamX,
            cameraY = startCamY,
            shields = startingShields,
            maxShields = currentMaxShields,
            invulnerableTimer = 1.0f,
            runScore = _gameState.value.runScore.takeIf { levelNumber > 1 && preserveRunState } ?: 0,
            obstacles = obstaclesBuffer.toList(),
            relics = relicsBuffer.toList(),
            brainRelic = brainRelic,
            powerUps = powerUpsBuffer.toList(),
            rescueTargets = rescueTargetsBuffer.toList(),
            infectedNeurons = neuronsBuffer.toList(),
            virtueProjectiles = projectilesBuffer.toList(),
            bioelectricArcs = emptyList(),
            gammaWaves = emptyList(),
            trail = trailBuffer.toList(),
            particles = particlesBuffer.toList(),
            selectedVirtueRemedy = _gameState.value.selectedVirtueRemedy,
            purificationEnergyPoints = startingPurificationPoints,
            showSphereRemedyMenu = false,
            neuronsPurifiedInLevel = 0,
            totalNeuronsPurified = cumulativeNeuronsPurified,
            totalTargetsSaved = cumulativeTargetsSaved,
            targetsSavedInLevel = 0,
            targetsDestroyedInLevel = 0,
            bannerMessage = when {
                levelNumber == 20 -> "🐑 Niv. 20 : La Nuée Grégaire + Obstacle simple « Captation » 🕸️ !"
                levelNumber == 30 -> "🌬️ Niv. 30 : Remède « SOUFFLE VAGAL » + Obstacle simple « Abus de faiblesse » 🪤 !"
                levelNumber == 40 -> "🔷 Niv. 40 : Prisme Dogmatique + « ÉGIDE DU LIBRE ARBITRE » + « Spoliation » 🏴 !"
                levelNumber == 50 -> "🔥 Niv. 50 : Remède « ÉLAN & ACTION » + Obstacle simple « Novlangue » 🗣️ !"
                levelNumber == 60 -> "🏮 Niv. 60 : Voile d'Obscurantisme + « PHARE DE MÉTACOGNITION » + « Subversion » 🦂 !"
                levelNumber == 80 -> "🌿 Niv. 80 : « HOMÉOSTASIE & PRÉSENCE » + Bourbier Mental + « Subterfuge » 🕳️ !"
                levelNumber == 100 -> "⚓ Niv. 100 : Miroir de Narcisse + « ANCRAGE D'HUMILITÉ » + « Infantilisation » 🍼 !"
                levelNumber == 110 -> "🧬 Niv. 110 : « GAINE DE MYÉLINE » + Obstacle simple « Asservissement » 🔒 !"
                levelNumber == 120 -> "👻 Niv. 120 : « DÉPHASAGE CONTEMPLATIF » + Obstacle simple « Arbitraire » 👑 !"
                levelNumber == 130 -> "🏛️ Niv. 130 : « ARCHE DE PLÉNITUDE » + Obstacle simple « Bâillonnement » 🤐 !"
                newlyUnlockedSimpleVariant != null ->
                    "${newlyUnlockedSimpleVariant.emoji} Niv. $levelNumber : Nouvel obstacle simple « ${newlyUnlockedSimpleVariant.displayName} » (${newlyUnlockedSimpleVariant.category.title}) !"
                levelNumber > 1 && (levelNumber - 1) % 10 == 0 ->
                    "🎵 Nouvelle musique (${AstroSoundManager.getMusicThemeTitle(levelNumber)}) • $neuronCount neurones à purifier !"
                else ->
                    "Niveau ${sector.levelNumber} • 🎵 ${AstroSoundManager.getMusicThemeTitle(levelNumber)} • $neuronCount neurones !"
            },
            bannerTimer = 3.6f
        )

        if (autoStart) {
            startGameLoop()
        }
    }

    fun startOrResumeGame() {
        val current = _gameState.value
        if (current.status == GameStatus.LEVEL_COMPLETE) {
            prepareLevel(current.level.levelNumber + 1, autoStart = false, preserveRunState = true)
            return
        }
        if (current.status == GameStatus.GAME_OVER) {
            _gameState.update { it.copy(runScore = 0) }
            prepareLevel(current.level.levelNumber, autoStart = true, preserveRunState = false)
            return
        }
        if (_isSoundEnabled.value) {
            AstroSoundManager.ensureAmbientPlaying()
        }
        _gameState.update { it.copy(status = GameStatus.PLAYING) }
        startGameLoop()
    }

    fun pauseGame() {
        gameLoopJob?.cancel()
        resumeCountdownJob?.cancel()
        _resumeCountdown.value = null
        AstroSoundManager.updateAmbientState(false)
        _gameState.update { state ->
            if (state.status == GameStatus.PLAYING) {
                state.copy(
                    status = GameStatus.PAUSED,
                    dpadDx = 0f,
                    dpadDy = 0f,
                    touchScreenX = null,
                    touchScreenY = null
                )
            } else state
        }
    }

    /**
     * Appelé lorsque l'application passe en arrière-plan ou que l'écran s'éteint (onPause / onStop).
     * Met immédiatement la partie en pause et coupe tout le son (musique et effets).
     */
    fun onAppBackgrounded() {
        gameLoopJob?.cancel()
        resumeCountdownJob?.cancel()
        _resumeCountdown.value = null
        _gameState.update { state ->
            if (state.status == GameStatus.PLAYING) {
                state.copy(
                    status = GameStatus.PAUSED,
                    dpadDx = 0f,
                    dpadDy = 0f,
                    touchScreenX = null,
                    touchScreenY = null
                )
            } else state
        }
        AstroSoundManager.onAppBackgrounded()
    }

    /**
     * Appelé lorsque l'application revient au premier plan (onResume).
     */
    fun onAppForegrounded() {
        AstroSoundManager.onAppForegrounded()
        val currentStatus = _gameState.value.status
        if (_isSoundEnabled.value && currentStatus == GameStatus.PLAYING) {
            AstroSoundManager.ensureAmbientPlaying()
        }
    }

    fun toggleQuickInventoryInGame() {
        val current = _gameState.value
        if (current.status == GameStatus.QUICK_INVENTORY) {
            _gameState.update { it.copy(status = GameStatus.PLAYING) }
            startGameLoop()
        } else {
            gameLoopJob?.cancel()
            AstroSoundManager.updateAmbientState(false)
            _gameState.update {
                it.copy(
                    status = GameStatus.QUICK_INVENTORY,
                    dpadDx = 0f,
                    dpadDy = 0f,
                    touchScreenX = null,
                    touchScreenY = null
                )
            }
        }
    }

    fun updateTouchTarget(screenNormalizedX: Float?, screenNormalizedY: Float?) {
        _gameState.update { state ->
            state.copy(
                touchScreenX = screenNormalizedX?.coerceIn(0f, 1f),
                touchScreenY = screenNormalizedY?.coerceIn(0f, 1f)
            )
        }
        if (screenNormalizedX != null && _gameState.value.status == GameStatus.READY) {
            startOrResumeGame()
        }
    }

    fun setDirectionalInput(dx: Float, dy: Float) {
        _gameState.update { state ->
            state.copy(
                dpadDx = dx.coerceIn(-1f, 1f),
                dpadDy = dy.coerceIn(-1f, 1f),
                touchScreenX = null,
                touchScreenY = null
            )
        }
        if ((dx != 0f || dy != 0f) && _gameState.value.status == GameStatus.READY) {
            startOrResumeGame()
        }
    }

    fun nudgePlayer(dx: Float, dy: Float) {
        if (_gameState.value.status == GameStatus.READY) {
            startOrResumeGame()
        }
        _gameState.update { state ->
            val rawX = state.playerX + dx
            val rawY = state.playerY + dy
            val wrappedX = wrapCoord(rawX, WORLD_WIDTH)
            val wrappedY = wrapCoord(rawY, WORLD_HEIGHT)
            val didWrap = (wrappedX != rawX) || (wrappedY != rawY)
            val (newCamX, newCamY) = computeCameraForPlayer(
                playerX = wrappedX,
                playerY = wrappedY,
                currentCamX = state.cameraX,
                currentCamY = state.cameraY,
                snappedByWrap = didWrap
            )
            if (didWrap) {
                clearAndRecycleTrail()
                AstroSoundManager.playBorderWarp()
            }
            val wrapEdge = when {
                rawX < 0f -> "RIGHT"
                rawX >= WORLD_WIDTH -> "LEFT"
                rawY < 0f -> "BOTTOM"
                rawY >= WORLD_HEIGHT -> "TOP"
                else -> state.borderWrapEdge
            }
            state.copy(
                playerX = wrappedX,
                playerY = wrappedY,
                cameraX = newCamX,
                cameraY = newCamY,
                borderWrapTimer = if (didWrap) 0.65f else state.borderWrapTimer,
                borderWrapEdge = wrapEdge,
                trail = trailBuffer.toList(),
                touchScreenX = null,
                touchScreenY = null
            )
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        if (_isSoundEnabled.value) {
            AstroSoundManager.ensureAmbientPlaying()
        }
        gameLoopJob = viewModelScope.launch {
            var lastTimeNanos = System.nanoTime()
            while (isActive && _gameState.value.status == GameStatus.PLAYING) {
                delay(16L) // ~60 FPS
                val now = System.nanoTime()
                val dt = ((now - lastTimeNanos) / 1_000_000_000f).coerceIn(0.005f, 0.05f)
                lastTimeNanos = now
                stepPhysics(dt)
            }
        }
    }

    private fun wrapCoord(value: Float, max: Float): Float {
        return when {
            value < 0f -> (value % max) + max
            value >= max -> value % max
            else -> value
        }
    }

    private fun shortestToroidalDelta(from: Float, to: Float, size: Float): Float {
        var delta = to - from
        if (delta > size * 0.5f) delta -= size
        if (delta < -size * 0.5f) delta += size
        return delta
    }

    /**
     * Distance from point (px, py) to segment (ax, ay)-(bx, by) so fast movement never skips a text pickup.
     */
    private fun distancePointToSegment(
        px: Float,
        py: Float,
        ax: Float,
        ay: Float,
        bx: Float,
        by: Float
    ): Float {
        val abx = bx - ax
        val aby = by - ay
        val lenSq = abx * abx + aby * aby
        if (lenSq <= 1e-6f) return hypot(px - ax, py - ay)
        val t = (((px - ax) * abx + (py - ay) * aby) / lenSq).coerceIn(0f, 1f)
        val projX = ax + t * abx
        val projY = ay + t * aby
        return hypot(px - projX, py - projY)
    }

    /**
     * Scrolls the camera when the player sphere approaches the edge of the screen (edgeMargin),
     * or snaps/recenters smoothly when the player wraps around the map border to the opposite side.
     */
    private fun computeCameraForPlayer(
        playerX: Float,
        playerY: Float,
        currentCamX: Float,
        currentCamY: Float,
        snappedByWrap: Boolean
    ): Pair<Float, Float> {
        val maxCamX = WORLD_WIDTH - VIEWPORT_WIDTH
        val maxCamY = WORLD_HEIGHT - VIEWPORT_HEIGHT

        if (snappedByWrap) {
            val camX = (playerX - VIEWPORT_WIDTH * 0.5f).coerceIn(0f, maxCamX)
            val camY = (playerY - VIEWPORT_HEIGHT * 0.5f).coerceIn(0f, maxCamY)
            return Pair(camX, camY)
        }

        val edgeMargin = 0.24f
        var camX = currentCamX
        var camY = currentCamY

        val screenX = playerX - camX
        if (screenX < edgeMargin) {
            camX = (playerX - edgeMargin).coerceIn(0f, maxCamX)
        } else if (screenX > VIEWPORT_WIDTH - edgeMargin) {
            camX = (playerX - (VIEWPORT_WIDTH - edgeMargin)).coerceIn(0f, maxCamX)
        }

        val screenY = playerY - camY
        if (screenY < edgeMargin) {
            camY = (playerY - edgeMargin).coerceIn(0f, maxCamY)
        } else if (screenY > VIEWPORT_HEIGHT - edgeMargin) {
            camY = (playerY - (VIEWPORT_HEIGHT - edgeMargin)).coerceIn(0f, maxCamY)
        }

        return Pair(camX, camY)
    }

    private fun stepPhysics(dt: Float) {
        val state = _gameState.value
        if (state.status != GameStatus.PLAYING) return
        if (_isSoundEnabled.value) {
            AstroSoundManager.ensureAmbientPlaying()
        }

        val sphereColorOption: SphereColorOption = getSphereColorById(profile.value.selectedColorId)

        // 1. Update Player Sphere Velocity & Position (Contemplative Pace)
        val isSpeedBoosted = state.speedBoostTimer > 0f || state.elanDashTimer > 0f
        val isHomeostasisActive = state.homeostasisFieldTimer > 0f
        val maxSpeed = when {
            state.elanDashTimer > 0f -> 0.78f
            state.speedBoostTimer > 0f -> 0.72f
            else -> 0.48f
        }
        var vx = state.playerVx
        var vy = state.playerVy

        if (state.touchScreenX != null && state.touchScreenY != null) {
            val playerScreenX = state.playerX - state.cameraX
            val playerScreenY = state.playerY - state.cameraY

            var dirX = state.touchScreenX - playerScreenX
            var dirY = state.touchScreenY - playerScreenY

            // When holding finger near the screen border, keep pushing toward that border
            // so the player can effortlessly cross the map limit and wrap to the opposite side.
            if (state.touchScreenX < 0.14f) dirX -= 0.25f
            if (state.touchScreenX > 0.86f) dirX += 0.25f
            if (state.touchScreenY < 0.14f) dirY -= 0.25f
            if (state.touchScreenY > 0.86f) dirY += 0.25f

            val dist = hypot(dirX, dirY)
            if (dist > 0.006f) {
                val desiredSpeed = (dist * 3.6f).coerceIn(0.20f, maxSpeed)
                vx = (dirX / dist) * desiredSpeed
                vy = (dirY / dist) * desiredSpeed
            } else {
                vx *= 0.70f
                vy *= 0.70f
            }
        } else if (state.dpadDx != 0f || state.dpadDy != 0f) {
            val mag = hypot(state.dpadDx, state.dpadDy).coerceAtLeast(1f)
            vx = (state.dpadDx / mag) * maxSpeed * 0.88f
            vy = (state.dpadDy / mag) * maxSpeed * 0.88f
        } else if (state.elanDashTimer > 0f) {
            // Micro-sprint de dégagement (Élan & Action) polarisant la trajectoire vers la Pensée Pure la plus proche
            val nearestUnsaved = rescueTargetsBuffer.filter { !it.isSaved && !it.isDestroyed }.minByOrNull { t ->
                hypot(
                    shortestToroidalDelta(state.playerX, t.x, WORLD_WIDTH),
                    shortestToroidalDelta(state.playerY, t.y, WORLD_HEIGHT)
                )
            }
            if (nearestUnsaved != null) {
                val tdx = shortestToroidalDelta(state.playerX, nearestUnsaved.x, WORLD_WIDTH)
                val tdy = shortestToroidalDelta(state.playerY, nearestUnsaved.y, WORLD_HEIGHT)
                val tDist = hypot(tdx, tdy).coerceAtLeast(0.001f)
                vx = vx * 0.80f + (tdx / tDist) * maxSpeed * 0.20f
                vy = vy * 0.80f + (tdy / tDist) * maxSpeed * 0.20f
            } else {
                vx *= 0.92f
                vy *= 0.92f
            }
        } else {
            vx *= 0.84f
            vy *= 0.84f
        }

        // 1b. Effets des obstacles mentaux sur le mouvement de la sphère :
        // - Incapacité (Le Bourbier Mental) : divise la vitesse par 3, sauf si Hyper-Propulsion, Élan & Action, Homéostasie, Déphasage Contemplatif ou Arche de Plénitude est actif !
        val isPhaseShifted = state.phaseShiftTimer > 0f
        val isPlenitudeArkActive = state.plenitudeArkTimer > 0f
        val isFreeWillAegisActive = state.freeWillAegisTimer > 0f
        val isMetacognitionBeaconActive = state.metacognitionBeaconTimer > 0f
        val isHumilityAnchorActive = state.humilityAnchorTimer > 0f
        val isMyelinSheathActive = state.myelinSheathTimer > 0f
        var myelinShockAbsorberAvailableNow = state.myelinShockAbsorberAvailable && isMyelinSheathActive
        var bonusEnergyEarnedFromDefenses = 0
        var temporaryShieldGrantedByHumility = false
        var phaseShiftRelicBoostCount = 0

        val isInsideMentalMire = state.obstacles.any { obs ->
            obs.type == ObstacleType.MENTAL_MIRE &&
                hypot(
                    shortestToroidalDelta(obs.x, state.playerX, WORLD_WIDTH),
                    shortestToroidalDelta(obs.y, state.playerY, WORLD_HEIGHT)
                ) <= (obs.radius + 0.036f)
        }
        if (isInsideMentalMire && !isSpeedBoosted && !isHomeostasisActive && !isPhaseShifted && !isPlenitudeArkActive) {
            vx /= 3f
            vy /= 3f
        }

        // - Conformisme (La Nuée Grégaire) : ralentit temporairement la sphère (neutralisé par Homéostasie, Égide du Libre Arbitre, Déphasage ou Arche)
        if (state.conformismSlowTimer > 0f && !isHomeostasisActive && !isFreeWillAegisActive && !isPhaseShifted && !isPlenitudeArkActive) {
            vx *= 0.58f
            vy *= 0.58f
        }

        // - L'Ego (Le Miroir de Narcisse) : attire la sphère vers lui tant qu'il n'est pas paralysé (sauf si Ancrage d'Humilité, Déphasage ou Arche est actif)
        for (mirror in state.obstacles) {
            if (mirror.type == ObstacleType.NARCISSUS_MIRROR && mirror.paralyzedTimer <= 0f) {
                val mdx = shortestToroidalDelta(state.playerX, mirror.x, WORLD_WIDTH)
                val mdy = shortestToroidalDelta(state.playerY, mirror.y, WORLD_HEIGHT)
                val mDist = hypot(mdx, mdy)
                val pullRange = 0.58f
                if (mDist in 0.005f..pullRange) {
                    if (isHumilityAnchorActive) {
                        // Niveau 100 — Ancrage d'Humilité : si un Miroir de Narcisse tente d'aspirer la sphère, le lien s'inverse et octroie +1 Bouclier Stellaire temporaire (max 7)
                        if (humilityConvertedMirrorIds.add(mirror.id)) {
                            temporaryShieldGrantedByHumility = true
                        }
                    } else if (!isPhaseShifted && !isPlenitudeArkActive) {
                        val pullStrength = (1f - (mDist / pullRange)).coerceIn(0.18f, 1f) * 0.24f
                        vx += (mdx / mDist) * pullStrength
                        vy += (mdy / mDist) * pullStrength
                    }
                }
            }
        }

        val oldPlayerX = state.playerX
        val oldPlayerY = state.playerY
        val rawPlayerX = oldPlayerX + vx * dt
        val rawPlayerY = oldPlayerY + vy * dt

        // Snake-style toroidal wrap-around at map boundaries (0f..WORLD_WIDTH, 0f..WORLD_HEIGHT)
        val newPlayerX = wrapCoord(rawPlayerX, WORLD_WIDTH)
        val newPlayerY = wrapCoord(rawPlayerY, WORLD_HEIGHT)
        val didWrapAround = (newPlayerX != rawPlayerX) || (newPlayerY != rawPlayerY)

        // Update Camera (scrolls when approaching screen edge, snaps when wrapping to opposite side)
        val (newCameraX, newCameraY) = computeCameraForPlayer(
            playerX = newPlayerX,
            playerY = newPlayerY,
            currentCamX = state.cameraX,
            currentCamY = state.cameraY,
            snappedByWrap = didWrapAround
        )

        val playerRadius = 0.042f

        // 2. Update Trail in staging buffer (reset trail segment on wrap-around so it doesn't streak across the map)
        var newBorderWrapTimer = (state.borderWrapTimer - dt).coerceAtLeast(0f)
        var newBorderWrapEdge = state.borderWrapEdge
        if (didWrapAround) {
            clearAndRecycleTrail()
            newBorderWrapTimer = 0.65f
            newBorderWrapEdge = when {
                rawPlayerX < 0f -> "RIGHT"
                rawPlayerX >= WORLD_WIDTH -> "LEFT"
                rawPlayerY < 0f -> "BOTTOM"
                rawPlayerY >= WORLD_HEIGHT -> "TOP"
                else -> "LEFT"
            }
            AstroSoundManager.playBorderWarp()
            // Burst of dimensional warp particles at emergence point
            for (wIdx in 0 until 18) {
                val ang = (wIdx / 18f) * 6.2831855f
                val spd = 0.14f + (wIdx % 3) * 0.08f
                val pCol = when (wIdx % 3) {
                    0 -> Color(0xFFF43F5E) // PlasmaRose border color
                    1 -> CyberCyanGlow
                    else -> sphereColorOption.glowColor
                }
                spawnParticle(
                    x = newPlayerX,
                    y = newPlayerY,
                    vx = cos(ang) * spd,
                    vy = sin(ang) * spd,
                    color = pCol,
                    life = 0.75f
                )
            }
        } else {
            for (i in trailBuffer.lastIndex downTo 0) {
                val pt = trailBuffer[i]
                val faded = pt.alpha - dt * 2.8f
                if (faded <= 0.05f || i >= 17) {
                    trailBuffer.removeAt(i)
                } else {
                    trailBuffer[i] = pt.copy(alpha = faded)
                }
            }
        }
        trailBuffer.add(0, obtainTrailPoint(newPlayerX, newPlayerY, 0.85f))

        // 3. Update Particles in staging buffer
        for (i in particlesBuffer.lastIndex downTo 0) {
            val p = particlesBuffer[i]
            val nextLife = p.life - dt * 1.9f
            if (nextLife <= 0f) {
                particlesBuffer.removeAt(i)
            } else {
                particlesBuffer[i] = p.copy(
                    x = p.x + p.vx * dt,
                    y = p.y + p.vy * dt,
                    life = nextLife
                )
            }
        }

        var hitDetected = false
        var obstacleDestroyedScore = 0
        var obstacleDestroyedMsg: String? = null
        var conformismTriggeredThisFrame = false
        var narcissusEnergyDrainedThisFrame = 0

        var isHarmonizingAny = state.brainRelic?.isHarmonizing == true
        if (!isHarmonizingAny) {
            for (i in 0 until relicsBuffer.size) {
                if (relicsBuffer[i].isHarmonizing) {
                    isHarmonizingAny = true
                    break
                }
            }
        }
        val isPeaceAuraActive = state.peaceAuraTimer > 0f || state.showSphereRemedyMenu
        val peaceFactor = when {
            state.showSphereRemedyMenu -> 0.08f
            isPeaceAuraActive -> 0.22f
            isHarmonizingAny -> 0.18f
            else -> (1.0f - state.pacificationLevel * 0.45f).coerceIn(0.35f, 1.0f)
        }

        val plenitudeArkDomeRadius = 0.24f

        // Update Obstacles in-place (wrap around 4x map boundaries) & Check Collisions / Targeted Destruction
        for (i in obstaclesBuffer.lastIndex downTo 0) {
            val obs = obstaclesBuffer[i]
            val nextParalyzed = (obs.paralyzedTimer - dt).coerceAtLeast(0f)
            var nextMyelinSlow = (obs.myelinSlowTimer - dt).coerceAtLeast(0f)

            // Niveau 110 — Gaine de Myéline : tout obstacle qui traverse le Sillage Spirituel voit sa vitesse réduite de 65% et ses pulsations désamorcées pendant 4s
            if (isMyelinSheathActive && trailBuffer.size >= 2) {
                var touchedTrail = false
                for (tIdx in 0 until trailBuffer.size - 1) {
                    val pA = trailBuffer[tIdx]
                    val pB = trailBuffer[tIdx + 1]
                    if (hypot(pA.x - pB.x, pA.y - pB.y) < 0.35f) {
                        val dSeg = distancePointToSegment(obs.x, obs.y, pA.x, pA.y, pB.x, pB.y)
                        if (dSeg <= (obs.radius + 0.032f)) {
                            touchedTrail = true
                            break
                        }
                    }
                }
                if (touchedTrail) {
                    if (nextMyelinSlow <= 0.2f && obstacleDestroyedMsg == null) {
                        obstacleDestroyedMsg = "🧬 Gaine de Myéline : ${obs.type.displayName} isolé dans votre sillage (Vitesse -65% & pulsations désamorcées 4s) !"
                    }
                    nextMyelinSlow = 4.0f
                }
            }

            val isParalyzed = nextParalyzed > 0f
            val isMyelinSlowed = nextMyelinSlow > 0f
            val myelinSpeedFactor = if (isMyelinSlowed) 0.35f else 1.0f // Vitesse réduite de 65%
            val moveScaleObs = if (isParalyzed) 0f else (peaceFactor * myelinSpeedFactor)

            var nx = wrapCoord(obs.x + obs.vx * dt * moveScaleObs, WORLD_WIDTH)
            var ny = wrapCoord(obs.y + obs.vy * dt * moveScaleObs, WORLD_HEIGHT)
            var nvx = obs.vx
            var nvy = obs.vy
            var nRadius = obs.radius

            // Niveau 100 — Ancrage d'Humilité : inverse l'aimant du Miroir de Narcisse, créant une zone de répulsion douce qui écarte l'Ego de votre trajectoire
            if (isHumilityAnchorActive && obs.type == ObstacleType.NARCISSUS_MIRROR) {
                val mdx = shortestToroidalDelta(newPlayerX, nx, WORLD_WIDTH)
                val mdy = shortestToroidalDelta(newPlayerY, ny, WORLD_HEIGHT)
                val mDist = hypot(mdx, mdy)
                if (mDist in 0.001f..0.62f) {
                    val pushSpeed = (1f - (mDist / 0.62f)).coerceIn(0.25f, 1f) * 0.32f
                    nvx = (mdx / mDist) * pushSpeed
                    nvy = (mdy / mDist) * pushSpeed
                    nx = wrapCoord(nx + nvx * dt, WORLD_WIDTH)
                    ny = wrapCoord(ny + nvy * dt, WORLD_HEIGHT)
                }
            }

            // Pulsations désamorcées pendant 4s si affecté par la Gaine de Myéline
            val newPulse = if (isParalyzed || isMyelinSlowed) obs.pulsePhase else obs.pulsePhase + dt * 2.8f
            val effectiveObstacleRadius = if (obs.type == ObstacleType.PULSAR_MINE && !isParalyzed && !isMyelinSlowed) {
                nRadius * (1f + 0.22f * sin(newPulse))
            } else {
                nRadius
            }

            val dx = shortestToroidalDelta(nx, newPlayerX, WORLD_WIDTH)
            val dy = shortestToroidalDelta(ny, newPlayerY, WORLD_HEIGHT)
            val distToPlayer = hypot(dx, dy)
            val minSeparation = playerRadius + effectiveObstacleRadius * 0.85f

            // Niveau 130 — Arche de Plénitude : Sanctuaire d'Harmonisation mobile qui repousse automatiquement tous les obstacles mentaux à sa bordure extérieure
            val arkBorderDist = plenitudeArkDomeRadius + effectiveObstacleRadius
            if (isPlenitudeArkActive && distToPlayer < arkBorderDist) {
                val normX = if (distToPlayer > 0.001f) -dx / distToPlayer else 1f
                val normY = if (distToPlayer > 0.001f) -dy / distToPlayer else 0f
                val speed = hypot(nvx, nvy).coerceAtLeast(0.18f)
                nvx = normX * speed
                nvy = normY * speed
                nx = wrapCoord(newPlayerX + normX * (arkBorderDist + 0.015f), WORLD_WIDTH)
                ny = wrapCoord(newPlayerY + normY * (arkBorderDist + 0.015f), WORLD_HEIGHT)
                obstaclesBuffer[i] = obs.copy(
                    x = nx,
                    y = ny,
                    vx = nvx,
                    vy = nvy,
                    radius = nRadius,
                    rotation = (obs.rotation + obs.rotationSpeed * dt * moveScaleObs) % 360f,
                    pulsePhase = newPulse,
                    paralyzedTimer = nextParalyzed,
                    myelinSlowTimer = nextMyelinSlow
                )
                continue
            }

            val isInsideConformismDashedZone = obs.type == ObstacleType.CONFORMISM_SWARM &&
                kotlin.math.abs(dx) <= (nRadius * 2.65f + playerRadius * 0.65f) &&
                kotlin.math.abs(dy) <= (nRadius * 1.95f + playerRadius * 0.65f)

            var removeObstacle = false
            if (distToPlayer < minSeparation || isInsideConformismDashedZone) {
                // Niveau 120 — Déphasage Contemplatif (Intangibilité Thêta) : traverse tous les obstacles sans dégâts ni ralentissement,
                // divise leur taille et leur vitesse par deux et accélère de +25% la jauge des Sanctuaires d'Harmonisation à proximité
                if (isPhaseShifted) {
                    if (phaseShiftedObstacleIds.add(obs.id)) {
                        nRadius = (nRadius * 0.5f).coerceAtLeast(0.018f)
                        nvx *= 0.5f
                        nvy *= 0.5f
                        phaseShiftRelicBoostCount++
                        obstacleDestroyedScore += 45
                        obstacleDestroyedMsg = "👻 Déphasage Contemplatif : ${obs.effectiveDisplayName} apaisé (taille & vitesse ÷2, Sanctuaires proches +25%) !"
                        for (p in 0 until 12) {
                            val ang = (p / 12f) * 6.28f
                            spawnParticle(nx, ny, cos(ang) * 0.18f, sin(ang) * 0.18f, Color(0xFFF3E8FF), 0.85f)
                        }
                    }
                } else {
                    when (obs.type) {
                        ObstacleType.ASTEROID,
                        ObstacleType.LASER_DRONE,
                        ObstacleType.PULSAR_MINE -> {
                            val canDestroy = when (obs.type) {
                                ObstacleType.ASTEROID -> state.antiAsteroidTimer > 0f
                                ObstacleType.LASER_DRONE -> state.antiDroneTimer > 0f
                                ObstacleType.PULSAR_MINE -> state.antiMineTimer > 0f
                                else -> false
                            }

                            if (canDestroy) {
                                val pts: Int
                                val msg: String
                                val pColor: Color
                                val obsName = obs.effectiveDisplayName
                                when (obs.type) {
                                    ObstacleType.ASTEROID -> {
                                        pts = 60
                                        msg = "🌿 Obstacle « $obsName » dissipé (+60 pts) !"
                                        pColor = EmeraldNovaGlow
                                    }
                                    ObstacleType.LASER_DRONE -> {
                                        pts = 80
                                        msg = "💠 Obstacle « $obsName » apaisé (+80 pts) !"
                                        pColor = CyberCyanGlow
                                    }
                                    else -> {
                                        pts = 100
                                        msg = "🌊 Obstacle « $obsName » neutralisé (+100 pts) !"
                                        pColor = CoralHyperGlow
                                    }
                                }
                                obstacleDestroyedScore += pts
                                obstacleDestroyedMsg = msg

                                for (p in 0 until 16) {
                                    val ang = (p / 16f) * 6.28f
                                    spawnParticle(
                                        x = nx,
                                        y = ny,
                                        vx = cos(ang) * 0.25f,
                                        vy = sin(ang) * 0.25f,
                                        color = pColor,
                                        life = 0.95f
                                    )
                                }
                                removeObstacle = true
                            } else if (isFreeWillAegisActive && obs.type == ObstacleType.LASER_DRONE) {
                                // Niveau 40 — Égide du Libre Arbitre : tout choc contre un Foyer d'Angoisse fait rebondir l'obstacle au loin sans consommer de bouclier et génère +5 pts d'énergie
                                val normX = if (distToPlayer > 0.001f) -dx / distToPlayer else 1f
                                val normY = if (distToPlayer > 0.001f) -dy / distToPlayer else 0f
                                val bounceSpeed = hypot(nvx, nvy).coerceAtLeast(0.28f) * 1.45f
                                nvx = normX * bounceSpeed
                                nvy = normY * bounceSpeed
                                nx = wrapCoord(newPlayerX + normX * (minSeparation + 0.065f), WORLD_WIDTH)
                                ny = wrapCoord(newPlayerY + normY * (minSeparation + 0.065f), WORLD_HEIGHT)
                                bonusEnergyEarnedFromDefenses += 5
                                obstacleDestroyedScore += 40
                                obstacleDestroyedMsg = "🔷 Égide du Libre Arbitre : « ${obs.effectiveDisplayName} » réfracté au loin (+5 pts d'énergie de purification) !"
                                AstroSoundManager.playPrismInteraction(shattered = false)
                                for (p in 0 until 14) {
                                    val ang = (p / 14f) * 6.28f
                                    spawnParticle(nx, ny, cos(ang) * 0.22f, sin(ang) * 0.22f, Color(0xFFE0F2FE), 0.85f)
                                }
                            } else if (myelinShockAbsorberAvailableNow) {
                                // Niveau 110 — Gaine de Myéline (Amortisseur de choc) : le 1er impact direct est intégralement absorbé sans perdre de vie + micro-onde de recul
                                myelinShockAbsorberAvailableNow = false
                                val normX = if (distToPlayer > 0.001f) -dx / distToPlayer else 1f
                                val normY = if (distToPlayer > 0.001f) -dy / distToPlayer else 0f
                                val speed = hypot(nvx, nvy).coerceAtLeast(0.24f)
                                nvx = normX * speed * 1.35f
                                nvy = normY * speed * 1.35f
                                nx = wrapCoord(newPlayerX + normX * (minSeparation + 0.08f), WORLD_WIDTH)
                                ny = wrapCoord(newPlayerY + normY * (minSeparation + 0.08f), WORLD_HEIGHT)
                                obstacleDestroyedMsg = "🧬 Amortisseur de Gaine de Myéline : Choc direct absorbé sans perte de vie + Micro-onde de recul !"
                                for (k in 0 until obstaclesBuffer.size) {
                                    if (k == i) continue
                                    val other = obstaclesBuffer[k]
                                    val odx = shortestToroidalDelta(newPlayerX, other.x, WORLD_WIDTH)
                                    val ody = shortestToroidalDelta(newPlayerY, other.y, WORLD_HEIGHT)
                                    val oDist = hypot(odx, ody)
                                    if (oDist in 0.001f..0.45f) {
                                        val pushX = (odx / oDist) * 0.14f
                                        val pushY = (ody / oDist) * 0.14f
                                        obstaclesBuffer[k] = other.copy(
                                            x = wrapCoord(other.x + pushX, WORLD_WIDTH),
                                            y = wrapCoord(other.y + pushY, WORLD_HEIGHT),
                                            myelinSlowTimer = maxOf(other.myelinSlowTimer, 4.0f)
                                        )
                                    }
                                }
                                for (p in 0 until 18) {
                                    val ang = (p / 18f) * 6.28f
                                    spawnParticle(newPlayerX, newPlayerY, cos(ang) * 0.26f, sin(ang) * 0.26f, Color(0xFF99F6E4), 0.9f)
                                }
                            } else if (!isParalyzed && !isPeaceAuraActive && !isHarmonizingAny && state.invulnerableTimer <= 0f) {
                                hitDetected = true
                                val normX = if (distToPlayer > 0.001f) -dx / distToPlayer else 1f
                                val normY = if (distToPlayer > 0.001f) -dy / distToPlayer else 0f
                                val speed = hypot(nvx, nvy).coerceAtLeast(0.14f)
                                nvx = normX * speed
                                nvy = normY * speed
                                nx = wrapCoord(newPlayerX + normX * (minSeparation + 0.025f), WORLD_WIDTH)
                                ny = wrapCoord(newPlayerY + normY * (minSeparation + 0.025f), WORLD_HEIGHT)
                            } else {
                                val normX = if (distToPlayer > 0.001f) -dx / distToPlayer else 1f
                                val normY = if (distToPlayer > 0.001f) -dy / distToPlayer else 0f
                                nx = wrapCoord(newPlayerX + normX * (minSeparation + 0.035f), WORLD_WIDTH)
                                ny = wrapCoord(newPlayerY + normY * (minSeparation + 0.035f), WORLD_HEIGHT)
                            }
                        }

                        ObstacleType.CONFORMISM_SWARM -> {
                            if (!isHomeostasisActive && !isFreeWillAegisActive && !isPhaseShifted && !isPlenitudeArkActive) {
                                conformismTriggeredThisFrame = true
                            } else if (isFreeWillAegisActive && state.bannerTimer <= 0.5f) {
                                obstacleDestroyedMsg = "🔷 Égide du Libre Arbitre : Vos bonus actifs (Turbo, Aimant) sont immunisés contre la Nuée Grégaire !"
                            }
                        }

                        ObstacleType.DOGMATIC_PRISM -> {
                            if (isFreeWillAegisActive) {
                                // Niveau 40 — Égide du Libre Arbitre : tout choc contre un Prisme Dogmatique fait rebondir l'obstacle au loin sans consommer de bouclier et génère +5 pts d'énergie
                                val normX = if (distToPlayer > 0.001f) -dx / distToPlayer else 1f
                                val normY = if (distToPlayer > 0.001f) -dy / distToPlayer else 0f
                                val bounceSpeed = hypot(nvx, nvy).coerceAtLeast(0.28f) * 1.45f
                                nvx = normX * bounceSpeed
                                nvy = normY * bounceSpeed
                                nx = wrapCoord(newPlayerX + normX * (minSeparation + 0.065f), WORLD_WIDTH)
                                ny = wrapCoord(newPlayerY + normY * (minSeparation + 0.065f), WORLD_HEIGHT)
                                bonusEnergyEarnedFromDefenses += 5
                                obstacleDestroyedScore += 50
                                obstacleDestroyedMsg = "🔷 Égide du Libre Arbitre : Prisme Dogmatique repoussé au loin (+5 pts d'énergie de purification) !"
                                AstroSoundManager.playPrismInteraction(shattered = false)
                                for (p in 0 until 14) {
                                    val ang = (p / 14f) * 6.28f
                                    spawnParticle(nx, ny, cos(ang) * 0.22f, sin(ang) * 0.22f, Color(0xFFE0F2FE), 0.85f)
                                }
                            } else if (myelinShockAbsorberAvailableNow) {
                                myelinShockAbsorberAvailableNow = false
                                val normX = if (distToPlayer > 0.001f) -dx / distToPlayer else 1f
                                val normY = if (distToPlayer > 0.001f) -dy / distToPlayer else 0f
                                val speed = hypot(nvx, nvy).coerceAtLeast(0.24f)
                                nvx = normX * speed * 1.35f
                                nvy = normY * speed * 1.35f
                                nx = wrapCoord(newPlayerX + normX * (minSeparation + 0.08f), WORLD_WIDTH)
                                ny = wrapCoord(newPlayerY + normY * (minSeparation + 0.08f), WORLD_HEIGHT)
                                obstacleDestroyedMsg = "🧬 Amortisseur de Gaine de Myéline : Impact du Prisme Dogmatique absorbé sans perdre de vie !"
                                for (p in 0 until 18) {
                                    val ang = (p / 18f) * 6.28f
                                    spawnParticle(newPlayerX, newPlayerY, cos(ang) * 0.26f, sin(ang) * 0.26f, Color(0xFF99F6E4), 0.9f)
                                }
                            } else {
                                if (!isParalyzed && !isPeaceAuraActive && !isHarmonizingAny && state.invulnerableTimer <= 0f) {
                                    hitDetected = true
                                    obstacleDestroyedMsg = "💎 Le Prisme Dogmatique est inébranlable ! Utilisez « Vérité & Discernement », « Égide du Libre Arbitre » ou une Onde Nova !"
                                }
                                val normX = if (distToPlayer > 0.001f) -dx / distToPlayer else 1f
                                val normY = if (distToPlayer > 0.001f) -dy / distToPlayer else 0f
                                nx = wrapCoord(newPlayerX + normX * (minSeparation + 0.028f), WORLD_WIDTH)
                                ny = wrapCoord(newPlayerY + normY * (minSeparation + 0.028f), WORLD_HEIGHT)
                            }
                        }

                        ObstacleType.OBSCURANTISM_VEIL -> Unit

                        ObstacleType.MENTAL_MIRE -> {
                            if (state.elanDashTimer > 0f) {
                                // Niveau 50 (Élan & Action) : dissout instantanément la viscosité d'un Bourbier Mental traversé !
                                obstacleDestroyedScore += 85
                                obstacleDestroyedMsg = "🔥 Élan Vital : Viscosité du Bourbier Mental dissoute instantanément (+85 pts) !"
                                for (p in 0 until 16) {
                                    val ang = (p / 16f) * 6.28f
                                    spawnParticle(nx, ny, cos(ang) * 0.24f, sin(ang) * 0.24f, Color(0xFFFB923C), 0.9f)
                                }
                                removeObstacle = true
                            } else if (!isSpeedBoosted && !isHomeostasisActive && state.bannerTimer <= 0.4f) {
                                obstacleDestroyedMsg = "🕸️ Bourbier Mental : Vitesse divisée par 3 ! (Utilisez « Élan & Action » 🔥, « Déphasage » 👻 ou l'Hyper-Propulsion)"
                            }
                        }

                        ObstacleType.NARCISSUS_MIRROR -> {
                            if (isHumilityAnchorActive) {
                                // Niveau 100 — Ancrage d'Humilité : verrouille votre énergie contre tout drainage et repousse le Miroir
                                val normX = if (distToPlayer > 0.001f) -dx / distToPlayer else 1f
                                val normY = if (distToPlayer > 0.001f) -dy / distToPlayer else 0f
                                nx = wrapCoord(newPlayerX + normX * (minSeparation + 0.08f), WORLD_WIDTH)
                                ny = wrapCoord(newPlayerY + normY * (minSeparation + 0.08f), WORLD_HEIGHT)
                                if (humilityConvertedMirrorIds.add(obs.id)) {
                                    temporaryShieldGrantedByHumility = true
                                }
                                obstacleDestroyedMsg = "⚓ Ancrage d'Humilité : Énergie verrouillée et Miroir de Narcisse repoussé (+1 🛡️ Bouclier Stellaire) !"
                            } else if (myelinShockAbsorberAvailableNow) {
                                myelinShockAbsorberAvailableNow = false
                                obstacleDestroyedMsg = "🧬 Amortisseur de Gaine de Myéline : Drainage du Miroir de Narcisse bloqué !"
                                removeObstacle = true
                            } else if (!isParalyzed && !isPeaceAuraActive && !isHarmonizingAny && state.invulnerableTimer <= 0f) {
                                if (state.purificationEnergyPoints >= 10) {
                                    narcissusEnergyDrainedThisFrame = minOf(20, state.purificationEnergyPoints)
                                    obstacleDestroyedMsg = "🪞 Miroir de Narcisse (L'Ego) ! Le faux astre a absorbé -$narcissusEnergyDrainedThisFrame pts d'énergie de purification !"
                                } else {
                                    hitDetected = true
                                    obstacleDestroyedMsg = "🪞 Miroir de Narcisse (L'Ego) ! Piège d'illusion : -1 ❤️ de bouclier drainé !"
                                }
                                for (p in 0 until 16) {
                                    val ang = (p / 16f) * 6.28f
                                    spawnParticle(
                                        x = nx,
                                        y = ny,
                                        vx = cos(ang) * 0.24f,
                                        vy = sin(ang) * 0.24f,
                                        color = SolarGoldGlow,
                                        life = 0.9f
                                    )
                                }
                                removeObstacle = true
                            }
                        }
                    }
                }
            }

            if (removeObstacle) {
                obstaclesBuffer.removeAt(i)
            } else {
                obstaclesBuffer[i] = obs.copy(
                    x = nx,
                    y = ny,
                    vx = nvx,
                    vy = nvy,
                    radius = nRadius,
                    rotation = (obs.rotation + obs.rotationSpeed * dt * moveScaleObs) % 360f,
                    pulsePhase = newPulse,
                    paralyzedTimer = nextParalyzed,
                    myelinSlowTimer = nextMyelinSlow
                )
            }
        }

        // 4. Update Floating Text Relics (A, B, C) in staging buffer with Sanctuaire d'Harmonisation
        newlyCollectedRelicsBuffer.clear()
        val isMagnetActiveForRelics = state.magnetTimer > 0f
        val relicAttractionDist = if (isMagnetActiveForRelics) 0.85f else 0.24f
        val relicAttractionSpeed = if (isMagnetActiveForRelics) 0.65f else 0.26f
        val harmonizationSpeedMult = if (isPlenitudeArkActive) 2.0f else 1.0f // Arche de Plénitude : harmonise 2x plus vite

        for (i in 0 until relicsBuffer.size) {
            val relic = relicsBuffer[i]
            if (!relic.isCollected) {
                val nextPhase = relic.phase + dt * 2.5f
                var rx = relic.x + relic.vx * dt * peaceFactor
                var ry = relic.y + relic.vy * dt * peaceFactor
                var rvx = relic.vx
                var rvy = relic.vy

                if (rx < 0.12f || rx > WORLD_WIDTH - 0.12f) {
                    rvx = -rvx
                    rx = rx.coerceIn(0.12f, WORLD_WIDTH - 0.12f)
                }
                if (ry < 0.12f || ry > WORLD_HEIGHT - 0.12f) {
                    rvy = -rvy
                    ry = ry.coerceIn(0.12f, WORLD_HEIGHT - 0.12f)
                }

                val visualRy = ry + sin(nextPhase) * 0.012f
                val distCenter = hypot(newPlayerX - rx, newPlayerY - visualRy)
                if (distCenter < relicAttractionDist && distCenter > 0.001f) {
                    rx += ((newPlayerX - rx) / distCenter) * dt * relicAttractionSpeed
                    ry += ((newPlayerY - visualRy) / distCenter) * dt * relicAttractionSpeed
                }

                val distSwept = if (!didWrapAround) {
                    minOf(
                        distancePointToSegment(rx, ry, oldPlayerX, oldPlayerY, newPlayerX, newPlayerY),
                        distancePointToSegment(rx, visualRy, oldPlayerX, oldPlayerY, newPlayerX, newPlayerY)
                    )
                } else {
                    minOf(hypot(newPlayerX - rx, newPlayerY - ry), hypot(newPlayerX - rx, newPlayerY - visualRy))
                }

                val effectiveSanctuaryRadius = if (isPlenitudeArkActive) maxOf(relic.sanctuaryRadius, plenitudeArkDomeRadius) else relic.sanctuaryRadius
                val isInsideSanctuary = distSwept <= effectiveSanctuaryRadius || distCenter <= effectiveSanctuaryRadius
                // Niveau 120 — Déphasage Contemplatif : +25% à la jauge des Sanctuaires d'Harmonisation à proximité lorsqu'un obstacle est traversé
                val phaseShiftBoost = if (phaseShiftRelicBoostCount > 0 && distCenter <= 0.68f) (0.25f * phaseShiftRelicBoostCount) else 0f
                val nextProgress = if (isInsideSanctuary || phaseShiftBoost > 0f) {
                    (relic.harmonizationProgress + (if (isInsideSanctuary) (dt / 1.35f) * harmonizationSpeedMult else 0f) + phaseShiftBoost).coerceIn(0f, 1f)
                } else {
                    (relic.harmonizationProgress - dt * 0.40f).coerceAtLeast(0f)
                }

                val isNowCollected = nextProgress >= 1f
                val updatedRelic = relic.copy(
                    x = rx,
                    y = ry,
                    vx = rvx,
                    vy = rvy,
                    isSpawned = true,
                    phase = nextPhase,
                    isCollected = isNowCollected,
                    harmonizationProgress = if (isNowCollected) 1f else nextProgress,
                    isHarmonizing = if (isNowCollected) false else isInsideSanctuary
                )
                relicsBuffer[i] = updatedRelic
                if (isNowCollected) {
                    newlyCollectedRelicsBuffer.add(updatedRelic)
                }
            }
        }

        // 4b. Update Brain Bonus Relic (🧠) with Sanctuaire d'Harmonisation
        var updatedBrainRelic = state.brainRelic
        var newlyCollectedBrainRelic: FloatingTextRelic? = null

        if (updatedBrainRelic != null && !updatedBrainRelic.isCollected) {
            val b = updatedBrainRelic
            val nextPhase = b.phase + dt * 2.8f
            var bx = b.x + b.vx * dt * peaceFactor
            var by = b.y + b.vy * dt * peaceFactor
            var bvx = b.vx
            var bvy = b.vy

            if (bx < 0.12f || bx > WORLD_WIDTH - 0.12f) {
                bvx = -bvx
                bx = bx.coerceIn(0.12f, WORLD_WIDTH - 0.12f)
            }
            if (by < 0.12f || by > WORLD_HEIGHT - 0.12f) {
                bvy = -bvy
                by = by.coerceIn(0.12f, WORLD_HEIGHT - 0.12f)
            }

            val visualBy = by + sin(nextPhase) * 0.014f
            val attractionDist = if (isMagnetActiveForRelics) 0.85f else 0.24f
            val attractionSpeed = if (isMagnetActiveForRelics) 0.65f else 0.28f
            val distCenter = hypot(newPlayerX - bx, newPlayerY - visualBy)
            if (distCenter < attractionDist && distCenter > 0.001f) {
                bx += ((newPlayerX - bx) / distCenter) * dt * attractionSpeed
                by += ((newPlayerY - visualBy) / distCenter) * dt * attractionSpeed
            }

            val distSwept = if (!didWrapAround) {
                minOf(
                    distancePointToSegment(bx, by, oldPlayerX, oldPlayerY, newPlayerX, newPlayerY),
                    distancePointToSegment(bx, visualBy, oldPlayerX, oldPlayerY, newPlayerX, newPlayerY)
                )
            } else {
                minOf(hypot(newPlayerX - bx, newPlayerY - by), hypot(newPlayerX - bx, newPlayerY - visualBy))
            }

            val effectiveSanctuaryRadius = if (isPlenitudeArkActive) maxOf(b.sanctuaryRadius, plenitudeArkDomeRadius) else b.sanctuaryRadius
            val isInsideSanctuary = distSwept <= effectiveSanctuaryRadius || distCenter <= effectiveSanctuaryRadius
            val phaseShiftBoost = if (phaseShiftRelicBoostCount > 0 && distCenter <= 0.68f) (0.25f * phaseShiftRelicBoostCount) else 0f
            val nextProgress = if (isInsideSanctuary || phaseShiftBoost > 0f) {
                (b.harmonizationProgress + (if (isInsideSanctuary) (dt / 1.35f) * harmonizationSpeedMult else 0f) + phaseShiftBoost).coerceIn(0f, 1f)
            } else {
                (b.harmonizationProgress - dt * 0.40f).coerceAtLeast(0f)
            }

            val isNowCollected = nextProgress >= 1f
            updatedBrainRelic = b.copy(
                x = bx,
                y = by,
                vx = bvx,
                vy = bvy,
                isSpawned = true,
                phase = nextPhase,
                isCollected = isNowCollected,
                harmonizationProgress = if (isNowCollected) 1f else nextProgress,
                isHarmonizing = if (isNowCollected) false else isInsideSanctuary
            )
            if (isNowCollected) {
                newlyCollectedBrainRelic = updatedBrainRelic
            }
        }

        // 5. Update Power-Ups in staging buffer (drift, toroidal wrap) & Check Player Collision
        newlyCollectedPowerUpsBuffer.clear()
        val pupRadius = playerRadius + 0.055f
        val pupAttractionDist = if (state.magnetTimer > 0f) 0.85f else 0.28f
        val pupPullSpeed = if (state.magnetTimer > 0f) 0.65f else 0.42f
        var activePupCount = 0
        var maxPowerUpId = 0

        for (i in 0 until powerUpsBuffer.size) {
            val pup = powerUpsBuffer[i]
            if (pup.id > maxPowerUpId) maxPowerUpId = pup.id
            if (!pup.isCollected) {
                val nextPhase = pup.phase + dt * 3.2f
                var px = wrapCoord(pup.x + pup.vx * dt, WORLD_WIDTH)
                var py = wrapCoord(pup.y + pup.vy * dt, WORLD_HEIGHT)

                val dx = shortestToroidalDelta(px, newPlayerX, WORLD_WIDTH)
                val dy = shortestToroidalDelta(py, newPlayerY, WORLD_HEIGHT)
                val dist = hypot(dx, dy)

                if (dist < pupAttractionDist && dist > 0.001f) {
                    px = wrapCoord(px + (dx / dist) * dt * pupPullSpeed, WORLD_WIDTH)
                    py = wrapCoord(py + (dy / dist) * dt * pupPullSpeed, WORLD_HEIGHT)
                }

                val finalDx = shortestToroidalDelta(px, newPlayerX, WORLD_WIDTH)
                val finalDy = shortestToroidalDelta(py, newPlayerY, WORLD_HEIGHT)
                val finalDist = hypot(finalDx, finalDy)

                val distSwept = if (!didWrapAround) {
                    distancePointToSegment(px, py, oldPlayerX, oldPlayerY, newPlayerX, newPlayerY)
                } else {
                    finalDist
                }

                val isCollectedNow = distSwept <= pupRadius || finalDist <= pupRadius
                val updatedPup = pup.copy(
                    x = px,
                    y = py,
                    phase = nextPhase,
                    isCollected = isCollectedNow
                )
                powerUpsBuffer[i] = updatedPup

                if (isCollectedNow) {
                    newlyCollectedPowerUpsBuffer.add(updatedPup)
                } else {
                    activePupCount++
                }
            }
        }

        // Periodic Power-Up spawning if map has fewer than 4 active power-ups
        if (activePupCount < 4 && Random.nextFloat() < (dt * 0.12f)) {
            val unlockedPupTypes = PowerUpType.entries.filter { state.level.levelNumber >= it.unlockLevel }
            val randomType = unlockedPupTypes[Random.nextInt(unlockedPupTypes.size)]
            val spawnX = wrapCoord(newPlayerX + 0.6f + Random.nextFloat() * (WORLD_WIDTH - 1.2f), WORLD_WIDTH)
            val spawnY = wrapCoord(newPlayerY + 0.6f + Random.nextFloat() * (WORLD_HEIGHT - 1.2f), WORLD_HEIGHT)
            val driftAngle = Random.nextFloat() * 6.28f
            powerUpsBuffer.add(
                SpacePowerUp(
                    id = maxPowerUpId + 1,
                    type = randomType,
                    x = spawnX,
                    y = spawnY,
                    vx = cos(driftAngle) * 0.015f,
                    vy = sin(driftAngle) * 0.015f,
                    phase = 0f
                )
            )
        }

        var newRunScore = state.runScore
        var newBannerMsg = state.bannerMessage
        var newBannerTimer = (state.bannerTimer - dt).coerceAtLeast(0f)
        if (newBannerTimer <= 0f) newBannerMsg = null

        var newShields = state.shields
        var newMaxShields = state.maxShields.coerceIn(3, 7)
        var newInvuln = (state.invulnerableTimer - dt).coerceAtLeast(0f)
        var newSpeedBoostTimer = (state.speedBoostTimer - dt).coerceAtLeast(0f)
        var newMagnetTimer = (state.magnetTimer - dt).coerceAtLeast(0f)
        var newAntiAsteroidTimer = (state.antiAsteroidTimer - dt).coerceAtLeast(0f)
        var newAntiDroneTimer = (state.antiDroneTimer - dt).coerceAtLeast(0f)
        var newAntiMineTimer = (state.antiMineTimer - dt).coerceAtLeast(0f)
        var newPeaceAuraTimer = (state.peaceAuraTimer - dt).coerceAtLeast(0f)
        var newConformismSlowTimer = if (isHomeostasisActive || isFreeWillAegisActive || isPhaseShifted || isPlenitudeArkActive) 0f else (state.conformismSlowTimer - dt).coerceAtLeast(0f)
        var newHomeostasisFieldTimer = (state.homeostasisFieldTimer - dt).coerceAtLeast(0f)
        var newElanDashTimer = (state.elanDashTimer - dt).coerceAtLeast(0f)
        var newFreeWillAegisTimer = (state.freeWillAegisTimer - dt).coerceAtLeast(0f)
        var newMetacognitionBeaconTimer = (state.metacognitionBeaconTimer - dt).coerceAtLeast(0f)
        var newHumilityAnchorTimer = (state.humilityAnchorTimer - dt).coerceAtLeast(0f)
        var newMyelinSheathTimer = (state.myelinSheathTimer - dt).coerceAtLeast(0f)
        var newMyelinShockAbsorber = myelinShockAbsorberAvailableNow && newMyelinSheathTimer > 0f
        var newPhaseShiftTimer = (state.phaseShiftTimer - dt).coerceAtLeast(0f)
        var newPlenitudeArkTimer = (state.plenitudeArkTimer - dt).coerceAtLeast(0f)

        if (temporaryShieldGrantedByHumility) {
            newShields = (newShields + 1).coerceAtMost(7)
            newMaxShields = maxOf(newMaxShields, newShields)
            newBannerMsg = "⚓ Ancrage d'Humilité : Lien du Miroir de Narcisse inversé → +1 Bouclier Stellaire ($newShields/$newMaxShields) !"
            newBannerTimer = 2.8f
        }

        var collectedRelicCount = 0
        for (i in 0 until relicsBuffer.size) {
            if (relicsBuffer[i].isCollected) collectedRelicCount++
        }
        val newPacificationLevel = (collectedRelicCount / 3f).coerceIn(0f, 1f)
        var nextStatus = state.status

        if (conformismTriggeredThisFrame) {
            if (newFreeWillAegisTimer > 0f) {
                if (newBannerTimer <= 0.6f) {
                    newBannerMsg = "🔷 Égide du Libre Arbitre : Vos bonus actifs (Turbo, Aimant) sont immunisés contre la Nuée Grégaire !"
                    newBannerTimer = 2.2f
                }
            } else if (newHomeostasisFieldTimer <= 0f && newPhaseShiftTimer <= 0f && newPlenitudeArkTimer <= 0f) {
                val hadActiveBonus = newSpeedBoostTimer > 0f || newMagnetTimer > 0f ||
                    newAntiAsteroidTimer > 0f || newAntiDroneTimer > 0f ||
                    newAntiMineTimer > 0f || newPeaceAuraTimer > 0f
                newSpeedBoostTimer = 0f
                newMagnetTimer = 0f
                newAntiAsteroidTimer = 0f
                newAntiDroneTimer = 0f
                newAntiMineTimer = 0f
                newPeaceAuraTimer = 0f
                newConformismSlowTimer = 3.5f
                if (hadActiveBonus || newBannerTimer <= 0.6f) {
                    newBannerMsg = if (hadActiveBonus) {
                        "🐑 La Nuée Grégaire (Conformisme) a absorbé vos bonus actifs et vous ralentit !"
                    } else {
                        "🐑 La Nuée Grégaire vous force à rentrer dans le rang (Ralentissement) !"
                    }
                    newBannerTimer = 2.6f
                }
            }
        }

        if (obstacleDestroyedScore > 0) {
            newRunScore += obstacleDestroyedScore
        }
        if (obstacleDestroyedMsg != null) {
            newBannerMsg = obstacleDestroyedMsg
            newBannerTimer = 2.4f
        }

        // 5b. Update Rescue Targets in-place (Étincelles de Lucidité, Fragments de Savoir, Souvenirs)
        newlySavedTargetsBuffer.clear()
        newlyDestroyedTargetsBuffer.clear()
        val targetPickupRadius = playerRadius + 0.048f
        val targetPullRange = if (newMagnetTimer > 0f) 0.75f else 0.22f
        val targetPullSpeed = if (newMagnetTimer > 0f) 0.58f else 0.32f

        for (i in 0 until rescueTargetsBuffer.size) {
            val target = rescueTargetsBuffer[i]
            if (!target.isSaved && !target.isDestroyed) {
                val nextPhase = target.phase + dt * 3.0f
                var tx = wrapCoord(target.x + target.vx * dt * peaceFactor, WORLD_WIDTH)
                var ty = wrapCoord(target.y + target.vy * dt * peaceFactor, WORLD_HEIGHT)

                val dxPlayer = shortestToroidalDelta(tx, newPlayerX, WORLD_WIDTH)
                val dyPlayer = shortestToroidalDelta(ty, newPlayerY, WORLD_HEIGHT)
                val distPlayer = hypot(dxPlayer, dyPlayer)
                if (distPlayer < targetPullRange && distPlayer > 0.001f) {
                    tx = wrapCoord(tx + (dxPlayer / distPlayer) * dt * targetPullSpeed, WORLD_WIDTH)
                    ty = wrapCoord(ty + (dyPlayer / distPlayer) * dt * targetPullSpeed, WORLD_HEIGHT)
                }

                val finalDx = shortestToroidalDelta(tx, newPlayerX, WORLD_WIDTH)
                val finalDy = shortestToroidalDelta(ty, newPlayerY, WORLD_HEIGHT)
                val finalDistPlayer = hypot(finalDx, finalDy)
                val sweptDistPlayer = if (!didWrapAround) {
                    distancePointToSegment(tx, ty, oldPlayerX, oldPlayerY, newPlayerX, newPlayerY)
                } else {
                    finalDistPlayer
                }

                // Arche de Plénitude (Niv. 130) : Toutes les Étincelles de Lucidité, Fragments de Savoir et Souvenirs
                // touchés par le périmètre du dôme sont instantanément recueillis à distance.
                val touchedByPlenitudeArk = newPlenitudeArkTimer > 0f &&
                    finalDistPlayer <= (plenitudeArkDomeRadius + target.radius)

                val isSavedNow = sweptDistPlayer <= targetPickupRadius ||
                    finalDistPlayer <= targetPickupRadius ||
                    touchedByPlenitudeArk
                if (isSavedNow) {
                    val savedTarget = target.copy(
                        x = tx,
                        y = ty,
                        phase = nextPhase,
                        isSaved = true
                    )
                    rescueTargetsBuffer[i] = savedTarget
                    newlySavedTargetsBuffer.add(savedTarget)
                } else {
                    // Phare de Métacognition (Niv. 60) : Pose une bulle d'invulnérabilité autour des Souvenirs
                    // et Étincelles de Lucidité présents à l'écran, empêchant les Pensées Parasites de les écraser.
                    val isVisibleOnScreen = (tx - newCameraX) in -0.04f..(VIEWPORT_WIDTH + 0.04f) &&
                        (ty - newCameraY) in -0.04f..(VIEWPORT_HEIGHT + 0.04f)
                    val isProtectedByMetacognitionBubble = newMetacognitionBeaconTimer > 0f && isVisibleOnScreen

                    var hitByObstacle = false
                    for (j in 0 until obstaclesBuffer.size) {
                        val obs = obstaclesBuffer[j]
                        if (obs.paralyzedTimer > 0f) continue
                        val canBreakTarget = obs.type == ObstacleType.ASTEROID ||
                            obs.type == ObstacleType.LASER_DRONE ||
                            obs.type == ObstacleType.PULSAR_MINE ||
                            obs.type == ObstacleType.DOGMATIC_PRISM
                        if (canBreakTarget) {
                            val odx = shortestToroidalDelta(tx, obs.x, WORLD_WIDTH)
                            val ody = shortestToroidalDelta(ty, obs.y, WORLD_HEIGHT)
                            val oDist = hypot(odx, ody)
                            if (oDist <= (obs.radius + target.radius * 0.85f)) {
                                if (isProtectedByMetacognitionBubble) {
                                    // Repousse l'obstacle hors de la bulle d'invulnérabilité de la Pensée Pure
                                    val pushNorm = oDist.coerceAtLeast(0.001f)
                                    val bounceSpd = hypot(obs.vx, obs.vy).coerceAtLeast(0.14f)
                                    obstaclesBuffer[j] = obs.copy(
                                        x = wrapCoord(tx + (odx / pushNorm) * (obs.radius + target.radius + 0.02f), WORLD_WIDTH),
                                        y = wrapCoord(ty + (ody / pushNorm) * (obs.radius + target.radius + 0.02f), WORLD_HEIGHT),
                                        vx = (odx / pushNorm) * bounceSpd,
                                        vy = (ody / pushNorm) * bounceSpd
                                    )
                                } else {
                                    hitByObstacle = true
                                    break
                                }
                            }
                        }
                    }
                    val updatedTarget = target.copy(
                        x = tx,
                        y = ty,
                        phase = nextPhase,
                        isDestroyed = hitByObstacle
                    )
                    rescueTargetsBuffer[i] = updatedTarget
                    if (hitByObstacle) {
                        newlyDestroyedTargetsBuffer.add(updatedTarget)
                    }
                }
            }
        }

        var newTargetsSavedInLevel = state.targetsSavedInLevel
        var newTargetsDestroyedInLevel = state.targetsDestroyedInLevel
        var newPurificationEnergyPoints = (state.purificationEnergyPoints + bonusEnergyEarnedFromDefenses - narcissusEnergyDrainedThisFrame).coerceAtLeast(0)
        val newVirtueFireCooldown = (state.virtueFireCooldown - dt).coerceAtLeast(0f)
        var newNeuronsPurifiedInLevel = state.neuronsPurifiedInLevel
        var extraHeartEarned = false

        for (i in 0 until newlySavedTargetsBuffer.size) {
            val saved = newlySavedTargetsBuffer[i]
            newRunScore += 10
            newPurificationEnergyPoints += 10
            cumulativeTargetsSaved += 1
            newTargetsSavedInLevel += 1

            for (p in 0 until 14) {
                val ang = (p / 14f) * 6.28f
                spawnParticle(
                    x = saved.x,
                    y = saved.y,
                    vx = cos(ang) * 0.20f,
                    vy = sin(ang) * 0.20f,
                    color = saved.type.glowColor,
                    life = 0.9f
                )
            }

            if (cumulativeTargetsSaved > 0 && cumulativeTargetsSaved % 50 == 0) {
                earnedMaxShields = (earnedMaxShields + 1).coerceAtMost(7)
                newMaxShields = earnedMaxShields
                newShields = (newShields + 1).coerceAtMost(7)
                extraHeartEarned = true
                newBannerMsg = "💖 50 Pensées Pures / Purifications ! +1 Cœur supplémentaire ($newShields/$newMaxShields cœurs max 7) !"
                newBannerTimer = 3.8f
            } else if (newBannerTimer <= 1.0f) {
                newBannerMsg = "${saved.type.emoji} Pensée Pure « ${saved.conceptLabel} » (+10 pts) ! Cliquez sur votre sphère pour choisir un remède !"
                newBannerTimer = 2.6f
            }
        }

        for (i in 0 until newlyDestroyedTargetsBuffer.size) {
            val destroyed = newlyDestroyedTargetsBuffer[i]
            newTargetsDestroyedInLevel += 1
            for (p in 0 until 16) {
                val ang = (p / 16f) * 6.28f
                val speed = 0.14f + (p % 3) * 0.05f
                spawnParticle(
                    x = destroyed.x,
                    y = destroyed.y,
                    vx = cos(ang) * speed,
                    vy = sin(ang) * speed,
                    color = if (p % 2 == 0) destroyed.type.primaryColor else Color(0xFFEF4444),
                    life = 0.85f
                )
            }
            if (newBannerTimer <= 1.2f) {
                newBannerMsg = if (destroyed.type == RescueTargetType.FRAGILE_MEMORY) {
                    "💔 Bulle de Souvenir « ${destroyed.conceptLabel} » brisée par une Pensée Parasite..."
                } else {
                    "💥 ${destroyed.type.displayName} « ${destroyed.conceptLabel} » brisée par un obstacle mental !"
                }
                newBannerTimer = 2.2f
            }
        }

        // 5c. Update Infected Neurons & Virtue Word Projectiles in-place
        var nearestVisibleInfected: InfectedNeuron? = null
        var nearestVisibleInfectedDist = Float.MAX_VALUE

        for (i in 0 until neuronsBuffer.size) {
            val neuron = neuronsBuffer[i]
            val stabilityFactor = (1f - neuron.purificationProgress * 0.75f).coerceIn(0.2f, 1f)
            val phaseStep = when (neuron.corruptionType) {
                NeuronCorruptionType.AGITATION -> dt * (5.2f * stabilityFactor + 1.5f)
                NeuronCorruptionType.TRISTESSE -> dt * 1.4f
                NeuronCorruptionType.COLERE_PEUR -> dt * 3.2f
                NeuronCorruptionType.RANCOEUR -> dt * 2.2f
                NeuronCorruptionType.SURTENSION -> dt * (6.2f * stabilityFactor + 1.8f)
                NeuronCorruptionType.APATHIE -> dt * 0.85f
                NeuronCorruptionType.RIGIDITE_OBSESSION -> dt * 2.6f
                NeuronCorruptionType.BURNOUT_DISSOCIATION -> dt * 1.1f
            }
            val nextPhase = neuron.phase + phaseStep
            var nvx = neuron.vx
            var nvy = neuron.vy
            if (!neuron.isPurified &&
                (neuron.corruptionType == NeuronCorruptionType.AGITATION || neuron.corruptionType == NeuronCorruptionType.SURTENSION)
            ) {
                nvx += cos(nextPhase * 2.7f) * 0.0018f * stabilityFactor
                nvy += sin(nextPhase * 3.1f) * 0.0018f * stabilityFactor
                val spd = hypot(nvx, nvy)
                if (spd > 0.032f) {
                    nvx = (nvx / spd) * 0.032f
                    nvy = (nvy / spd) * 0.032f
                }
            }
            val moveScale = when {
                state.showSphereRemedyMenu -> 0f
                neuron.isPurified -> 0.25f
                else -> peaceFactor * stabilityFactor
            }
            val nx = wrapCoord(neuron.x + nvx * dt * moveScale, WORLD_WIDTH)
            val ny = wrapCoord(neuron.y + nvy * dt * moveScale, WORLD_HEIGHT)

            val updatedNeuron = neuron.copy(
                x = nx,
                y = ny,
                vx = nvx,
                vy = nvy,
                phase = nextPhase,
                hitFlashTimer = (neuron.hitFlashTimer - dt).coerceAtLeast(0f),
                purifiedGlowTimer = (neuron.purifiedGlowTimer - dt).coerceAtLeast(0f)
            )
            neuronsBuffer[i] = updatedNeuron

            if (!updatedNeuron.isPurified &&
                (nx - newCameraX) in 0.03f..(VIEWPORT_WIDTH - 0.03f) &&
                (ny - newCameraY) in 0.03f..(VIEWPORT_HEIGHT - 0.03f)
            ) {
                val d = hypot(nx - newPlayerX, ny - newPlayerY)
                if (d <= ActiveGameState.NEURON_PURIFICATION_PROXIMITY && d < nearestVisibleInfectedDist) {
                    nearestVisibleInfectedDist = d
                    nearestVisibleInfected = updatedNeuron
                }
            }
        }

        if (nearestVisibleInfected != null && !state.showSphereRemedyMenu) {
            if (newBannerTimer <= 0.3f) {
                newBannerMsg = if (newPurificationEnergyPoints >= 10) {
                    "🔮 Neurone « ${nearestVisibleInfected.negativeAffectLabel} » à proximité ! Cliquez sur votre sphère pour choisir un remède !"
                } else {
                    "✨ Récupérez d'abord une Pensée Pure (+10 pts) puis cliquez sur votre sphère pour purifier « ${nearestVisibleInfected.negativeAffectLabel} » !"
                }
                newBannerTimer = 2.0f
            }
        }

        // Update active PurificationFeedbackEffects (0%, 50%, 100% purifié floating callouts & shockwaves)
        for (fIdx in purificationFeedbacksBuffer.lastIndex downTo 0) {
            val fb = purificationFeedbacksBuffer[fIdx]
            val nextFbLife = fb.life - dt
            if (nextFbLife <= 0f) {
                purificationFeedbacksBuffer.removeAt(fIdx)
            } else {
                purificationFeedbacksBuffer[fIdx] = fb.copy(
                    y = wrapCoord(fb.y - dt * 0.036f, WORLD_HEIGHT),
                    life = nextFbLife
                )
            }
        }

        // Update Virtue Word Projectiles in staging buffer
        var anyNeuronPurifiedThisFrame = false
        for (i in projectilesBuffer.lastIndex downTo 0) {
            val proj = projectilesBuffer[i]
            val nextLife = proj.life - dt
            if (nextLife <= 0f) {
                projectilesBuffer.removeAt(i)
                continue
            }

            var targetNeuron: InfectedNeuron? = null
            if (proj.targetNeuronId >= 0) {
                for (nIdx in 0 until neuronsBuffer.size) {
                    val cand = neuronsBuffer[nIdx]
                    if (cand.id == proj.targetNeuronId && !cand.isPurified) {
                        targetNeuron = cand
                        break
                    }
                }
            }
            var targetObstacle: SpaceObstacle? = null
            if (targetNeuron == null && proj.targetObstacleId >= 0) {
                for (oIdx in 0 until obstaclesBuffer.size) {
                    val cand = obstaclesBuffer[oIdx]
                    if (cand.id == proj.targetObstacleId) {
                        targetObstacle = cand
                        break
                    }
                }
            }

            var pvx = proj.vx
            var pvy = proj.vy
            if (targetNeuron != null) {
                val dx = shortestToroidalDelta(proj.x, targetNeuron.x, WORLD_WIDTH)
                val dy = shortestToroidalDelta(proj.y, targetNeuron.y, WORLD_HEIGHT)
                val dist = hypot(dx, dy).coerceAtLeast(0.001f)
                val projSpeed = 1.15f
                pvx = (dx / dist) * projSpeed
                pvy = (dy / dist) * projSpeed
            } else if (targetObstacle != null) {
                val dx = shortestToroidalDelta(proj.x, targetObstacle.x, WORLD_WIDTH)
                val dy = shortestToroidalDelta(proj.y, targetObstacle.y, WORLD_HEIGHT)
                val dist = hypot(dx, dy).coerceAtLeast(0.001f)
                val projSpeed = 1.15f
                pvx = (dx / dist) * projSpeed
                pvy = (dy / dist) * projSpeed
            }
            var npx = wrapCoord(proj.x + pvx * dt, WORLD_WIDTH)
            var npy = wrapCoord(proj.y + pvy * dt, WORLD_HEIGHT)

            // 1) Check collision with Dogmatic Prism or Obscurantism Veil
            var hitPrismOrVeilIdx = -1
            for (oIdx in 0 until obstaclesBuffer.size) {
                val obs = obstaclesBuffer[oIdx]
                if ((obs.type == ObstacleType.DOGMATIC_PRISM || obs.type == ObstacleType.OBSCURANTISM_VEIL) &&
                    hypot(
                        shortestToroidalDelta(npx, obs.x, WORLD_WIDTH),
                        shortestToroidalDelta(npy, obs.y, WORLD_HEIGHT)
                    ) <= (obs.radius + 0.030f)
                ) {
                    hitPrismOrVeilIdx = oIdx
                    break
                }
            }

            if (hitPrismOrVeilIdx >= 0) {
                val hitObs = obstaclesBuffer[hitPrismOrVeilIdx]
                if (hitObs.type == ObstacleType.OBSCURANTISM_VEIL) {
                    obstaclesBuffer.removeAt(hitPrismOrVeilIdx)
                    obstacleDestroyedScore += 70
                    newRunScore += 70
                    newBannerMsg = "☀️ Voile d'Obscurantisme dissipé de façon permanente par « ${proj.wordLabel} » (+70 pts) ! Capteurs rétablis !"
                    newBannerTimer = 2.8f
                    for (p in 0 until 18) {
                        val ang = (p / 18f) * 6.28f
                        spawnParticle(
                            x = hitObs.x,
                            y = hitObs.y,
                            vx = cos(ang) * 0.25f,
                            vy = sin(ang) * 0.25f,
                            color = proj.glowColor,
                            life = 0.95f
                        )
                    }
                    projectilesBuffer.removeAt(i)
                    continue
                } else if (hitObs.type == ObstacleType.DOGMATIC_PRISM) {
                    if (proj.virtue == VirtueRemedyType.VERITE_DISCERNEMENT) {
                        AstroSoundManager.playPrismInteraction(shattered = true)
                        obstaclesBuffer.removeAt(hitPrismOrVeilIdx)
                        obstacleDestroyedScore += 90
                        newRunScore += 90
                        newBannerMsg = "🔍 Prisme Dogmatique (Endoctrinement) brisé par « ${proj.wordLabel} » (+90 pts) !"
                        newBannerTimer = 2.8f
                        for (p in 0 until 18) {
                            val ang = (p / 18f) * 6.28f
                            spawnParticle(
                                x = hitObs.x,
                                y = hitObs.y,
                                vx = cos(ang) * 0.26f,
                                vy = sin(ang) * 0.26f,
                                color = VioletPulsarGlow,
                                life = 0.95f
                            )
                        }
                        projectilesBuffer.removeAt(i)
                        continue
                    } else {
                        AstroSoundManager.playPrismInteraction(shattered = false)
                        val rdx = shortestToroidalDelta(hitObs.x, npx, WORLD_WIDTH)
                        val rdy = shortestToroidalDelta(hitObs.y, npy, WORLD_HEIGHT)
                        val rDist = hypot(rdx, rdy).coerceAtLeast(0.001f)
                        val speed = hypot(pvx, pvy).coerceAtLeast(1.0f)
                        pvx = (rdx / rDist) * speed
                        pvy = (rdy / rDist) * speed
                        npx = wrapCoord(hitObs.x + (rdx / rDist) * (hitObs.radius + 0.042f), WORLD_WIDTH)
                        npy = wrapCoord(hitObs.y + (rdy / rDist) * (hitObs.radius + 0.042f), WORLD_HEIGHT)
                        newBannerMsg = "💎 Le Prisme Dogmatique a dévié « ${proj.wordLabel} » ! Projeter « Vérité & Discernement » 🔍 ou une Onde Nova !"
                        newBannerTimer = 2.8f
                        projectilesBuffer[i] = proj.copy(
                            targetNeuronId = -1,
                            targetObstacleId = -1,
                            x = npx,
                            y = npy,
                            vx = pvx,
                            vy = pvy,
                            life = nextLife.coerceAtMost(0.85f)
                        )
                        continue
                    }
                }
            }

            // 2) Check collision with unpurified neuron (prioritizing targeted neuron so another connected neuron doesn't intercept a matching remedy)
            var hitNeuronIdx = -1
            if (proj.targetNeuronId >= 0) {
                for (nIdx in 0 until neuronsBuffer.size) {
                    val n = neuronsBuffer[nIdx]
                    if (n.id == proj.targetNeuronId && !n.isPurified && hypot(
                            shortestToroidalDelta(npx, n.x, WORLD_WIDTH),
                            shortestToroidalDelta(npy, n.y, WORLD_HEIGHT)
                        ) <= (n.radius + 0.036f)
                    ) {
                        hitNeuronIdx = nIdx
                        break
                    }
                }
            }
            if (hitNeuronIdx < 0) {
                val hasActiveTargetNeuron = proj.targetNeuronId >= 0 &&
                    neuronsBuffer.any { it.id == proj.targetNeuronId && !it.isPurified }
                for (nIdx in 0 until neuronsBuffer.size) {
                    val n = neuronsBuffer[nIdx]
                    val matchesVirtue = proj.virtue == n.corruptionType.remedyVirtue ||
                        proj.virtue == n.corruptionType.secondaryVirtue ||
                        proj.virtue == VirtueRemedyType.INFLUX_BIOELECTRIQUE ||
                        proj.virtue == VirtueRemedyType.RESONANCE_GAMMA
                    if (!n.isPurified &&
                        (!hasActiveTargetNeuron || matchesVirtue) &&
                        hypot(
                            shortestToroidalDelta(npx, n.x, WORLD_WIDTH),
                            shortestToroidalDelta(npy, n.y, WORLD_HEIGHT)
                        ) <= (n.radius + 0.036f)
                    ) {
                        hitNeuronIdx = nIdx
                        break
                    }
                }
            }

            if (hitNeuronIdx >= 0) {
                val hitNeuron = neuronsBuffer[hitNeuronIdx]
                val isPrimaryAffinity = proj.virtue == hitNeuron.corruptionType.remedyVirtue ||
                    proj.virtue == VirtueRemedyType.INFLUX_BIOELECTRIQUE
                val isSecondaryAffinity = proj.virtue == hitNeuron.corruptionType.secondaryVirtue ||
                    proj.virtue == VirtueRemedyType.RESONANCE_GAMMA

                if (isPrimaryAffinity || isSecondaryAffinity) {
                    val progressDelta = if (isPrimaryAffinity) 1.0f else 0.50f
                    val updatedProgress = (hitNeuron.purificationProgress + progressDelta).coerceAtMost(1.0f)
                    val nowPurified = updatedProgress >= 0.99f

                    neuronsBuffer[hitNeuronIdx] = hitNeuron.copy(
                        purificationProgress = updatedProgress,
                        isPurified = nowPurified,
                        purifiedGlowTimer = if (nowPurified) 4.0f else 0f,
                        hitFlashTimer = 0.45f,
                        lastVirtueWord = proj.wordLabel
                    )

                    val pCount = if (nowPurified) 24 else 14
                    for (p in 0 until pCount) {
                        val ang = (p.toFloat() / pCount) * 6.28f
                        val spd = if (nowPurified) 0.25f else 0.16f
                        spawnParticle(
                            x = hitNeuron.x,
                            y = hitNeuron.y,
                            vx = cos(ang) * spd,
                            vy = sin(ang) * spd,
                            color = if (p % 2 == 0) hitNeuron.corruptionType.purifiedAuraColor else SolarGoldGlow,
                            life = if (nowPurified) 1.05f else 0.75f
                        )
                    }

                    if (nowPurified) {
                        anyNeuronPurifiedThisFrame = true
                        newRunScore += 10
                        cumulativeNeuronsPurified += 1
                        newNeuronsPurifiedInLevel += 1
                        cumulativeTargetsSaved += 1

                        AstroSoundManager.playNeuronPurificationResult(100)
                        purificationFeedbacksBuffer.add(
                            PurificationFeedbackEffect(
                                id = nextPurificationFeedbackId++,
                                x = hitNeuron.x,
                                y = hitNeuron.y,
                                percentage = 100,
                                label = "✨ 100% PURIFIÉ !",
                                subLabel = "${proj.virtue.emoji} ${proj.virtue.displayName} • « ${hitNeuron.negativeAffectLabel} » (+10 pts)",
                                primaryColor = Color(0xFF10B981),
                                glowColor = SolarGoldGlow
                            )
                        )

                        if (cumulativeNeuronsPurified % 50 == 0 || cumulativeTargetsSaved % 50 == 0) {
                            earnedMaxShields = (earnedMaxShields + 1).coerceAtMost(7)
                            newMaxShields = earnedMaxShields
                            newShields = (newShields + 1).coerceAtMost(7)
                            extraHeartEarned = true
                            newBannerMsg = "💖 50 Neurones Purifiés ! +1 Cœur supplémentaire ($newShields/$newMaxShields cœurs, max 7) !"
                            newBannerTimer = 3.8f
                        } else {
                            newBannerMsg = "✨ 100% PURIFIÉ ! Neurone (${hitNeuron.negativeAffectLabel}) guéri par « ${proj.wordLabel} » (+10 pts) !"
                            newBannerTimer = 2.5f
                        }
                    } else {
                        AstroSoundManager.playNeuronPurificationResult(50)
                        purificationFeedbacksBuffer.add(
                            PurificationFeedbackEffect(
                                id = nextPurificationFeedbackId++,
                                x = hitNeuron.x,
                                y = hitNeuron.y,
                                percentage = 50,
                                label = "⚡ 50% PURIFIÉ !",
                                subLabel = "${proj.virtue.emoji} ${proj.virtue.displayName} • Résonance partielle sur « ${hitNeuron.negativeAffectLabel} »",
                                primaryColor = Color(0xFF0EA5E9),
                                glowColor = CyberCyanGlow
                            )
                        )
                        newBannerMsg = "⚡ 50% PURIFIÉ ! Résonance partielle sur « ${hitNeuron.negativeAffectLabel} » (idéal 100% : ${hitNeuron.corruptionType.remedyVirtue.displayName}) !"
                        newBannerTimer = 2.8f
                    }
                } else {
                    // Wrong remedy (neither primary nor secondary affinity): 0% purified!
                    AstroSoundManager.playNeuronPurificationResult(0)
                    neuronsBuffer[hitNeuronIdx] = hitNeuron.copy(
                        hitFlashTimer = 0.40f
                    )
                    purificationFeedbacksBuffer.add(
                        PurificationFeedbackEffect(
                            id = nextPurificationFeedbackId++,
                            x = hitNeuron.x,
                            y = hitNeuron.y,
                            percentage = 0,
                            label = "❌ 0% PURIFIÉ !",
                            subLabel = "${proj.virtue.emoji} ${proj.virtue.displayName} • Inadapté pour « ${hitNeuron.negativeAffectLabel} »",
                            primaryColor = Color(0xFFDC2626),
                            glowColor = Color(0xFFFCA5A5)
                        )
                    )
                    for (p in 0 until 12) {
                        val ang = (p.toFloat() / 12f) * 6.28f
                        spawnParticle(
                            x = hitNeuron.x,
                            y = hitNeuron.y,
                            vx = cos(ang) * 0.18f,
                            vy = sin(ang) * 0.18f,
                            color = Color(0xFFEF4444),
                            life = 0.65f
                        )
                    }
                    newBannerMsg = "❌ 0% PURIFIÉ ! « ${proj.virtue.displayName} » ne guérit pas « ${hitNeuron.negativeAffectLabel} » (idéal 100% : ${hitNeuron.corruptionType.remedyVirtue.emoji} ${hitNeuron.corruptionType.remedyVirtue.displayName}, 50% : ${hitNeuron.corruptionType.secondaryVirtue.emoji} ${hitNeuron.corruptionType.secondaryVirtue.displayName}) !"
                    newBannerTimer = 3.2f
                }
                projectilesBuffer.removeAt(i)
            } else {
                projectilesBuffer[i] = proj.copy(
                    x = npx,
                    y = npy,
                    vx = pvx,
                    vy = pvy,
                    life = nextLife
                )
            }
        }

        if (newlySavedTargetsBuffer.isNotEmpty() || anyNeuronPurifiedThisFrame || extraHeartEarned) {
            val savedSnapshot = cumulativeTargetsSaved
            val purifiedSnapshot = cumulativeNeuronsPurified
            val maxShieldsSnapshot = earnedMaxShields
            viewModelScope.launch {
                repository.updateCumulativeStats(
                    totalTargetsSaved = savedSnapshot,
                    totalNeuronsPurified = purifiedSnapshot,
                    maxShields = maxShieldsSnapshot
                )
            }
        }

        // Handle Power-Up Activation
        for (idx in 0 until newlyCollectedPowerUpsBuffer.size) {
            val pup = newlyCollectedPowerUpsBuffer[idx]
            when (pup.type) {
                PowerUpType.SHIELD -> {
                    newShields = (newShields + 1).coerceAtMost(newMaxShields)
                    newInvuln = 4.0f
                    newBannerMsg = "🛡️ Bouclier Stellaire (+Aura protectrice) !"
                    newBannerTimer = 3.0f
                }
                PowerUpType.SPEED_BOOST -> {
                    newSpeedBoostTimer = 8.0f
                    newBannerMsg = "⚡ Hyper-Propulsion activée (Vitesse x1.5) !"
                    newBannerTimer = 3.0f
                }
                PowerUpType.MAGNET -> {
                    newMagnetTimer = 10.0f
                    newBannerMsg = "🧲 Aimant Cosmique actif (Attire les méditations) !"
                    newBannerTimer = 3.0f
                }
                PowerUpType.ANTI_ASTEROID -> {
                    newAntiAsteroidTimer = 12.0f
                    newBannerMsg = "🌿 Dissipateur Parasite actif (12s) : Dissipez les Pensées Parasites / Toxines Cérébrales !"
                    newBannerTimer = 3.2f
                    var nearestIdx = -1
                    var nearestDist = Float.MAX_VALUE
                    for (j in 0 until obstaclesBuffer.size) {
                        val obs = obstaclesBuffer[j]
                        if (obs.type == ObstacleType.ASTEROID) {
                            val d = hypot(shortestToroidalDelta(obs.x, newPlayerX, WORLD_WIDTH), shortestToroidalDelta(obs.y, newPlayerY, WORLD_HEIGHT))
                            if (d < nearestDist) {
                                nearestDist = d
                                nearestIdx = j
                            }
                        }
                    }
                    if (nearestIdx >= 0) {
                        val removed = obstaclesBuffer.removeAt(nearestIdx)
                        newRunScore += 60
                        for (p in 0 until 14) {
                            val ang = (p / 14f) * 6.28f
                            spawnParticle(removed.x, removed.y, cos(ang) * 0.24f, sin(ang) * 0.24f, EmeraldNovaGlow, 0.9f)
                        }
                    }
                }
                PowerUpType.ANTI_DRONE -> {
                    newAntiDroneTimer = 12.0f
                    newBannerMsg = "💠 Sérénité Anti-Angoisse active (12s) : Apaisez les Foyers d'Angoisse / Pics de Stress !"
                    newBannerTimer = 3.2f
                    var nearestIdx = -1
                    var nearestDist = Float.MAX_VALUE
                    for (j in 0 until obstaclesBuffer.size) {
                        val obs = obstaclesBuffer[j]
                        if (obs.type == ObstacleType.LASER_DRONE) {
                            val d = hypot(shortestToroidalDelta(obs.x, newPlayerX, WORLD_WIDTH), shortestToroidalDelta(obs.y, newPlayerY, WORLD_HEIGHT))
                            if (d < nearestDist) {
                                nearestDist = d
                                nearestIdx = j
                            }
                        }
                    }
                    if (nearestIdx >= 0) {
                        val removed = obstaclesBuffer.removeAt(nearestIdx)
                        newRunScore += 80
                        for (p in 0 until 14) {
                            val ang = (p / 14f) * 6.28f
                            spawnParticle(removed.x, removed.y, cos(ang) * 0.24f, sin(ang) * 0.24f, CyberCyanGlow, 0.9f)
                        }
                    }
                }
                PowerUpType.ANTI_MINE -> {
                    newAntiMineTimer = 12.0f
                    newBannerMsg = "🌊 Calme Anti-Panique actif (12s) : Neutralisez les Crises de Panique / Décharges Émotives !"
                    newBannerTimer = 3.2f
                    var nearestIdx = -1
                    var nearestDist = Float.MAX_VALUE
                    for (j in 0 until obstaclesBuffer.size) {
                        val obs = obstaclesBuffer[j]
                        if (obs.type == ObstacleType.PULSAR_MINE) {
                            val d = hypot(shortestToroidalDelta(obs.x, newPlayerX, WORLD_WIDTH), shortestToroidalDelta(obs.y, newPlayerY, WORLD_HEIGHT))
                            if (d < nearestDist) {
                                nearestDist = d
                                nearestIdx = j
                            }
                        }
                    }
                    if (nearestIdx >= 0) {
                        val removed = obstaclesBuffer.removeAt(nearestIdx)
                        newRunScore += 100
                        for (p in 0 until 14) {
                            val ang = (p / 14f) * 6.28f
                            spawnParticle(removed.x, removed.y, cos(ang) * 0.24f, sin(ang) * 0.24f, CoralHyperGlow, 0.9f)
                        }
                    }
                }
                PowerUpType.EMP_BLAST -> {
                    val blastRadius = 1.35f
                    var destroyedCount = 0
                    for (j in obstaclesBuffer.lastIndex downTo 0) {
                        val obs = obstaclesBuffer[j]
                        val dx = shortestToroidalDelta(obs.x, newPlayerX, WORLD_WIDTH)
                        val dy = shortestToroidalDelta(obs.y, newPlayerY, WORLD_HEIGHT)
                        if (hypot(dx, dy) <= blastRadius) {
                            destroyedCount++
                            for (p in 0 until 14) {
                                val ang = (p / 14f) * 6.28f
                                spawnParticle(
                                    x = obs.x,
                                    y = obs.y,
                                    vx = cos(ang) * 0.26f,
                                    vy = sin(ang) * 0.26f,
                                    color = pup.type.glowColor,
                                    life = 0.9f
                                )
                            }
                            obstaclesBuffer.removeAt(j)
                        }
                    }
                    val bonusScore = (destroyedCount * 50).coerceAtLeast(75)
                    newRunScore += bonusScore
                    newBannerMsg = "💥 Onde Nova ! $destroyedCount obstacles mentaux dissipés (+$bonusScore pts) !"
                    newBannerTimer = 3.2f
                }
                PowerUpType.FREE_WILL_AEGIS -> {
                    newFreeWillAegisTimer = 6.0f
                    newConformismSlowTimer = 0f
                    newBannerMsg = "🔷 Égide du Libre Arbitre (6s) : Bonus immunisés contre la Nuée & rebond réfractaire (+5 énergie/choc) !"
                    newBannerTimer = 3.4f
                }
                PowerUpType.METACOGNITION_BEACON -> {
                    newMetacognitionBeaconTimer = 8.0f
                    newBannerMsg = "🏮 Phare de Métacognition (8s) : Mini-carte rétablie, cibles révélées & Pensées Pures sous bulle !"
                    newBannerTimer = 3.4f
                }
                PowerUpType.HUMILITY_ANCHOR -> {
                    newHumilityAnchorTimer = 6.0f
                    humilityConvertedMirrorIds.clear()
                    newBannerMsg = "⚓ Ancrage d'Humilité (6s) : Énergie verrouillée & inversion du Miroir de Narcisse (+1 🛡️) !"
                    newBannerTimer = 3.4f
                }
                PowerUpType.MYELIN_SHEATH -> {
                    newMyelinSheathTimer = 7.0f
                    newMyelinShockAbsorber = true
                    newBannerMsg = "🧬 Gaine de Myéline (7s) : Sillage isolant (-65% vitesse) & Amortisseur de choc actif !"
                    newBannerTimer = 3.4f
                }
                PowerUpType.CONTEMPLATIVE_PHASE_SHIFT -> {
                    newPhaseShiftTimer = 4.0f
                    newConformismSlowTimer = 0f
                    phaseShiftedObstacleIds.clear()
                    newBannerMsg = "👻 Déphasage Contemplatif (4s) : Intangibilité Thêta, apaisement des obstacles & Sanctuaires +25% !"
                    newBannerTimer = 3.4f
                }
                PowerUpType.PLENITUDE_ARK -> {
                    newPlenitudeArkTimer = 8.0f
                    newConformismSlowTimer = 0f
                    newBannerMsg = "🏛️ Arche de Plénitude (8s) : Sanctuaire Nomade (Dôme répulsif, collecte radiale & harmonisation x2) !"
                    newBannerTimer = 3.6f
                }
            }

            // Burst particles for power-up pickup
            for (i in 0 until 16) {
                val angle = (i / 16f) * 6.28f
                spawnParticle(
                    x = pup.x,
                    y = pup.y,
                    vx = cos(angle) * 0.20f,
                    vy = sin(angle) * 0.20f,
                    color = pup.type.glowColor,
                    life = 1f
                )
            }
        }

        // Handle Instant Relic Collection (Facettes du Fruit de l'Esprit)
        for (idx in 0 until newlyCollectedRelicsBuffer.size) {
            val collected = newlyCollectedRelicsBuffer[idx]
            newRunScore += collected.points
            val emoji = collected.fruitEmoji.ifEmpty { "✨" }
            newBannerMsg = "$emoji ${collected.letter} recueilli (+${collected.points} pts) → Inventaire !"
            newBannerTimer = 2.8f

            val burstColor = MeditationsMatrix.getFruitGlowColor(collected.fruitId)
            for (i in 0 until 18) {
                val angle = (i / 18f) * (Math.PI.toFloat() * 2f)
                val speed = 0.15f + (i % 3) * 0.08f
                spawnParticle(
                    x = collected.x,
                    y = collected.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = burstColor,
                    life = 1f
                )
            }

            viewModelScope.launch {
                repository.saveCollectedText(
                    levelNumber = state.level.levelNumber,
                    sectorName = state.level.name,
                    relic = collected,
                    sphereColorId = sphereColorOption.id
                )
            }
        }

        // Handle Brain Bonus Relic (🧠) Collection
        if (newlyCollectedBrainRelic != null) {
            val brain = newlyCollectedBrainRelic
            newRunScore += brain.points
            newBannerMsg = "🧠 Fragment Bonus #${brain.brainLevel} recueilli : ${brain.title} (+${brain.points} pts) → Inventaire !"
            newBannerTimer = 3.6f

            for (i in 0 until 22) {
                val angle = (i / 22f) * (Math.PI.toFloat() * 2f)
                val speed = 0.16f + (i % 3) * 0.08f
                val color = if (i % 2 == 0) VioletPulsarGlow else PlasmaRoseGlow
                spawnParticle(
                    x = brain.x,
                    y = brain.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    color = color,
                    life = 1.1f
                )
            }

            viewModelScope.launch {
                repository.saveCollectedText(
                    levelNumber = state.level.levelNumber,
                    sectorName = state.level.name,
                    relic = brain,
                    sphereColorId = sphereColorOption.id
                )
            }

            if (_isDirectTextEnabled.value) {
                _directCollectedText.value = brain
                if (nextStatus == GameStatus.PLAYING) {
                    nextStatus = GameStatus.PAUSED
                }
            }
        }

        // Direct Text Display if enabled in settings
        if (newlyCollectedRelicsBuffer.isNotEmpty() && _isDirectTextEnabled.value) {
            val lastCollected = newlyCollectedRelicsBuffer[newlyCollectedRelicsBuffer.lastIndex]
            _directCollectedText.value = lastCollected
            if (nextStatus == GameStatus.PLAYING) {
                nextStatus = GameStatus.PAUSED
            }
        }

        // Conditions pour passer au niveau suivant :
        val allThreeCollected = collectedRelicCount == relicsBuffer.size && relicsBuffer.isNotEmpty()
        val bonusTextCollected = updatedBrainRelic?.isCollected ?: true
        val totalNeuronsInLevel = neuronsBuffer.size
        val requiredPurifiedNeurons = if (totalNeuronsInLevel == 0) 0 else (totalNeuronsInLevel + 1) / 2
        var purifiedNeuronsCount = 0
        for (i in 0 until neuronsBuffer.size) {
            if (neuronsBuffer[i].isPurified) purifiedNeuronsCount++
        }
        newNeuronsPurifiedInLevel = purifiedNeuronsCount
        val halfNeuronsPurified = purifiedNeuronsCount >= requiredPurifiedNeurons
        val isLevelComplete = allThreeCollected && bonusTextCollected && halfNeuronsPurified

        // Safety check: if the player still needs to purify neurons to reach half, has < 10 energy points,
        // no virtue projectile is in flight, and no active Pensées Pures remain on the map, revive one in-place so the level is never soft-locked.
        if (!halfNeuronsPurified && newPurificationEnergyPoints < 10 && projectilesBuffer.isEmpty()) {
            var hasActiveTarget = false
            var reviveIdx = -1
            var fallbackSavedIdx = -1
            for (i in 0 until rescueTargetsBuffer.size) {
                val t = rescueTargetsBuffer[i]
                if (!t.isSaved && !t.isDestroyed) {
                    hasActiveTarget = true
                    break
                } else if (t.isDestroyed && reviveIdx < 0) {
                    reviveIdx = i
                } else if (t.isSaved && fallbackSavedIdx < 0) {
                    fallbackSavedIdx = i
                }
            }
            val targetIdxToRevive = if (reviveIdx >= 0) reviveIdx else fallbackSavedIdx
            if (!hasActiveTarget && targetIdxToRevive >= 0) {
                if (reviveIdx >= 0) {
                    newTargetsDestroyedInLevel = (newTargetsDestroyedInLevel - 1).coerceAtLeast(0)
                }
                val revived = rescueTargetsBuffer[targetIdxToRevive]
                rescueTargetsBuffer[targetIdxToRevive] = revived.copy(
                    x = wrapCoord(newPlayerX + 0.55f, WORLD_WIDTH),
                    y = wrapCoord(newPlayerY + 0.55f, WORLD_HEIGHT),
                    isDestroyed = false,
                    isSaved = false
                )
            }
        }

        // Inform the player if one of the conditions was just completed while others are still pending
        val justFinishedFragments = newlyCollectedRelicsBuffer.isNotEmpty() && allThreeCollected
        val justFinishedBonusText = newlyCollectedBrainRelic != null && bonusTextCollected
        val justReachedHalfNeurons = anyNeuronPurifiedThisFrame && purifiedNeuronsCount == requiredPurifiedNeurons
        if (!isLevelComplete && (justFinishedFragments || justFinishedBonusText || justReachedHalfNeurons)) {
            val missingParts = buildList {
                if (!allThreeCollected) {
                    add("Fragments ($collectedRelicCount/${relicsBuffer.size})")
                }
                if (!bonusTextCollected) {
                    add("Texte Bonus 🧠")
                }
                if (!halfNeuronsPurified) {
                    add("Neurones purifiés ($purifiedNeuronsCount/$requiredPurifiedNeurons)")
                }
            }
            if (missingParts.isNotEmpty()) {
                newBannerMsg = "🎯 Objectif en cours ! Il reste à valider : ${missingParts.joinToString(" • ")}"
                newBannerTimer = 3.6f
            }
        }

        var showEndPopup = state.showEndMissionTargetsPopup
        var endSavedCount = state.endMissionSavedCount
        var endUnsavedOnMapCount = state.endMissionUnsavedOnMapCount
        var endDestroyedCount = state.endMissionDestroyedCount
        var endSavedPoints = state.endMissionSavedPoints
        var endLostPoints = state.endMissionLostPoints
        var endPlayerTotalBefore = state.endMissionPlayerTotalBefore
        var endPlayerTotalAfter = state.endMissionPlayerTotalAfter

        var savedTargetsCount = 0
        var unsavedOnMapTargetsCount = 0
        var destroyedTargetsCount = 0
        for (i in 0 until rescueTargetsBuffer.size) {
            val t = rescueTargetsBuffer[i]
            when {
                t.isSaved -> savedTargetsCount++
                t.isDestroyed -> destroyedTargetsCount++
                else -> unsavedOnMapTargetsCount++
            }
        }
        val totalUnsavedTargetsCount = unsavedOnMapTargetsCount + destroyedTargetsCount
        val savedTargetsPoints = savedTargetsCount * 10
        val lostTargetsPoints = totalUnsavedTargetsCount * 20
        val netTargetScoreDelta = savedTargetsPoints - lostTargetsPoints
        var newlyCollectedPointsThisFrame = newlyCollectedBrainRelic?.points ?: 0
        for (i in 0 until newlyCollectedRelicsBuffer.size) {
            newlyCollectedPointsThisFrame += newlyCollectedRelicsBuffer[i].points
        }

        if (isLevelComplete) {
            val bonus = state.level.completionBonusPoints
            newRunScore = (newRunScore + bonus - lostTargetsPoints).coerceAtLeast(0)
            nextStatus = GameStatus.LEVEL_COMPLETE
            newBannerMsg = "Mission validée : Tous les fragments + Texte Bonus 🧠 + $purifiedNeuronsCount/$totalNeuronsInLevel neurones purifiés ! +$bonus pts"
            newBannerTimer = 4f

            val baseTotal = profile.value.totalScore + newlyCollectedPointsThisFrame + bonus
            val finalTotal = (baseTotal + netTargetScoreDelta).coerceAtLeast(0)
            showEndPopup = true
            endSavedCount = savedTargetsCount
            endUnsavedOnMapCount = unsavedOnMapTargetsCount
            endDestroyedCount = destroyedTargetsCount
            endSavedPoints = savedTargetsPoints
            endLostPoints = lostTargetsPoints
            endPlayerTotalBefore = baseTotal
            endPlayerTotalAfter = finalTotal

            val collectedA = relicsBuffer.any { (it.slotIndex == 0 || it.letter.equals("A", ignoreCase = true)) && it.isCollected }
            val collectedB = relicsBuffer.any { (it.slotIndex == 1 || it.letter.equals("B", ignoreCase = true)) && it.isCollected }
            val collectedC = relicsBuffer.any { (it.slotIndex == 2 || it.letter.equals("C", ignoreCase = true)) && it.isCollected }
            val savedSnapshot = cumulativeTargetsSaved
            val purifiedSnapshot = cumulativeNeuronsPurified
            val maxShieldsSnapshot = earnedMaxShields

            viewModelScope.launch {
                repository.recordLevelCompletion(
                    levelNumber = state.level.levelNumber,
                    levelRunScore = newRunScore,
                    completionBonus = bonus,
                    targetScoreDelta = netTargetScoreDelta,
                    collectedTextA = collectedA,
                    collectedTextB = collectedB,
                    collectedTextC = collectedC,
                    totalTargetsSaved = savedSnapshot,
                    totalNeuronsPurified = purifiedSnapshot,
                    maxShields = maxShieldsSnapshot
                )
            }
        } else if (hitDetected) {
            newShields -= 1
            newInvuln = 0.1f
            for (i in 0 until 16) {
                val angle = (i / 16f) * (Math.PI.toFloat() * 2f)
                spawnParticle(
                    x = newPlayerX,
                    y = newPlayerY,
                    vx = cos(angle) * 0.24f,
                    vy = sin(angle) * 0.24f,
                    color = Color(0xFFEF4444),
                    life = 1f
                )
            }
            if (newShields <= 0) {
                newRunScore = (newRunScore - lostTargetsPoints).coerceAtLeast(0)
                nextStatus = GameStatus.GAME_OVER

                val baseTotal = profile.value.totalScore + newlyCollectedPointsThisFrame
                val finalTotal = (baseTotal + netTargetScoreDelta).coerceAtLeast(0)
                showEndPopup = true
                endSavedCount = savedTargetsCount
                endUnsavedOnMapCount = unsavedOnMapTargetsCount
                endDestroyedCount = destroyedTargetsCount
                endSavedPoints = savedTargetsPoints
                endLostPoints = lostTargetsPoints
                endPlayerTotalBefore = baseTotal
                endPlayerTotalAfter = finalTotal

                val savedSnapshot = cumulativeTargetsSaved
                val purifiedSnapshot = cumulativeNeuronsPurified
                val maxShieldsSnapshot = earnedMaxShields

                viewModelScope.launch {
                    repository.recordGameOver(
                        levelNumber = state.level.levelNumber,
                        runScore = newRunScore,
                        targetScoreDelta = netTargetScoreDelta,
                        totalTargetsSaved = savedSnapshot,
                        totalNeuronsPurified = purifiedSnapshot,
                        maxShields = maxShieldsSnapshot
                    )
                }
            } else {
                newBannerMsg = "Impact d'obstacle ! Boucliers restants : $newShields"
                newBannerTimer = 2.0f
            }
        }

        // Sound effects for gameplay events & special sounds for elements in the player's zone
        currentFrameZoneKeysBuffer.clear()
        val playerZoneRadius = ActiveGameState.NEURON_PURIFICATION_PROXIMITY // 0.48f

        fun isVisibleOnViewport(wx: Float, wy: Float): Boolean {
            val rx = wx - newCameraX
            val ry = wy - newCameraY
            return rx in 0.02f..(VIEWPORT_WIDTH - 0.02f) && ry in 0.02f..(VIEWPORT_HEIGHT - 0.02f)
        }

        // 1) Neurones infectés dans la zone du joueur
        for (i in 0 until neuronsBuffer.size) {
            val n = neuronsBuffer[i]
            if (!n.isPurified && isVisibleOnViewport(n.x, n.y)) {
                val dist = hypot(
                    shortestToroidalDelta(n.x, newPlayerX, WORLD_WIDTH),
                    shortestToroidalDelta(n.y, newPlayerY, WORLD_HEIGHT)
                )
                if (dist <= playerZoneRadius) {
                    val key = "neuron_${n.id}"
                    currentFrameZoneKeysBuffer.add(key)
                    val isNewInZone = !activeZoneElementKeys.contains(key)
                    AstroSoundManager.playZoneInfectedNeuron(n.corruptionType, isImmediateEntry = isNewInZone)
                }
            }
        }

        // 2) Obstacles mentaux dans la zone du joueur
        for (i in 0 until obstaclesBuffer.size) {
            val obs = obstaclesBuffer[i]
            if (isVisibleOnViewport(obs.x, obs.y)) {
                val dx = shortestToroidalDelta(obs.x, newPlayerX, WORLD_WIDTH)
                val dy = shortestToroidalDelta(obs.y, newPlayerY, WORLD_HEIGHT)
                val dist = hypot(dx, dy)
                val inConformismRect = obs.type == ObstacleType.CONFORMISM_SWARM &&
                    kotlin.math.abs(dx) <= (obs.radius * 2.65f + 0.05f) &&
                    kotlin.math.abs(dy) <= (obs.radius * 1.95f + 0.05f)
                val zoneThreshold = when (obs.type) {
                    ObstacleType.NARCISSUS_MIRROR -> 0.56f
                    else -> 0.42f + obs.radius
                }
                if (dist <= zoneThreshold || inConformismRect) {
                    val key = "obs_${obs.id}"
                    currentFrameZoneKeysBuffer.add(key)
                    val isNewInZone = !activeZoneElementKeys.contains(key)
                    AstroSoundManager.playZoneObstacle(obs.type, isImmediateEntry = isNewInZone)
                }
            }
        }

        // 3) Sanctuaires de méditation & Texte Bonus 🧠 dans la zone du joueur
        for (i in 0 until relicsBuffer.size) {
            val relic = relicsBuffer[i]
            if (!relic.isCollected && isVisibleOnViewport(relic.x, relic.y)) {
                val dist = hypot(newPlayerX - relic.x, newPlayerY - relic.y)
                if (dist <= 0.44f) {
                    val key = "relic_${relic.slotIndex}"
                    currentFrameZoneKeysBuffer.add(key)
                    val isNewInZone = !activeZoneElementKeys.contains(key)
                    AstroSoundManager.playZoneSanctuaryRelic(isBrainBonus = false, isImmediateEntry = isNewInZone)
                }
            }
        }
        if (updatedBrainRelic != null && !updatedBrainRelic.isCollected && isVisibleOnViewport(updatedBrainRelic.x, updatedBrainRelic.y)) {
            val dist = hypot(newPlayerX - updatedBrainRelic.x, newPlayerY - updatedBrainRelic.y)
            if (dist <= 0.46f) {
                val key = "brain_relic"
                currentFrameZoneKeysBuffer.add(key)
                val isNewInZone = !activeZoneElementKeys.contains(key)
                AstroSoundManager.playZoneSanctuaryRelic(isBrainBonus = true, isImmediateEntry = isNewInZone)
            }
        }

        // 4) Pensées Pures / Cibles à sauver dans la zone du joueur
        for (i in 0 until rescueTargetsBuffer.size) {
            val target = rescueTargetsBuffer[i]
            if (!target.isSaved && !target.isDestroyed && !state.isTargetHiddenByObscurantism(target) && isVisibleOnViewport(target.x, target.y)) {
                val dist = hypot(
                    shortestToroidalDelta(target.x, newPlayerX, WORLD_WIDTH),
                    shortestToroidalDelta(target.y, newPlayerY, WORLD_HEIGHT)
                )
                if (dist <= 0.40f) {
                    val key = "target_${target.id}"
                    currentFrameZoneKeysBuffer.add(key)
                    val isNewInZone = !activeZoneElementKeys.contains(key)
                    AstroSoundManager.playZoneRescueTarget(target.type, isImmediateEntry = isNewInZone)
                }
            }
        }

        // 5) Bonus Cosmiques dans la zone du joueur
        for (i in 0 until powerUpsBuffer.size) {
            val pup = powerUpsBuffer[i]
            if (!pup.isCollected && isVisibleOnViewport(pup.x, pup.y)) {
                val dist = hypot(
                    shortestToroidalDelta(pup.x, newPlayerX, WORLD_WIDTH),
                    shortestToroidalDelta(pup.y, newPlayerY, WORLD_HEIGHT)
                )
                if (dist <= 0.40f) {
                    val key = "pup_${pup.id}"
                    currentFrameZoneKeysBuffer.add(key)
                    val isNewInZone = !activeZoneElementKeys.contains(key)
                    AstroSoundManager.playZonePowerUp(pup.type, isImmediateEntry = isNewInZone)
                }
            }
        }

        activeZoneElementKeys.clear()
        activeZoneElementKeys.addAll(currentFrameZoneKeysBuffer)

        if (conformismTriggeredThisFrame) {
            AstroSoundManager.playSpecialObstacleEffect(ObstacleType.CONFORMISM_SWARM)
        }
        if (narcissusEnergyDrainedThisFrame > 0) {
            AstroSoundManager.playSpecialObstacleEffect(ObstacleType.NARCISSUS_MIRROR)
        }

        var maxRelicHarmonizingProgress = 0f
        for (i in 0 until relicsBuffer.size) {
            val r = relicsBuffer[i]
            if (r.isHarmonizing && r.harmonizationProgress > maxRelicHarmonizingProgress) {
                maxRelicHarmonizingProgress = r.harmonizationProgress
            }
        }
        val activeHarmonizingProgress = maxOf(
            maxRelicHarmonizingProgress,
            if (updatedBrainRelic?.isHarmonizing == true) updatedBrainRelic.harmonizationProgress else 0f
        )
        if (activeHarmonizingProgress > 0f && newlyCollectedRelicsBuffer.isEmpty() && newlyCollectedBrainRelic == null) {
            AstroSoundManager.playHarmonizingPulse(activeHarmonizingProgress)
        }
        if (obstacleDestroyedScore > 0) {
            AstroSoundManager.playObstacleDestroyed()
        }
        if (extraHeartEarned) {
            AstroSoundManager.playExtraHeartEarned()
        } else if (anyNeuronPurifiedThisFrame || newlySavedTargetsBuffer.isNotEmpty()) {
            AstroSoundManager.playTargetSaved()
        } else if (newlyDestroyedTargetsBuffer.isNotEmpty()) {
            AstroSoundManager.playTargetDestroyed()
        }
        if (newlyCollectedPowerUpsBuffer.isNotEmpty()) {
            AstroSoundManager.playPowerUpCollected()
        }
        if (newlyCollectedBrainRelic != null && !isLevelComplete) {
            AstroSoundManager.playBrainBonusCollected()
        }
        if (newlyCollectedRelicsBuffer.isNotEmpty() && !isLevelComplete) {
            AstroSoundManager.playRelicCollected()
        }
        if (isLevelComplete) {
            AstroSoundManager.updateAmbientState(false)
            AstroSoundManager.playLevelComplete()
        } else if (hitDetected) {
            if (newShields <= 0) {
                AstroSoundManager.updateAmbientState(false)
                AstroSoundManager.playGameOver()
            } else {
                AstroSoundManager.playShieldHit()
            }
        }

        _gameState.value = state.copy(
            status = nextStatus,
            playerX = newPlayerX,
            playerY = newPlayerY,
            playerVx = vx,
            playerVy = vy,
            cameraX = newCameraX,
            cameraY = newCameraY,
            shields = newShields,
            maxShields = newMaxShields,
            invulnerableTimer = newInvuln,
            speedBoostTimer = newSpeedBoostTimer,
            magnetTimer = newMagnetTimer,
            antiAsteroidTimer = newAntiAsteroidTimer,
            antiDroneTimer = newAntiDroneTimer,
            antiMineTimer = newAntiMineTimer,
            conformismSlowTimer = newConformismSlowTimer,
            homeostasisFieldTimer = newHomeostasisFieldTimer,
            elanDashTimer = newElanDashTimer,
            freeWillAegisTimer = newFreeWillAegisTimer,
            metacognitionBeaconTimer = newMetacognitionBeaconTimer,
            humilityAnchorTimer = newHumilityAnchorTimer,
            myelinSheathTimer = newMyelinSheathTimer,
            myelinShockAbsorberAvailable = newMyelinShockAbsorber,
            phaseShiftTimer = newPhaseShiftTimer,
            plenitudeArkTimer = newPlenitudeArkTimer,
            borderWrapTimer = newBorderWrapTimer,
            borderWrapEdge = newBorderWrapEdge,
            peaceAuraTimer = newPeaceAuraTimer,
            pacificationLevel = newPacificationLevel,
            activeAnchoredThought = state.activeAnchoredThought,
            runScore = newRunScore,
            obstacles = obstaclesBuffer.toList(),
            relics = relicsBuffer.toList(),
            brainRelic = updatedBrainRelic,
            powerUps = powerUpsBuffer.toList(),
            rescueTargets = rescueTargetsBuffer.toList(),
            infectedNeurons = neuronsBuffer.toList(),
            virtueProjectiles = projectilesBuffer.toList(),
            bioelectricArcs = bioelectricArcsBuffer.toList(),
            gammaWaves = gammaWavesBuffer.toList(),
            purificationFeedbacks = purificationFeedbacksBuffer.toList(),
            purificationEnergyPoints = newPurificationEnergyPoints,
            virtueFireCooldown = newVirtueFireCooldown,
            neuronsPurifiedInLevel = newNeuronsPurifiedInLevel,
            totalNeuronsPurified = cumulativeNeuronsPurified,
            totalTargetsSaved = cumulativeTargetsSaved,
            targetsSavedInLevel = newTargetsSavedInLevel,
            targetsDestroyedInLevel = newTargetsDestroyedInLevel,
            showEndMissionTargetsPopup = showEndPopup,
            endMissionSavedCount = endSavedCount,
            endMissionUnsavedOnMapCount = endUnsavedOnMapCount,
            endMissionDestroyedCount = endDestroyedCount,
            endMissionSavedPoints = endSavedPoints,
            endMissionLostPoints = endLostPoints,
            endMissionPlayerTotalBefore = endPlayerTotalBefore,
            endMissionPlayerTotalAfter = endPlayerTotalAfter,
            trail = trailBuffer.toList(),
            particles = particlesBuffer.toList(),
            bannerMessage = newBannerMsg,
            bannerTimer = newBannerTimer,
            elapsedTime = state.elapsedTime + dt
        )
    }

    override fun onCleared() {
        super.onCleared()
        AstroSoundManager.stopAmbientSpaceMusic()
    }

    class Factory(private val repository: AstroRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AstroViewModel(repository) as T
        }
    }
}
