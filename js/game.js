// ============================================================================
// NOÔRBIS — MOTEUR DE JEU HTML5 CANVAS (MONDE TORIQUE 4x4, RADAR, JOYSTICK, ENTITÉS)
// ============================================================================

window.NoorbisGame = (function () {
  const C = window.NoorbisCatalog;
  const A = window.NoorbisAudio;

  const WORLD_SIZE = 4.0;
  const VIEW_SIZE = 1.0;

  // Starfield in 4.0 x 4.0 world
  const stars = [];
  for (let i = 0; i < 260; i++) {
    stars.push({
      x: Math.random() * WORLD_SIZE,
      y: Math.random() * WORLD_SIZE,
      size: 1 + Math.random() * 2.2,
      alpha: 0.25 + Math.random() * 0.75,
      phase: Math.random() * Math.PI * 2
    });
  }

  let canvas = null;
  let ctx = null;
  let animFrameId = null;
  let lastTime = 0;
  let onStateChange = null;

  // Keyboard state
  const keysDown = new Set();

  // Pointer / Joystick state
  let joystickTouchId = null;
  let joystickVec = { x: 0, y: 0 };
  let canvasPointerDown = false;
  let canvasPointerWorld = null;

  // Active Game State
  const state = {
    levelNumber: 1,
    sector: C.getSector(1),
    playerX: 2.0,
    playerY: 2.0,
    targetX: 2.0,
    targetY: 2.0,
    cameraX: 1.5,
    cameraY: 1.5,
    playerRadius: 0.038,
    shields: 3,
    maxShields: 3,
    invulnerableTimer: 0,
    speedBoostTimer: 0,
    magnetTimer: 0,
    antiAsteroidTimer: 0,
    antiDroneTimer: 0,
    antiMineTimer: 0,
    freeWillTimer: 0,
    metacognitionTimer: 0,
    humilityTimer: 0,
    myelinTimer: 0,
    phaseShiftTimer: 0,
    plenitudeTimer: 0,
    peaceAuraTimer: 0,
    isInMentalMire: false,
    isObscurantismActive: false,
    activeSanctuaryFruit: null,
    sanctuaryProgress: 0,
    obstacles: [],
    relics: [],
    powerUps: [],
    rescueTargets: [],
    infectedNeurons: [],
    virtueProjectiles: [],
    particles: [],
    floatingTexts: [],
    borderWarpEffects: [],
    PlayerTrail: [],
    targetsSavedCount: 0,
    targetsDestroyedCount: 0,
    totalTargetsCount: 0,
    neuronsPurifiedCount: 0,
    totalNeuronsCount: 0,
    purificationEnergy: 0,
    nearbyTargetNeuronId: null,
    isVirtueMenuOpen: false,
    collectedCount: 0,
    isBrainBonusCollected: false,
    score: 0,
    isPaused: false,
    isMissionBriefingOpen: false,
    newUnlockedElements: [],
    isEndTargetsScoreDialogOpen: false,
    isLevelComplete: false,
    isGameOver: false,
    lastCollectedMeditation: null,
    newlyCollectedThisLevel: [],
    powerUpSpawnTimer: 6.0
  };

  function getMilestoneUnlocks(level) {
    const list = [];
    if (level === 1) {
      list.push(
        { emoji: "🕊️", title: "Reliques des Fruits & Sanctuaires", category: "Relique & Harmonie", desc: "Approchez une relique pour activer son Sanctuaire d'Harmonisation et recueillir sa méditation (+100 pts).", color: "#00E5FF" },
        { emoji: "🧠", title: "Relique Cerveau (72 Fragments)", category: "Droit & Esprit Critique", desc: "Chaque secteur abrite un fragment de pensée libre, de droit ou de neurobiologie (+150 pts).", color: "#F43F5E" },
        { emoji: "✨", title: "Pensées Pures à Sauver", category: "Sauvetage (+10 / -20 pts)", desc: "Secourez les Étincelles de Lucidité avant que les obstacles ne les brisent (+10 pts & +10 Énergie).", color: "#38BDF8" },
        { emoji: "🦠", title: "Neurones Infectés & Remèdes", category: "Purification Synaptique", desc: "Cliquez sur votre sphère près d'un neurone corrompu (≥10 Énergie) pour projeter la vertu adaptée !", color: "#A855F7" }
      );
    }
    C.SimpleObstacleVariants.forEach(v => {
      if (v.minLevel === level && level > 1) {
        list.push({
          emoji: v.symbolEmoji,
          title: v.displayName,
          category: "Nouvel Obstacle Juridique / Cognitif",
          desc: v.definition,
          color: v.accentHex
        });
      }
    });
    Object.values(C.ObstacleTypes).forEach(o => {
      if (o.minLevel === level && level > 1) {
        list.push({
          emoji: "⚠️",
          title: o.displayName,
          category: "Archétype Complexe",
          desc: o.description,
          color: o.strokeHex
        });
      }
    });
    Object.values(C.NeuronCorruptionTypes).forEach(n => {
      if (n.minLevel === level && level > 1) {
        list.push({
          emoji: "🧬",
          title: n.displayName,
          category: "Nouvelle Corruption Neuronale",
          desc: `Symptôme : ${n.symptomLabel}. Remède idéal : ${C.VirtueRemedyTypes[n.idealRemedy].displayName}.`,
          color: n.corruptedColorHex
        });
      }
    });
    Object.values(C.VirtueRemedyTypes).forEach(r => {
      if (r.minLevel === level && level > 1) {
        list.push({
          emoji: r.emoji,
          title: r.displayName,
          category: "Nouveau Remède de la Sphère",
          desc: r.description,
          color: r.colorHex
        });
      }
    });
    Object.values(C.PowerUpTypes).forEach(p => {
      if (p.minLevel === level && level > 1) {
        list.push({
          emoji: p.iconLabel,
          title: p.displayName,
          category: "Nouveau Bonus Protecteur",
          desc: p.description,
          color: p.colorHex
        });
      }
    });
    return list;
  }

  function randomFarFromCenter(minDist = 0.35) {
    let x = 2.0, y = 2.0;
    for (let attempts = 0; attempts < 30; attempts++) {
      x = 0.22 + Math.random() * (WORLD_SIZE - 0.44);
      y = 0.22 + Math.random() * (WORLD_SIZE - 0.44);
      if (Math.hypot(x - 2.0, y - 2.0) >= minDist) break;
    }
    return { x, y };
  }

  function spawnObstacle(id, level, speedMult) {
    const pos = randomFarFromCenter(0.42);
    const angle = Math.random() * Math.PI * 2;
    const baseSpeed = (0.14 + Math.random() * 0.14) * speedMult;

    // Determine archetype
    const unlockedArchetypes = ["ASTEROID", "LASER_DRONE", "PULSAR_MINE"];
    if (level >= 20) unlockedArchetypes.push("CONFORMISM_SWARM");
    if (level >= 40) unlockedArchetypes.push("DOGMATIC_PRISM");
    if (level >= 60) unlockedArchetypes.push("OBSCURANTISM_VEIL");
    if (level >= 80) unlockedArchetypes.push("MENTAL_MIRE");
    if (level >= 100) unlockedArchetypes.push("NARCISSUS_MIRROR");

    let type = unlockedArchetypes[id % unlockedArchetypes.length];
    if (id < 3) type = ["ASTEROID", "LASER_DRONE", "PULSAR_MINE"][id % 3];

    let variant = null;
    if (type === "ASTEROID" || type === "LASER_DRONE" || type === "PULSAR_MINE") {
      const matchingVariants = C.SimpleObstacleVariants.filter(
        v => v.baseArchetype === type && v.minLevel <= level
      );
      if (matchingVariants.length > 0) {
        variant = matchingVariants[(id + level) % matchingVariants.length];
      }
    }

    const radius = type === "MENTAL_MIRE" ? 0.11 : type === "OBSCURANTISM_VEIL" ? 0.09 : 0.036 + (id % 3) * 0.008;

    return {
      id,
      x: pos.x,
      y: pos.y,
      vx: Math.cos(angle) * baseSpeed,
      vy: Math.sin(angle) * baseSpeed,
      radius,
      type,
      variant,
      rotation: Math.random() * 360,
      rotationSpeed: (Math.random() - 0.5) * 90,
      phase: Math.random() * Math.PI * 2
    };
  }

  function spawnPowerUp(id, level) {
    const unlocked = Object.values(C.PowerUpTypes).filter(p => p.minLevel <= level);
    const chosen = unlocked[Math.floor(Math.random() * unlocked.length)] || C.PowerUpTypes.SHIELD;
    const pos = randomFarFromCenter(0.28);
    return {
      id,
      x: pos.x,
      y: pos.y,
      radius: 0.034,
      type: chosen.id,
      phase: Math.random() * Math.PI * 2
    };
  }

  const MAX_PARTICLES = 90;
  const MAX_FLOATING_TEXTS = 18;
  let energyRegenAccumulator = 0;

  function initLevel(levelNumber, appState) {
    const clamped = Math.max(1, Math.min(135, levelNumber));
    const sector = C.getSector(clamped);

    const collectedIdsSet = new Set(
      appState && Array.isArray(appState.collectedItems)
        ? appState.collectedItems.map(i => i.id)
        : []
    );
    C.syncCollectedFragments(collectedIdsSet);
    const assignedIdsInLevel = new Set();

    state.levelNumber = clamped;
    state.sector = sector;
    state.playerX = 2.0;
    state.playerY = 2.0;
    state.targetX = 2.0;
    state.targetY = 2.0;
    state.cameraX = 1.5;
    state.cameraY = 1.5;
    state.maxShields = appState ? appState.maxShields : 3;
    state.shields = state.maxShields;
    state.invulnerableTimer = 0;
    state.speedBoostTimer = 0;
    state.magnetTimer = 0;
    state.antiAsteroidTimer = 0;
    state.antiDroneTimer = 0;
    state.antiMineTimer = 0;
    state.freeWillTimer = 0;
    state.metacognitionTimer = 0;
    state.humilityTimer = 0;
    state.myelinTimer = 0;
    state.phaseShiftTimer = 0;
    state.plenitudeTimer = 0;
    state.peaceAuraTimer = 0;
    state.isInMentalMire = false;
    state.activeSanctuaryFruit = null;
    state.sanctuaryProgress = 0;
    state.virtueProjectiles.length = 0;
    state.particles.length = 0;
    state.floatingTexts.length = 0;
    state.borderWarpEffects.length = 0;
    state.PlayerTrail.length = 0;
    state.collectedCount = 0;
    state.totalFruitRelicsCount = 3;
    state.isBrainBonusCollected = false;
    state.score = 0;
    state.isPaused = false;
    state.isVirtueMenuOpen = false;
    state.isEndTargetsScoreDialogOpen = false;
    state.isLevelComplete = false;
    state.isGameOver = false;
    state.lastCollectedMeditation = null;
    state.newlyCollectedThisLevel = [];
    energyRegenAccumulator = 0;

    // Relics: 3 Fruit relics (avec pré-attribution sans doublon) + 1 Brain Bonus relic
    state.relics = [];
    const quadrantOffsets = [
      { x: 0.85, y: 0.85 },
      { x: 3.15, y: 0.85 },
      { x: 0.85, y: 3.15 },
      { x: 3.15, y: 3.15 }
    ];
    for (let i = 0; i < 3; i++) {
      const q = quadrantOffsets[i];
      const fruitName = sector.fruits[i % sector.fruits.length];
      const preAssignedMed = C.getNextMeditationForFruit(
        fruitName,
        collectedIdsSet,
        clamped,
        assignedIdsInLevel
      );
      if (preAssignedMed && typeof preAssignedMed.id === "number") {
        assignedIdsInLevel.add(preAssignedMed.id);
      }
      state.relics.push({
        id: i,
        x: Math.max(0.25, Math.min(WORLD_SIZE - 0.25, q.x + (Math.random() - 0.5) * 0.7)),
        y: Math.max(0.25, Math.min(WORLD_SIZE - 0.25, q.y + (Math.random() - 0.5) * 0.7)),
        radius: 0.044,
        sanctuaryRadius: 0.20,
        harmonizationProgress: 0,
        fruitName: preAssignedMed ? preAssignedMed.fruit : fruitName,
        assignedMeditation: preAssignedMed || null,
        isCollected: false,
        isBrainBonus: false
      });
    }
    state.totalFruitRelicsCount = state.relics.length;

    const qb = quadrantOffsets[3];
    const uncollectedBrainFrag = C.getUncollectedBrainBonusForLevel(
      clamped,
      collectedIdsSet,
      assignedIdsInLevel
    );
    if (uncollectedBrainFrag) {
      assignedIdsInLevel.add(uncollectedBrainFrag.id);
      state.relics.push({
        id: 99,
        x: Math.max(0.25, Math.min(WORLD_SIZE - 0.25, qb.x + (Math.random() - 0.5) * 0.7)),
        y: Math.max(0.25, Math.min(WORLD_SIZE - 0.25, qb.y + (Math.random() - 0.5) * 0.7)),
        radius: 0.046,
        sanctuaryRadius: 0.20,
        harmonizationProgress: 0,
        fruitName: `🧠 ${uncollectedBrainFrag.title}`,
        assignedMeditation: C.formatBrainMeditation(uncollectedBrainFrag),
        isCollected: false,
        isBrainBonus: true
      });
    } else {
      // Les 72 fragments Cerveau ont tous été collectés : aucun doublon proposé
      state.isBrainBonusCollected = true;
    }

    // Rescue Targets: 3 base + 1 every 2 levels
    const targetCount = 3 + Math.floor((clamped - 1) / 2);
    const targetTypes = Object.keys(C.RescueTargetTypes);
    state.rescueTargets = [];
    for (let i = 0; i < targetCount; i++) {
      const pos = randomFarFromCenter(0.30);
      const angle = Math.random() * Math.PI * 2;
      const tType = targetTypes[i % targetTypes.length];
      state.rescueTargets.push({
        id: i,
        x: pos.x,
        y: pos.y,
        vx: Math.cos(angle) * 0.055,
        vy: Math.sin(angle) * 0.055,
        radius: 0.030,
        type: tType,
        isSaved: false,
        isDestroyed: false,
        phase: Math.random() * Math.PI * 2
      });
    }
    state.targetsSavedCount = 0;
    state.targetsDestroyedCount = 0;
    state.totalTargetsCount = targetCount;

    // Infected Neurons: 2 base + 1 every 10 levels
    const neuronCount = 2 + Math.floor((clamped - 1) / 10);
    const unlockedCorruptions = Object.values(C.NeuronCorruptionTypes).filter(c => c.minLevel <= clamped);
    state.infectedNeurons = [];
    for (let i = 0; i < neuronCount; i++) {
      const pos = randomFarFromCenter(0.45);
      const corr = unlockedCorruptions[i % unlockedCorruptions.length];
      state.infectedNeurons.push({
        id: i + 1,
        x: pos.x,
        y: pos.y,
        radius: 0.052,
        corruptionType: corr.id,
        purificationProgress: 0,
        isPurified: false,
        phase: Math.random() * Math.PI * 2
      });
    }
    state.neuronsPurifiedCount = 0;
    state.totalNeuronsCount = neuronCount;
    state.purificationEnergy = 10; // Start with 10 energy so player can test remedies early
    state.nearbyTargetNeuronId = null;

    // Obstacles
    state.obstacles = [];
    for (let i = 0; i < sector.obstacleCount; i++) {
      state.obstacles.push(spawnObstacle(i, clamped, sector.obstacleSpeed));
    }

    // Initial Power-Ups
    state.powerUps = [];
    for (let i = 0; i < 4; i++) {
      state.powerUps.push(spawnPowerUp(i, clamped));
    }

    // Check milestone unlocks
    const unlocks = getMilestoneUnlocks(clamped);
    state.newUnlockedElements = unlocks;
    state.isMissionBriefingOpen = unlocks.length > 0;

    notifyUI();
  }

  function spawnBurst(x, y, colorHex, count = 14) {
    const allowed = Math.min(count, Math.max(4, MAX_PARTICLES - state.particles.length));
    for (let i = 0; i < allowed; i++) {
      const angle = (Math.PI * 2 * i) / allowed + (Math.random() - 0.5) * 0.3;
      const speed = 0.08 + Math.random() * 0.26;
      state.particles.push({
        x,
        y,
        vx: Math.cos(angle) * speed,
        vy: Math.sin(angle) * speed,
        size: 2.5 + Math.random() * 3.5,
        alpha: 1.0,
        color: colorHex
      });
    }
  }

  function addFloatingText(x, y, text, colorHex) {
    if (state.floatingTexts.length >= MAX_FLOATING_TEXTS) {
      state.floatingTexts.shift();
    }
    state.floatingTexts.push({
      x,
      y,
      text,
      color: colorHex,
      alpha: 1.0,
      vy: -0.12
    });
  }

  function checkLevelCompletion(appCallbacks) {
    if (state.isLevelComplete || state.isEndTargetsScoreDialogOpen || state.isGameOver) {
      return false;
    }

    // Condition de victoire : toutes les reliques de Fruits collectées, tous les neurones purifiés, et toutes les pensées pures résolues
    let remainingFruitRelics = 0;
    for (let i = 0; i < state.relics.length; i++) {
      if (!state.relics[i].isBrainBonus && !state.relics[i].isCollected) {
        remainingFruitRelics++;
      }
    }

    const allFruitRelicsDone =
      remainingFruitRelics === 0 || state.collectedCount >= (state.totalFruitRelicsCount || 3);
    const allTargetsResolved =
      state.rescueTargets.length === 0 ||
      (state.targetsSavedCount + state.targetsDestroyedCount) >= state.totalTargetsCount;
    const allNeuronsPurified =
      state.infectedNeurons.length === 0 ||
      state.neuronsPurifiedCount >= state.totalNeuronsCount;

    if (allFruitRelicsDone && allTargetsResolved && allNeuronsPurified) {
      const directEnabled = appCallbacks && appCallbacks.getSettings
        ? Boolean(appCallbacks.getSettings().directTextEnabled)
        : false;
      const readerModal = document.getElementById("modal-meditation-reader");
      const isReaderModalOpen = Boolean(readerModal && readerModal.classList.contains("open"));

      if (state.lastCollectedMeditation && directEnabled && isReaderModalOpen) {
        // Attend que le joueur ferme la fenêtre de méditation
        return false;
      }

      state.lastCollectedMeditation = null;
      state.isVirtueMenuOpen = false;
      state.isEndTargetsScoreDialogOpen = true;
      notifyUI();
      if (appCallbacks && appCallbacks.onTriggerEndMissionTally) {
        appCallbacks.onTriggerEndMissionTally();
      }
      return true;
    }
    return false;
  }

  function fireVirtueRemedy(remedyId) {
    const remedy = C.VirtueRemedyTypes[remedyId];
    if (!remedy || state.purificationEnergy < 10) return false;

    // Find nearest unpurified neuron or targetable complex obstacle
    let bestTarget = null;
    let bestDist = 0.75;

    for (let i = 0; i < state.infectedNeurons.length; i++) {
      const n = state.infectedNeurons[i];
      if (!n.isPurified) {
        const d = Math.hypot(n.x - state.playerX, n.y - state.playerY);
        if (d < bestDist) {
          bestDist = d;
          bestTarget = { kind: "NEURON", id: n.id, x: n.x, y: n.y };
        }
      }
    }

    if (!bestTarget) {
      for (let i = 0; i < state.obstacles.length; i++) {
        const o = state.obstacles[i];
        if (o.type === "DOGMATIC_PRISM" || o.type === "OBSCURANTISM_VEIL" || o.type === "NARCISSUS_MIRROR" || o.type === "MENTAL_MIRE") {
          const d = Math.hypot(o.x - state.playerX, o.y - state.playerY);
          if (d < bestDist) {
            bestDist = d;
            bestTarget = { kind: "OBSTACLE", id: o.id, x: o.x, y: o.y };
          }
        }
      }
    }

    // Si un neurone infecté existe encore sur la carte mais est un peu plus loin, cibler le plus proche pour éviter tout blocage
    if (!bestTarget && state.infectedNeurons.length > 0) {
      let fallbackDist = Infinity;
      for (let i = 0; i < state.infectedNeurons.length; i++) {
        const n = state.infectedNeurons[i];
        if (!n.isPurified) {
          const d = Math.hypot(n.x - state.playerX, n.y - state.playerY);
          if (d < fallbackDist) {
            fallbackDist = d;
            bestTarget = { kind: "NEURON", id: n.id, x: n.x, y: n.y };
          }
        }
      }
    }

    if (!bestTarget) return false;

    state.purificationEnergy = Math.max(0, state.purificationEnergy - 10);
    state.isVirtueMenuOpen = false;
    A.playVirtueProjectile();

    state.virtueProjectiles.push({
      id: Date.now() + Math.random(),
      x: state.playerX,
      y: state.playerY,
      targetKind: bestTarget.kind,
      targetId: bestTarget.id,
      remedyId: remedy.id,
      word: remedy.word,
      colorHex: remedy.colorHex,
      speed: 1.15
    });

    notifyUI();
    return true;
  }

  function purifyNeuronEntity(neuronIndex, points, labelText, colorHex, appCallbacks) {
    const n = state.infectedNeurons[neuronIndex];
    if (!n) return;
    n.purificationProgress = 1.0;
    n.isPurified = true;
    state.neuronsPurifiedCount = Math.min(state.totalNeuronsCount, state.neuronsPurifiedCount + 1);
    state.score += points;
    spawnBurst(n.x, n.y, colorHex, 20);
    addFloatingText(n.x, n.y, labelText, colorHex);
    // Suppression physique immédiate de l'entité purifiée pour éviter toute fuite mémoire
    state.infectedNeurons.splice(neuronIndex, 1);
    if (appCallbacks && appCallbacks.onPositiveThoughtEarned) {
      appCallbacks.onPositiveThoughtEarned(1);
    }
  }

  function update(dt, appCallbacks) {
    const launchModal = document.getElementById("modal-launch-start");
    if (launchModal && launchModal.classList.contains("open")) {
      return;
    }

    const readerModal = document.getElementById("modal-meditation-reader");
    const isReaderModalOpen = Boolean(readerModal && readerModal.classList.contains("open"));

    if (!isReaderModalOpen && state.lastCollectedMeditation) {
      state.lastCollectedMeditation = null;
    }

    if (
      state.isPaused ||
      state.isMissionBriefingOpen ||
      state.isVirtueMenuOpen ||
      state.isEndTargetsScoreDialogOpen ||
      state.isLevelComplete ||
      state.isGameOver ||
      (state.lastCollectedMeditation && isReaderModalOpen && appCallbacks.getSettings().directTextEnabled)
    ) {
      return;
    }

    // Vérifier immédiatement si toutes les conditions de fin de mission sont déjà remplies
    if (checkLevelCompletion(appCallbacks)) {
      return;
    }

    // Decrement timers
    const dec = (t) => Math.max(0, t - dt);
    state.invulnerableTimer = dec(state.invulnerableTimer);
    state.speedBoostTimer = dec(state.speedBoostTimer);
    state.magnetTimer = dec(state.magnetTimer);
    state.antiAsteroidTimer = dec(state.antiAsteroidTimer);
    state.antiDroneTimer = dec(state.antiDroneTimer);
    state.antiMineTimer = dec(state.antiMineTimer);
    state.freeWillTimer = dec(state.freeWillTimer);
    state.metacognitionTimer = dec(state.metacognitionTimer);
    state.humilityTimer = dec(state.humilityTimer);
    state.myelinTimer = dec(state.myelinTimer);
    state.phaseShiftTimer = dec(state.phaseShiftTimer);
    state.plenitudeTimer = dec(state.plenitudeTimer);
    state.peaceAuraTimer = dec(state.peaceAuraTimer);

    // Régénération automatique d'énergie si des neurones restent à purifier et que l'énergie est < 10 (anti-blocage)
    if (state.infectedNeurons.length > 0 && state.purificationEnergy < 10) {
      energyRegenAccumulator += dt * 2.5;
      if (energyRegenAccumulator >= 1) {
        const gain = Math.floor(energyRegenAccumulator);
        energyRegenAccumulator -= gain;
        state.purificationEnergy = Math.min(10, state.purificationEnergy + gain);
        notifyUI();
      }
    } else {
      energyRegenAccumulator = 0;
    }

    // Periodic Power-Up spawn
    state.powerUpSpawnTimer -= dt;
    if (state.powerUpSpawnTimer <= 0) {
      state.powerUpSpawnTimer = 7.5;
      if (state.powerUps.length < 6) {
        state.powerUps.push(spawnPowerUp(Date.now(), state.levelNumber));
      }
    }

    // Compute input vector from Keyboard + Joystick + Direct Canvas Drag
    let inputX = joystickVec.x;
    let inputY = joystickVec.y;

    if (keysDown.has("ArrowLeft") || keysDown.has("KeyA") || keysDown.has("KeyQ")) inputX -= 1;
    if (keysDown.has("ArrowRight") || keysDown.has("KeyD")) inputX += 1;
    if (keysDown.has("ArrowUp") || keysDown.has("KeyW") || keysDown.has("KeyZ")) inputY -= 1;
    if (keysDown.has("ArrowDown") || keysDown.has("KeyS")) inputY += 1;

    const inputMag = Math.hypot(inputX, inputY);
    if (inputMag > 1) {
      inputX /= inputMag;
      inputY /= inputMag;
    }

    // Check Mental Mire slowdown & Obscurantism Veil radar jamming
    let inMire = false;
    let hasVeil = false;
    for (let i = 0; i < state.obstacles.length; i++) {
      const o = state.obstacles[i];
      if (o.type === "MENTAL_MIRE") {
        if (Math.hypot(o.x - state.playerX, o.y - state.playerY) < o.radius + state.playerRadius) {
          inMire = true;
        }
      } else if (o.type === "OBSCURANTISM_VEIL") {
        hasVeil = true;
      }
    }
    state.isInMentalMire = inMire && state.humilityTimer <= 0 && state.myelinTimer <= 0 && state.phaseShiftTimer <= 0;
    state.isObscurantismActive = hasVeil && state.metacognitionTimer <= 0 && state.plenitudeTimer <= 0;

    let moveSpeed = 0.62;
    if (state.speedBoostTimer > 0) moveSpeed *= 1.55;
    if (state.myelinTimer > 0) moveSpeed *= 1.75;
    if (state.isInMentalMire) moveSpeed *= 0.42;

    if (Math.abs(inputX) > 0.01 || Math.abs(inputY) > 0.01) {
      state.playerX += inputX * moveSpeed * dt;
      state.playerY += inputY * moveSpeed * dt;
      state.targetX = state.playerX;
      state.targetY = state.playerY;
    } else if (canvasPointerDown && canvasPointerWorld) {
      const dx = canvasPointerWorld.x - state.playerX;
      const dy = canvasPointerWorld.y - state.playerY;
      const dist = Math.hypot(dx, dy);
      if (dist > 0.01) {
        const step = Math.min(dist, moveSpeed * dt);
        state.playerX += (dx / dist) * step;
        state.playerY += (dy / dist) * step;
      }
    }

    // Toroidal Wrap on Borders (0..4.0)
    const margin = 0.025;
    let warpedSide = null;
    if (state.playerX < margin) {
      state.playerX = WORLD_SIZE - margin - 0.01;
      warpedSide = "LEFT";
    } else if (state.playerX > WORLD_SIZE - margin) {
      state.playerX = margin + 0.01;
      warpedSide = "RIGHT";
    }
    if (state.playerY < margin) {
      state.playerY = WORLD_SIZE - margin - 0.01;
      warpedSide = "TOP";
    } else if (state.playerY > WORLD_SIZE - margin) {
      state.playerY = margin + 0.01;
      warpedSide = "BOTTOM";
    }

    if (warpedSide) {
      state.targetX = state.playerX;
      state.targetY = state.playerY;
      state.cameraX = Math.max(0, Math.min(WORLD_SIZE - VIEW_SIZE, state.playerX - VIEW_SIZE / 2));
      state.cameraY = Math.max(0, Math.min(WORLD_SIZE - VIEW_SIZE, state.playerY - VIEW_SIZE / 2));
      if (state.borderWarpEffects.length < 6) {
        state.borderWarpEffects.push({ side: warpedSide, pos: 0.5, progress: 0 });
      }
      spawnBurst(state.playerX, state.playerY, "#00E5FF", 16);
      A.playBorderWarp();
    } else {
      // Smooth camera tracking
      const desiredCamX = Math.max(0, Math.min(WORLD_SIZE - VIEW_SIZE, state.playerX - VIEW_SIZE / 2));
      const desiredCamY = Math.max(0, Math.min(WORLD_SIZE - VIEW_SIZE, state.playerY - VIEW_SIZE / 2));
      state.cameraX += (desiredCamX - state.cameraX) * Math.min(1, dt * 10);
      state.cameraY += (desiredCamY - state.cameraY) * Math.min(1, dt * 10);
    }

    // Player trail
    state.PlayerTrail.push({ x: state.playerX, y: state.playerY, alpha: 0.65 });
    if (state.PlayerTrail.length > 16) state.PlayerTrail.shift();
    for (let i = 0; i < state.PlayerTrail.length; i++) {
      state.PlayerTrail[i].alpha *= 0.91;
    }

    // Border warp effects update in-place
    for (let i = state.borderWarpEffects.length - 1; i >= 0; i--) {
      state.borderWarpEffects[i].progress += dt * 1.8;
      if (state.borderWarpEffects[i].progress >= 1.0) {
        state.borderWarpEffects.splice(i, 1);
      }
    }

    // Relics & Sanctuaries of Harmonization (suppression physique à la collecte + anti-doublon)
    let currentSanctuary = null;
    let currentSanctuaryProg = 0;
    const effectiveMagnet = state.magnetTimer > 0 || state.plenitudeTimer > 0;

    for (let i = state.relics.length - 1; i >= 0; i--) {
      const r = state.relics[i];
      if (r.isCollected) {
        state.relics.splice(i, 1);
        continue;
      }
      const dx = state.playerX - r.x;
      const dy = state.playerY - r.y;
      const dist = Math.hypot(dx, dy);

      if (effectiveMagnet && dist < 0.42 && dist > 0.01) {
        r.x += (dx / dist) * 0.25 * dt;
        r.y += (dy / dist) * 0.25 * dt;
      }

      if (dist <= r.sanctuaryRadius) {
        r.harmonizationProgress = Math.min(1.0, r.harmonizationProgress + dt * 0.95);
        currentSanctuary = r.fruitName;
        currentSanctuaryProg = r.harmonizationProgress;
      } else {
        r.harmonizationProgress = Math.max(0, r.harmonizationProgress - dt * 0.35);
      }

      if (dist <= state.playerRadius + r.radius || r.harmonizationProgress >= 1.0) {
        r.isCollected = true;
        // Retirer immédiatement l'entité relique de la liste active
        state.relics.splice(i, 1);

        A.playRelicCollected();
        spawnBurst(r.x, r.y, r.isBrainBonus ? "#F43F5E" : "#F59E0B", 22);

        const collectedSet = appCallbacks ? appCallbacks.getCollectedIdsSet() : new Set();
        const alreadyInLevelIds = new Set(state.newlyCollectedThisLevel.map(m => m.id));

        if (r.isBrainBonus) {
          state.isBrainBonusCollected = true;
          state.score += 150;
          state.purificationEnergy += 20;

          let med = r.assignedMeditation;
          if (!med || collectedSet.has(med.id) || alreadyInLevelIds.has(med.id)) {
            med = C.getBrainBonusMeditationText(state.levelNumber, collectedSet, alreadyInLevelIds);
          }
          if (med) {
            C.markFragmentCollected(med.id);
            state.lastCollectedMeditation = med;
            state.newlyCollectedThisLevel.push(med);
            if (appCallbacks && appCallbacks.onCollectMeditation) {
              appCallbacks.onCollectMeditation(med);
            }
          }
          addFloatingText(r.x, r.y, "🧠 +150 pts !", "#F43F5E");
        } else {
          state.collectedCount += 1;
          state.score += 100;
          state.purificationEnergy += 15;

          let med = r.assignedMeditation;
          if (!med || collectedSet.has(med.id) || alreadyInLevelIds.has(med.id)) {
            med = C.getNextMeditationForFruit(
              r.fruitName,
              collectedSet,
              state.levelNumber,
              alreadyInLevelIds
            );
          }
          if (med) {
            C.markFragmentCollected(med.id);
            state.lastCollectedMeditation = med;
            state.newlyCollectedThisLevel.push(med);
            if (appCallbacks && appCallbacks.onCollectMeditation) {
              appCallbacks.onCollectMeditation(med);
            }
          }
          addFloatingText(r.x, r.y, "+100 pts", "#F59E0B");
        }
        notifyUI();
        checkLevelCompletion(appCallbacks);
      }
    }
    state.activeSanctuaryFruit = currentSanctuary;
    state.sanctuaryProgress = currentSanctuaryProg;

    // Power-Ups collision (suppression in-place sans réallocation de tableau)
    for (let i = state.powerUps.length - 1; i >= 0; i--) {
      const p = state.powerUps[i];
      p.phase += dt * 3;
      const dist = Math.hypot(state.playerX - p.x, state.playerY - p.y);
      if (dist <= state.playerRadius + p.radius) {
        state.powerUps.splice(i, 1);
        A.playPowerUp();
        const pInfo = C.PowerUpTypes[p.type];
        spawnBurst(p.x, p.y, pInfo ? pInfo.colorHex : "#00E5FF", 16);
        addFloatingText(p.x, p.y, `${pInfo ? pInfo.iconLabel : "✨"} ${pInfo ? pInfo.displayName : ""}`, pInfo ? pInfo.colorHex : "#00E5FF");

        switch (p.type) {
          case "SHIELD":
            state.shields = Math.min(state.maxShields, state.shields + 1);
            break;
          case "SPEED_BOOST":
            state.speedBoostTimer = 10.0;
            break;
          case "MAGNET":
            state.magnetTimer = 12.0;
            break;
          case "ANTI_ASTEROID":
            state.antiAsteroidTimer = 12.0;
            break;
          case "ANTI_DRONE":
            state.antiDroneTimer = 12.0;
            break;
          case "ANTI_MINE":
            state.antiMineTimer = 12.0;
            break;
          case "EMP_BLAST":
            for (let j = state.obstacles.length - 1; j >= 0; j--) {
              const o = state.obstacles[j];
              if (Math.hypot(o.x - state.playerX, o.y - state.playerY) < 0.75) {
                spawnBurst(o.x, o.y, "#EC4899", 10);
                state.score += 25;
                state.obstacles.splice(j, 1);
              }
            }
            break;
          case "FREE_WILL_AEGIS":
            state.freeWillTimer = 14.0;
            break;
          case "METACOGNITION_BEACON":
            state.metacognitionTimer = 15.0;
            break;
          case "HUMILITY_ANCHOR":
            state.humilityTimer = 14.0;
            break;
          case "MYELIN_SHEATH":
            state.myelinTimer = 12.0;
            break;
          case "CONTEMPLATIVE_PHASE_SHIFT":
            state.phaseShiftTimer = 10.0;
            break;
          case "PLENITUDE_ARK":
            state.plenitudeTimer = 12.0;
            break;
        }
        notifyUI();
      }
    }

    // Si toutes les reliques de Fruits et tous les neurones sont accomplis, attirer automatiquement les dernières pensées pures vers le joueur pour éviter d'errer dans le vide
    const allFruitDoneNow = state.collectedCount >= (state.totalFruitRelicsCount || 3);
    const allNeuronsDoneNow = state.infectedNeurons.length === 0;
    const autoHomeRemainingTargets = allFruitDoneNow && allNeuronsDoneNow;

    // Rescue Targets update (suppression in-place dès qu'une pensée est sauvée)
    for (let i = state.rescueTargets.length - 1; i >= 0; i--) {
      const t = state.rescueTargets[i];
      if (t.isSaved || t.isDestroyed) {
        state.rescueTargets.splice(i, 1);
        continue;
      }
      t.phase += dt * 3.5;
      t.x += t.vx * dt;
      t.y += t.vy * dt;
      if (t.x < 0.12 || t.x > WORLD_SIZE - 0.12) t.vx *= -1;
      if (t.y < 0.12 || t.y > WORLD_SIZE - 0.12) t.vy *= -1;

      const dx = state.playerX - t.x;
      const dy = state.playerY - t.y;
      const dist = Math.hypot(dx, dy);

      if (autoHomeRemainingTargets && dist > 0.01) {
        t.x += (dx / dist) * 1.35 * dt;
        t.y += (dy / dist) * 1.35 * dt;
      } else if (effectiveMagnet && dist < 0.45 && dist > 0.01) {
        t.x += (dx / dist) * 0.30 * dt;
        t.y += (dy / dist) * 0.30 * dt;
      }

      if (dist <= state.playerRadius + t.radius) {
        t.isSaved = true;
        state.rescueTargets.splice(i, 1);
        state.targetsSavedCount += 1;
        state.purificationEnergy += 10;
        A.playTargetSaved();
        const tInfo = C.RescueTargetTypes[t.type];
        spawnBurst(t.x, t.y, tInfo.colorHex, 14);
        addFloatingText(t.x, t.y, "✨ Sauvée (+10 Énergie)", tInfo.colorHex);
        if (appCallbacks && appCallbacks.onPositiveThoughtEarned) {
          appCallbacks.onPositiveThoughtEarned(1);
        }
        notifyUI();
        checkLevelCompletion(appCallbacks);
      }
    }

    // Infected Neurons proximity, contact harmonization & update (suppression in-place une fois purifiés)
    let nearestNeuron = null;
    let nearestNeuronDist = 0.55;
    for (let i = state.infectedNeurons.length - 1; i >= 0; i--) {
      const n = state.infectedNeurons[i];
      if (n.isPurified) {
        state.infectedNeurons.splice(i, 1);
        continue;
      }
      n.phase += dt * 2.8;
      const d = Math.hypot(n.x - state.playerX, n.y - state.playerY);
      if (d < nearestNeuronDist) {
        nearestNeuronDist = d;
        nearestNeuron = n.id;
      }

      // Harmonisation de proximité directe : rester tout près d'un neurone le purifie progressivement (évite tout blocage en fin de niveau)
      if (d <= n.radius + state.playerRadius + 0.085) {
        n.purificationProgress = Math.min(1.0, (n.purificationProgress || 0) + dt * 0.55);
        if (n.purificationProgress >= 1.0) {
          A.playNeuronPurified(1.0);
          purifyNeuronEntity(i, 80, "100% Harmonie (+80 pts)", "#10B981", appCallbacks);
          notifyUI();
          checkLevelCompletion(appCallbacks);
        }
      }
    }
    state.nearbyTargetNeuronId = nearestNeuron;

    // Virtue Projectiles homing update (in-place)
    for (let i = state.virtueProjectiles.length - 1; i >= 0; i--) {
      const p = state.virtueProjectiles[i];
      let targetObj = null;
      let targetIdx = -1;

      if (p.targetKind === "NEURON") {
        targetIdx = state.infectedNeurons.findIndex(n => n.id === p.targetId && !n.isPurified);
        if (targetIdx !== -1) targetObj = state.infectedNeurons[targetIdx];
      } else {
        targetIdx = state.obstacles.findIndex(o => o.id === p.targetId);
        if (targetIdx !== -1) targetObj = state.obstacles[targetIdx];
      }

      if (!targetObj) {
        state.virtueProjectiles.splice(i, 1);
        continue;
      }

      const dx = targetObj.x - p.x;
      const dy = targetObj.y - p.y;
      const dist = Math.hypot(dx, dy);

      if (dist <= targetObj.radius + 0.025) {
        state.virtueProjectiles.splice(i, 1);
        if (p.targetKind === "NEURON") {
          const affinity = C.getRemedyAffinity(targetObj.corruptionType, p.remedyId);
          A.playNeuronPurified(affinity);
          const originX = targetObj.x;
          const originY = targetObj.y;

          if (affinity >= 1.0) {
            purifyNeuronEntity(targetIdx, 80, "100% Harmonie (+80 pts)", "#10B981", appCallbacks);

            // Bioelectric chain or Gamma resonance bonus
            if (p.remedyId === "INFLUX_BIOELECTRIQUE" || p.remedyId === "RESONANCE_GAMMA") {
              for (let k = state.infectedNeurons.length - 1; k >= 0; k--) {
                const other = state.infectedNeurons[k];
                if (!other.isPurified && Math.hypot(other.x - originX, other.y - originY) < 0.75) {
                  purifyNeuronEntity(k, 80, "⚡ Arc 100% (+80)", "#22D3EE", appCallbacks);
                }
              }
            }
          } else {
            // Tout remède projeté fait progresser la purification d'au moins 50% pour ne jamais bloquer la fin de mission
            targetObj.purificationProgress = (targetObj.purificationProgress || 0) + (affinity >= 0.5 ? 0.5 : 0.35);
            if (targetObj.purificationProgress >= 1.0) {
              purifyNeuronEntity(targetIdx, 60, "Purifié ! (+60 pts)", "#10B981", appCallbacks);
            } else {
              spawnBurst(targetObj.x, targetObj.y, "#FBBF24", 12);
              addFloatingText(targetObj.x, targetObj.y, `${Math.round(targetObj.purificationProgress * 100)}% Apaisé`, "#FBBF24");
            }
          }
          notifyUI();
          checkLevelCompletion(appCallbacks);
        } else {
          // Targetable complex obstacle dissolved by Virtue
          state.obstacles.splice(targetIdx, 1);
          state.score += 60;
          spawnBurst(targetObj.x, targetObj.y, p.colorHex, 18);
          addFloatingText(targetObj.x, targetObj.y, `${p.word} ! (+60 pts)`, p.colorHex);
          notifyUI();
        }
      } else {
        p.x += (dx / dist) * p.speed * dt;
        p.y += (dy / dist) * p.speed * dt;
      }
    }

    // Obstacles movement & collisions (in-place)
    for (let i = state.obstacles.length - 1; i >= 0; i--) {
      const o = state.obstacles[i];
      o.rotation += o.rotationSpeed * dt;
      o.phase += dt * 3;
      o.x += o.vx * dt;
      o.y += o.vy * dt;

      if (o.x < 0.08 || o.x > WORLD_SIZE - 0.08) o.vx *= -1;
      if (o.y < 0.08 || o.y > WORLD_SIZE - 0.08) o.vy *= -1;

      // Sanctuary repulsion
      for (let rIdx = 0; rIdx < state.relics.length; rIdx++) {
        const r = state.relics[rIdx];
        const dx = o.x - r.x;
        const dy = o.y - r.y;
        const d = Math.hypot(dx, dy);
        if (d < r.sanctuaryRadius + o.radius && d > 0.001) {
          o.vx = Math.abs(o.vx) * Math.sign(dx);
          o.vy = Math.abs(o.vy) * Math.sign(dy);
          o.x = r.x + (dx / d) * (r.sanctuaryRadius + o.radius + 0.005);
          o.y = r.y + (dy / d) * (r.sanctuaryRadius + o.radius + 0.005);
        }
      }

      // Peace Aura repulsion (when "Ancrer cette pensée" is active)
      if (state.peaceAuraTimer > 0) {
        const dx = o.x - state.playerX;
        const dy = o.y - state.playerY;
        const d = Math.hypot(dx, dy);
        if (d < 0.24 && d > 0.001) {
          o.x = state.playerX + (dx / d) * 0.245;
          o.y = state.playerY + (dy / d) * 0.245;
          o.vx = Math.abs(o.vx) * Math.sign(dx);
          o.vy = Math.abs(o.vy) * Math.sign(dy);
        }
      }

      // Check collision with Rescue Targets (suppression immédiate de l'entité détruite)
      if (o.type !== "MENTAL_MIRE" && o.type !== "OBSCURANTISM_VEIL") {
        for (let tIdx = state.rescueTargets.length - 1; tIdx >= 0; tIdx--) {
          const t = state.rescueTargets[tIdx];
          const dtarg = Math.hypot(o.x - t.x, o.y - t.y);
          if (dtarg < o.radius + t.radius * 0.85) {
            t.isDestroyed = true;
            state.rescueTargets.splice(tIdx, 1);
            state.targetsDestroyedCount += 1;
            spawnBurst(t.x, t.y, "#F43F5E", 12);
            addFloatingText(t.x, t.y, "💔 Pensée Perdue", "#F43F5E");
            notifyUI();
            checkLevelCompletion(appCallbacks);
          }
        }
      }

      // Check collision with Player
      const distPlayer = Math.hypot(o.x - state.playerX, o.y - state.playerY);
      if (distPlayer < o.radius + state.playerRadius) {
        const canDestroy =
          state.plenitudeTimer > 0 ||
          (o.type === "ASTEROID" && state.antiAsteroidTimer > 0) ||
          (o.type === "LASER_DRONE" && state.antiDroneTimer > 0) ||
          (o.type === "PULSAR_MINE" && state.antiMineTimer > 0) ||
          ((o.type === "CONFORMISM_SWARM" || o.type === "DOGMATIC_PRISM") && state.freeWillTimer > 0) ||
          (o.type === "OBSCURANTISM_VEIL" && state.metacognitionTimer > 0) ||
          (o.type === "NARCISSUS_MIRROR" && state.humilityTimer > 0);

        if (canDestroy) {
          state.obstacles.splice(i, 1);
          state.score += 35;
          spawnBurst(o.x, o.y, "#00E5FF", 14);
          addFloatingText(o.x, o.y, "+35 Dissous !", "#00E5FF");
          notifyUI();
          continue;
        }

        if (o.type === "MENTAL_MIRE" || o.type === "OBSCURANTISM_VEIL") {
          continue;
        }

        if (o.type === "CONFORMISM_SWARM" && state.freeWillTimer <= 0 && state.phaseShiftTimer <= 0) {
          state.playerX += o.vx * dt * 1.4;
          state.playerY += o.vy * dt * 1.4;
          continue;
        }

        if (o.type === "NARCISSUS_MIRROR" && state.humilityTimer <= 0 && state.phaseShiftTimer <= 0) {
          const nx = (state.playerX - o.x) / Math.max(0.001, distPlayer);
          const ny = (state.playerY - o.y) / Math.max(0.001, distPlayer);
          state.playerX = o.x + nx * (o.radius + state.playerRadius + 0.06);
          state.playerY = o.y + ny * (o.radius + state.playerRadius + 0.06);
          state.score = Math.max(0, state.score - 15);
          addFloatingText(o.x, o.y, "🪞 Illusion (-15)", "#FDE047");
          A.playObstacleHit();
          notifyUI();
          continue;
        }

        if (state.invulnerableTimer <= 0 && state.phaseShiftTimer <= 0 && state.activeSanctuaryFruit === null) {
          state.shields -= 1;
          state.invulnerableTimer = 1.6;
          A.playObstacleHit();
          spawnBurst(state.playerX, state.playerY, "#F43F5E", 18);
          addFloatingText(state.playerX, state.playerY, "💔 -1 Bouclier", "#F43F5E");
          if (state.shields <= 0) {
            state.isGameOver = true;
            A.playGameOver();
          }
          notifyUI();
        }
      }
    }

    // Update particles & floating texts in-place
    for (let i = state.particles.length - 1; i >= 0; i--) {
      const pt = state.particles[i];
      pt.x += pt.vx * dt;
      pt.y += pt.vy * dt;
      pt.alpha -= dt * 1.45;
      if (pt.alpha <= 0) {
        state.particles.splice(i, 1);
      }
    }

    for (let i = state.floatingTexts.length - 1; i >= 0; i--) {
      const ft = state.floatingTexts[i];
      ft.y += ft.vy * dt;
      ft.alpha -= dt * 0.9;
      if (ft.alpha <= 0) {
        state.floatingTexts.splice(i, 1);
      }
    }

    // Vérification systématique de la condition de fin de mission à chaque tick
    checkLevelCompletion(appCallbacks);
  }

  function render(appCallbacks) {
    if (!canvas || !ctx) return;
    const w = canvas.width;
    const h = canvas.height;
    const minDim = Math.min(w, h);
    const nowSec = performance.now() / 1000;
    const settings = appCallbacks ? appCallbacks.getSettings() : {};
    const sphereColor = appCallbacks ? appCallbacks.getSphereColor() : C.AvailableSphereColors[0];

    ctx.clearRect(0, 0, w, h);

    // 1. Deep space gradient background
    const bgGrad = ctx.createRadialGradient(w * 0.5, h * 0.4, 20, w * 0.5, h * 0.5, Math.max(w, h) * 0.75);
    bgGrad.addColorStop(0, "#101935");
    bgGrad.addColorStop(0.6, "#090E22");
    bgGrad.addColorStop(1, "#050814");
    ctx.fillStyle = bgGrad;
    ctx.fillRect(0, 0, w, h);

    const toScreenX = (wx) => (wx - state.cameraX) * w;
    const toScreenY = (wy) => (wy - state.cameraY) * h;

    // 2. Optional Tactical Grid
    if (settings.showGridEnabled) {
      ctx.strokeStyle = "rgba(0, 229, 255, 0.06)";
      ctx.lineWidth = 1;
      const step = 0.25;
      for (let gx = 0; gx <= WORLD_SIZE; gx += step) {
        const sx = toScreenX(gx);
        if (sx >= 0 && sx <= w) {
          ctx.beginPath();
          ctx.moveTo(sx, 0);
          ctx.lineTo(sx, h);
          ctx.stroke();
        }
      }
      for (let gy = 0; gy <= WORLD_SIZE; gy += step) {
        const sy = toScreenY(gy);
        if (sy >= 0 && sy <= h) {
          ctx.beginPath();
          ctx.moveTo(0, sy);
          ctx.lineTo(w, sy);
          ctx.stroke();
        }
      }
    }

    // 3. Twinkling Starfield
    stars.forEach(s => {
      const sx = toScreenX(s.x);
      const sy = toScreenY(s.y);
      if (sx >= -10 && sx <= w + 10 && sy >= -10 && sy <= h + 10) {
        const twinkle = 0.35 + 0.65 * Math.abs(Math.sin(nowSec * 1.8 + s.phase));
        ctx.fillStyle = `rgba(248, 250, 252, ${s.alpha * twinkle})`;
        ctx.beginPath();
        ctx.arc(sx, sy, s.size, 0, Math.PI * 2);
        ctx.fill();
      }
    });

    // 4. Toroidal World Borders (0.0 to 4.0)
    const leftX = toScreenX(0.02);
    const rightX = toScreenX(WORLD_SIZE - 0.02);
    const topY = toScreenY(0.02);
    const bottomY = toScreenY(WORLD_SIZE - 0.02);

    ctx.save();
    ctx.strokeStyle = "rgba(0, 229, 255, 0.55)";
    ctx.lineWidth = 3;
    ctx.setLineDash([12, 8]);
    ctx.lineDashOffset = -nowSec * 40;
    ctx.strokeRect(leftX, topY, rightX - leftX, bottomY - topY);
    ctx.restore();

    // Border Warp Wave Flash Effects
    state.borderWarpEffects.forEach(bw => {
      const alpha = (1 - bw.progress) * 0.65;
      ctx.fillStyle = `rgba(0, 229, 255, ${alpha})`;
      if (bw.side === "LEFT") ctx.fillRect(0, 0, 24 * (1 - bw.progress), h);
      if (bw.side === "RIGHT") ctx.fillRect(w - 24 * (1 - bw.progress), 0, 24, h);
      if (bw.side === "TOP") ctx.fillRect(0, 0, w, 24 * (1 - bw.progress));
      if (bw.side === "BOTTOM") ctx.fillRect(0, h - 24 * (1 - bw.progress), w, 24);
    });

    // 5. Relics & Sanctuaries of Harmonization
    state.relics.forEach(r => {
      if (r.isCollected) return;
      const sx = toScreenX(r.x);
      const sy = toScreenY(r.y);
      const sr = r.sanctuaryRadius * minDim;
      const rr = r.radius * minDim;

      if (sx < -sr - 40 || sx > w + sr + 40 || sy < -sr - 40 || sy > h + sr + 40) return;

      const baseColor = r.isBrainBonus ? "#F43F5E" : "#F59E0B";

      // Sanctuary circle
      ctx.save();
      ctx.beginPath();
      ctx.arc(sx, sy, sr, 0, Math.PI * 2);
      ctx.fillStyle = r.isBrainBonus ? "rgba(244, 63, 94, 0.06)" : "rgba(245, 158, 11, 0.06)";
      ctx.fill();
      ctx.strokeStyle = r.isBrainBonus ? "rgba(244, 63, 94, 0.28)" : "rgba(245, 158, 11, 0.28)";
      ctx.lineWidth = 1.5;
      ctx.setLineDash([6, 6]);
      ctx.stroke();
      ctx.restore();

      // Harmonization progress arc
      if (r.harmonizationProgress > 0.01) {
        ctx.beginPath();
        ctx.arc(sx, sy, sr, -Math.PI / 2, -Math.PI / 2 + Math.PI * 2 * r.harmonizationProgress);
        ctx.strokeStyle = "#10B981";
        ctx.lineWidth = 4;
        ctx.stroke();
      }

      // Relic core
      const pulse = 1 + 0.1 * Math.sin(nowSec * 4 + r.id);
      const grad = ctx.createRadialGradient(sx, sy, 2, sx, sy, rr * 1.5 * pulse);
      grad.addColorStop(0, "#FFFFFF");
      grad.addColorStop(0.45, baseColor);
      grad.addColorStop(1, "rgba(0,0,0,0)");
      ctx.fillStyle = grad;
      ctx.beginPath();
      ctx.arc(sx, sy, rr * 1.5 * pulse, 0, Math.PI * 2);
      ctx.fill();

      // Label
      ctx.fillStyle = "#F8FAFC";
      ctx.font = "bold 12px 'Plus Jakarta Sans', sans-serif";
      ctx.textAlign = "center";
      ctx.fillText(r.fruitName, sx, sy + rr + 16);
    });

    // 6. Power-Ups
    state.powerUps.forEach(p => {
      const sx = toScreenX(p.x);
      const sy = toScreenY(p.y);
      const pr = p.radius * minDim;
      if (sx < -50 || sx > w + 50 || sy < -50 || sy > h + 50) return;

      const info = C.PowerUpTypes[p.type] || C.PowerUpTypes.SHIELD;
      ctx.save();
      ctx.beginPath();
      ctx.arc(sx, sy, pr * (1 + 0.08 * Math.sin(p.phase)), 0, Math.PI * 2);
      ctx.fillStyle = "rgba(17, 24, 39, 0.85)";
      ctx.fill();
      ctx.lineWidth = 2.5;
      ctx.strokeStyle = info.colorHex;
      ctx.stroke();

      ctx.font = `${Math.round(pr * 1.1)}px sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText(info.iconLabel, sx, sy);
      ctx.restore();
    });

    // 7. Rescue Targets (Pensées Pures)
    state.rescueTargets.forEach(t => {
      if (t.isSaved || t.isDestroyed) return;
      const sx = toScreenX(t.x);
      const sy = toScreenY(t.y);
      const tr = t.radius * minDim;
      if (sx < -50 || sx > w + 50 || sy < -50 || sy > h + 50) return;

      const info = C.RescueTargetTypes[t.type];
      ctx.save();
      ctx.beginPath();
      ctx.arc(sx, sy, tr * (1.1 + 0.12 * Math.sin(t.phase)), 0, Math.PI * 2);
      ctx.fillStyle = `${info.colorHex}28`;
      ctx.fill();
      ctx.strokeStyle = info.colorHex;
      ctx.lineWidth = 2;
      ctx.stroke();

      ctx.font = `${Math.round(tr * 1.1)}px sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText(info.symbol, sx, sy);
      ctx.restore();
    });

    // 8. Infected Neurons
    state.infectedNeurons.forEach(n => {
      const sx = toScreenX(n.x);
      const sy = toScreenY(n.y);
      const nr = n.radius * minDim;
      if (sx < -80 || sx > w + 80 || sy < -80 || sy > h + 80) return;

      const corr = C.NeuronCorruptionTypes[n.corruptionType];
      const color = n.isPurified ? "#10B981" : corr.corruptedColorHex;

      ctx.save();
      // Synaptic dendrites
      ctx.strokeStyle = color;
      ctx.lineWidth = 2;
      for (let d = 0; d < 6; d++) {
        const ang = (Math.PI * 2 * d) / 6 + Math.sin(n.phase * 0.5) * 0.2;
        ctx.beginPath();
        ctx.moveTo(sx, sy);
        ctx.lineTo(sx + Math.cos(ang) * nr * 1.45, sy + Math.sin(ang) * nr * 1.45);
        ctx.stroke();
      }

      // Soma body
      ctx.beginPath();
      ctx.arc(sx, sy, nr * (1 + 0.07 * Math.sin(n.phase)), 0, Math.PI * 2);
      ctx.fillStyle = "#111827";
      ctx.fill();
      ctx.lineWidth = 3;
      ctx.strokeStyle = color;
      ctx.stroke();

      ctx.font = `${Math.round(nr * 0.9)}px sans-serif`;
      ctx.textAlign = "center";
      ctx.textBaseline = "middle";
      ctx.fillText(n.isPurified ? "🌟" : "🦠", sx, sy);

      // Symptom label below
      ctx.font = "bold 11px 'Plus Jakarta Sans', sans-serif";
      ctx.fillStyle = color;
      ctx.fillText(n.isPurified ? "Neurone Purifié" : corr.symptomLabel, sx, sy + nr + 15);
      ctx.restore();
    });

    // 9. Obstacles (8 Archetypes + 29 Variants)
    state.obstacles.forEach(o => {
      const sx = toScreenX(o.x);
      const sy = toScreenY(o.y);
      const or = o.radius * minDim;
      if (sx < -120 || sx > w + 120 || sy < -120 || sy > h + 120) return;

      const arch = C.ObstacleTypes[o.type] || C.ObstacleTypes.ASTEROID;
      const fillHex = o.variant ? o.variant.colorHex : arch.colorHex;
      const strokeHex = o.variant ? o.variant.accentHex : arch.strokeHex;

      ctx.save();
      ctx.translate(sx, sy);

      if (o.type === "MENTAL_MIRE") {
        ctx.beginPath();
        ctx.arc(0, 0, or, 0, Math.PI * 2);
        ctx.fillStyle = "rgba(101, 163, 13, 0.22)";
        ctx.fill();
        ctx.strokeStyle = "rgba(163, 230, 53, 0.5)";
        ctx.lineWidth = 2;
        ctx.setLineDash([8, 6]);
        ctx.stroke();
        ctx.font = "bold 11px 'Plus Jakarta Sans', sans-serif";
        ctx.fillStyle = "#A3E635";
        ctx.textAlign = "center";
        ctx.fillText("🕸️ Marécage Mental", 0, 4);
      } else if (o.type === "OBSCURANTISM_VEIL") {
        ctx.beginPath();
        ctx.arc(0, 0, or, 0, Math.PI * 2);
        ctx.fillStyle = "rgba(71, 85, 105, 0.45)";
        ctx.fill();
        ctx.strokeStyle = "#94A3B8";
        ctx.lineWidth = 2;
        ctx.stroke();
        ctx.font = "bold 11px 'Plus Jakarta Sans', sans-serif";
        ctx.fillStyle = "#CBD5E1";
        ctx.textAlign = "center";
        ctx.fillText("🌑 Voile d'Obscurantisme", 0, 4);
      } else {
        ctx.rotate((o.rotation * Math.PI) / 180);
        ctx.beginPath();
        const sides = o.type === "DOGMATIC_PRISM" ? 3 : o.type === "LASER_DRONE" ? 4 : 6;
        for (let s = 0; s < sides; s++) {
          const a = (Math.PI * 2 * s) / sides;
          const px = Math.cos(a) * or;
          const py = Math.sin(a) * or;
          if (s === 0) ctx.moveTo(px, py);
          else ctx.lineTo(px, py);
        }
        ctx.closePath();
        ctx.fillStyle = fillHex;
        ctx.fill();
        ctx.lineWidth = 2.2;
        ctx.strokeStyle = strokeHex;
        ctx.stroke();
        ctx.rotate((-o.rotation * Math.PI) / 180);

        if (o.variant) {
          ctx.font = `${Math.max(12, Math.round(or * 0.85))}px sans-serif`;
          ctx.textAlign = "center";
          ctx.textBaseline = "middle";
          ctx.fillText(o.variant.symbolEmoji, 0, 0);

          ctx.font = "bold 10px 'Plus Jakarta Sans', sans-serif";
          ctx.fillStyle = strokeHex;
          ctx.fillText(o.variant.shortLabel, 0, or + 12);
        }
      }
      ctx.restore();
    });

    // 10. Virtue Word Projectiles
    state.virtueProjectiles.forEach(vp => {
      const sx = toScreenX(vp.x);
      const sy = toScreenY(vp.y);
      ctx.save();
      ctx.beginPath();
      ctx.arc(sx, sy, 14, 0, Math.PI * 2);
      ctx.fillStyle = vp.colorHex;
      ctx.fill();
      ctx.font = "bold 12px 'Orbitron', sans-serif";
      ctx.fillStyle = "#FFFFFF";
      ctx.textAlign = "center";
      ctx.fillText(`✨ ${vp.word}`, sx, sy - 18);
      ctx.restore();
    });

    // 11. Player Trail & "Étincelle de l'Esprit" Sphere
    state.PlayerTrail.forEach((pt, idx) => {
      const tx = toScreenX(pt.x);
      const ty = toScreenY(pt.y);
      const r = state.playerRadius * minDim * (idx / state.PlayerTrail.length) * 0.85;
      ctx.beginPath();
      ctx.arc(tx, ty, r, 0, Math.PI * 2);
      ctx.fillStyle = `${sphereColor.primaryHex}33`;
      ctx.fill();
    });

    const px = toScreenX(state.playerX);
    const py = toScreenY(state.playerY);
    const pr = state.playerRadius * minDim;

    // Active defense auras
    if (state.peaceAuraTimer > 0) {
      ctx.beginPath();
      ctx.arc(px, py, 0.22 * minDim, 0, Math.PI * 2);
      ctx.strokeStyle = "rgba(16, 185, 129, 0.55)";
      ctx.lineWidth = 3;
      ctx.stroke();
    }
    if (state.plenitudeTimer > 0 || state.freeWillTimer > 0 || state.phaseShiftTimer > 0) {
      ctx.beginPath();
      ctx.arc(px, py, pr * 1.65, 0, Math.PI * 2);
      ctx.strokeStyle = state.plenitudeTimer > 0 ? "#FBBF24" : state.phaseShiftTimer > 0 ? "#C084FC" : "#F472B6";
      ctx.lineWidth = 2.5;
      ctx.stroke();
    }

    // Sphere glow & core
    const pGrad = ctx.createRadialGradient(px, py, 2, px, py, pr * 1.7);
    pGrad.addColorStop(0, "#FFFFFF");
    pGrad.addColorStop(0.45, sphereColor.primaryHex);
    pGrad.addColorStop(1, "rgba(0,0,0,0)");
    ctx.fillStyle = pGrad;
    ctx.beginPath();
    ctx.arc(px, py, pr * 1.7, 0, Math.PI * 2);
    ctx.fill();

    // Prompt if nearby infected neuron & enough energy
    if (state.nearbyTargetNeuronId !== null && state.purificationEnergy >= 10) {
      ctx.save();
      ctx.font = "bold 12px 'Plus Jakarta Sans', sans-serif";
      ctx.fillStyle = "#10B981";
      ctx.textAlign = "center";
      ctx.fillText("✨ Cliquez / Espace : Projeter un Remède", px, py - pr - 16);
      ctx.restore();
    }

    // 12. Particles & Floating Callouts
    state.particles.forEach(pt => {
      const sx = toScreenX(pt.x);
      const sy = toScreenY(pt.y);
      ctx.save();
      ctx.globalAlpha = Math.max(0, pt.alpha);
      ctx.fillStyle = pt.color;
      ctx.beginPath();
      ctx.arc(sx, sy, pt.size, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();
    });

    state.floatingTexts.forEach(ft => {
      const sx = toScreenX(ft.x);
      const sy = toScreenY(ft.y);
      ctx.save();
      ctx.globalAlpha = Math.max(0, ft.alpha);
      ctx.font = "bold 13px 'Plus Jakarta Sans', sans-serif";
      ctx.fillStyle = ft.color;
      ctx.textAlign = "center";
      ctx.fillText(ft.text, sx, sy);
      ctx.restore();
    });

    // 13. Off-Screen Radar Edge Beacons for uncollected Relics
    if (!state.isObscurantismActive) {
      state.relics.forEach(r => {
        if (r.isCollected) return;
        const sx = toScreenX(r.x);
        const sy = toScreenY(r.y);
        if (sx < 0 || sx > w || sy < 0 || sy > h) {
          const angle = Math.atan2(sy - h / 2, sx - w / 2);
          const bx = Math.max(24, Math.min(w - 24, w / 2 + Math.cos(angle) * (w / 2 - 26)));
          const by = Math.max(84, Math.min(h - 24, h / 2 + Math.sin(angle) * (h / 2 - 26)));
          ctx.save();
          ctx.beginPath();
          ctx.arc(bx, by, 10, 0, Math.PI * 2);
          ctx.fillStyle = r.isBrainBonus ? "#F43F5E" : "#F59E0B";
          ctx.fill();
          ctx.restore();
        }
      });
    }

    // 14. Top-Right 4x Mini-Map Radar
    if (settings.showMiniMapEnabled !== false) {
      const mapSize = Math.min(116, Math.round(minDim * 0.24));
      const mapX = w - mapSize - 16;
      const mapY = 92;

      ctx.save();
      ctx.fillStyle = "rgba(15, 23, 42, 0.86)";
      ctx.strokeStyle = "rgba(0, 229, 255, 0.45)";
      ctx.lineWidth = 1.5;
      ctx.beginPath();
      ctx.roundRect(mapX, mapY, mapSize, mapSize, 10);
      ctx.fill();
      ctx.stroke();

      if (state.isObscurantismActive) {
        ctx.fillStyle = "#94A3B8";
        ctx.font = "bold 10px 'Plus Jakarta Sans', sans-serif";
        ctx.textAlign = "center";
        ctx.fillText("🌑 VOILE", mapX + mapSize / 2, mapY + mapSize / 2 - 4);
        ctx.fillText("BROUILLÉ", mapX + mapSize / 2, mapY + mapSize / 2 + 10);
      } else {
        // Viewport box
        const vx = mapX + (state.cameraX / WORLD_SIZE) * mapSize;
        const vy = mapY + (state.cameraY / WORLD_SIZE) * mapSize;
        const vw = (VIEW_SIZE / WORLD_SIZE) * mapSize;
        ctx.strokeStyle = "rgba(255, 255, 255, 0.35)";
        ctx.lineWidth = 1;
        ctx.strokeRect(vx, vy, vw, vw);

        // Relics on mini-map
        state.relics.forEach(r => {
          if (r.isCollected) return;
          ctx.fillStyle = r.isBrainBonus ? "#F43F5E" : "#F59E0B";
          ctx.beginPath();
          ctx.arc(mapX + (r.x / WORLD_SIZE) * mapSize, mapY + (r.y / WORLD_SIZE) * mapSize, 3.2, 0, Math.PI * 2);
          ctx.fill();
        });

        // Unsaved targets on mini-map
        state.rescueTargets.forEach(t => {
          if (t.isSaved || t.isDestroyed) return;
          ctx.fillStyle = "#38BDF8";
          ctx.beginPath();
          ctx.arc(mapX + (t.x / WORLD_SIZE) * mapSize, mapY + (t.y / WORLD_SIZE) * mapSize, 2.2, 0, Math.PI * 2);
          ctx.fill();
        });

        // Infected neurons on mini-map
        state.infectedNeurons.forEach(n => {
          if (n.isPurified) return;
          ctx.fillStyle = "#A855F7";
          ctx.beginPath();
          ctx.arc(mapX + (n.x / WORLD_SIZE) * mapSize, mapY + (n.y / WORLD_SIZE) * mapSize, 2.8, 0, Math.PI * 2);
          ctx.fill();
        });

        // Player dot on mini-map
        ctx.fillStyle = sphereColor.primaryHex;
        ctx.beginPath();
        ctx.arc(
          mapX + (state.playerX / WORLD_SIZE) * mapSize,
          mapY + (state.playerY / WORLD_SIZE) * mapSize,
          3.8,
          0,
          Math.PI * 2
        );
        ctx.fill();
      }
      ctx.restore();
    }

    // 15. Round Directional Joystick (Bottom-Right)
    if (settings.joystickEnabled !== false) {
      const joyR = 54;
      const joyCx = w - joyR - 26;
      const joyCy = h - joyR - 26;

      ctx.save();
      ctx.beginPath();
      ctx.arc(joyCx, joyCy, joyR, 0, Math.PI * 2);
      ctx.fillStyle = "rgba(15, 23, 42, 0.72)";
      ctx.fill();
      ctx.lineWidth = 2;
      ctx.strokeStyle = joystickTouchId !== null ? sphereColor.primaryHex : "rgba(148, 163, 184, 0.38)";
      ctx.stroke();

      // Cardinal dots
      [0, Math.PI / 2, Math.PI, (Math.PI * 3) / 2].forEach(ang => {
        ctx.beginPath();
        ctx.arc(joyCx + Math.cos(ang) * (joyR - 10), joyCy + Math.sin(ang) * (joyR - 10), 2.5, 0, Math.PI * 2);
        ctx.fillStyle = "rgba(148, 163, 184, 0.45)";
        ctx.fill();
      });

      const knobX = joyCx + joystickVec.x * (joyR * 0.62);
      const knobY = joyCy + joystickVec.y * (joyR * 0.62);

      ctx.beginPath();
      ctx.moveTo(joyCx, joyCy);
      ctx.lineTo(knobX, knobY);
      ctx.strokeStyle = `${sphereColor.primaryHex}88`;
      ctx.lineWidth = 3;
      ctx.stroke();

      ctx.beginPath();
      ctx.arc(knobX, knobY, 18, 0, Math.PI * 2);
      ctx.fillStyle = sphereColor.primaryHex;
      ctx.fill();
      ctx.restore();
    }
  }

  let canvasEventsBound = false;
  let hangarAnimFrameId = null;

  function bindCanvasEvents(canvasEl, appCallbacks) {
    canvas = canvasEl;
    if (!canvas) return;
    ctx = canvas.getContext("2d");
    if (canvasEventsBound) return;
    canvasEventsBound = true;

    function resize() {
      if (!canvas || canvas.offsetParent === null) return;
      const rect = canvas.getBoundingClientRect();
      const targetW = Math.max(300, Math.floor(rect.width));
      const targetH = Math.max(300, Math.floor(rect.height));
      if (canvas.width !== targetW || canvas.height !== targetH) {
        canvas.width = targetW;
        canvas.height = targetH;
      }
    }
    window.addEventListener("resize", resize);
    resize();

    function getJoystickGeom() {
      const joyR = 54;
      return {
        cx: canvas.width - joyR - 26,
        cy: canvas.height - joyR - 26,
        r: joyR
      };
    }

    canvas.addEventListener("pointerdown", (e) => {
      A.ensureContext();
      const rect = canvas.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      // 1. Check if user clicked on Player Sphere to open Virtue Remedy chooser
      const px = (state.playerX - state.cameraX) * canvas.width;
      const py = (state.playerY - state.cameraY) * canvas.height;
      const pr = state.playerRadius * Math.min(canvas.width, canvas.height) * 1.8;
      if (Math.hypot(x - px, y - py) <= pr && state.purificationEnergy >= 10) {
        state.isVirtueMenuOpen = true;
        notifyUI();
        return;
      }

      // 2. Check if user touched inside Round Directional Joystick
      const settings = appCallbacks ? appCallbacks.getSettings() : {};
      if (settings.joystickEnabled !== false) {
        const j = getJoystickGeom();
        const dj = Math.hypot(x - j.cx, y - j.cy);
        if (dj <= j.r * 1.25) {
          joystickTouchId = e.pointerId;
          try { canvas.setPointerCapture(e.pointerId); } catch (_) {}
          const clamped = Math.min(j.r, dj);
          const ang = Math.atan2(y - j.cy, x - j.cx);
          joystickVec.x = (Math.cos(ang) * clamped) / j.r;
          joystickVec.y = (Math.sin(ang) * clamped) / j.r;
          return;
        }
      }

      // 3. Otherwise direct canvas navigation toward pointer
      canvasPointerDown = true;
      canvasPointerWorld = {
        x: state.cameraX + x / canvas.width,
        y: state.cameraY + y / canvas.height
      };
    });

    canvas.addEventListener("pointermove", (e) => {
      const rect = canvas.getBoundingClientRect();
      const x = e.clientX - rect.left;
      const y = e.clientY - rect.top;

      if (joystickTouchId === e.pointerId) {
        const j = getJoystickGeom();
        const dj = Math.hypot(x - j.cx, y - j.cy);
        const clamped = Math.min(j.r, dj);
        const ang = Math.atan2(y - j.cy, x - j.cx);
        joystickVec.x = (Math.cos(ang) * clamped) / j.r;
        joystickVec.y = (Math.sin(ang) * clamped) / j.r;
      } else if (canvasPointerDown) {
        canvasPointerWorld = {
          x: state.cameraX + x / canvas.width,
          y: state.cameraY + y / canvas.height
        };
      }
    });

    function clearPointer(e) {
      if (joystickTouchId === e.pointerId) {
        joystickTouchId = null;
        joystickVec.x = 0;
        joystickVec.y = 0;
      }
      canvasPointerDown = false;
      canvasPointerWorld = null;
    }

    canvas.addEventListener("pointerup", clearPointer);
    canvas.addEventListener("pointercancel", clearPointer);

    window.addEventListener("keydown", (e) => {
      if (e.code === "Space" && state.purificationEnergy >= 10) {
        state.isVirtueMenuOpen = !state.isVirtueMenuOpen;
        notifyUI();
      }
      keysDown.add(e.code);
    });
    window.addEventListener("keyup", (e) => {
      keysDown.delete(e.code);
    });
  }

  function startLoop(appCallbacks) {
    if (animFrameId) {
      cancelAnimationFrame(animFrameId);
      animFrameId = null;
    }
    lastTime = performance.now();
    function loop(now) {
      const rawDt = (now - lastTime) / 1000;
      lastTime = now;
      const dt = Math.max(0.001, Math.min(0.05, rawDt));

      if (!document.hidden && canvas && canvas.offsetParent !== null) {
        const cw = Math.max(300, canvas.clientWidth);
        const ch = Math.max(300, canvas.clientHeight);
        if (canvas.width !== cw || canvas.height !== ch) {
          canvas.width = cw;
          canvas.height = ch;
        }
        update(dt, appCallbacks);
        render(appCallbacks);
      }
      animFrameId = requestAnimationFrame(loop);
    }
    animFrameId = requestAnimationFrame(loop);
  }

  function notifyUI() {
    if (typeof onStateChange === "function") {
      onStateChange(state);
    }
  }

  // Hangar animated sphere preview (optimisé : en pause quand l'onglet Hangar n'est pas visible)
  function startHangarSphereAnimation(hangarCanvas, getSphereColorFn) {
    if (!hangarCanvas) return;
    if (hangarAnimFrameId) {
      cancelAnimationFrame(hangarAnimFrameId);
      hangarAnimFrameId = null;
    }
    const hCtx = hangarCanvas.getContext("2d");
    function drawHangarSphere(now) {
      if (!document.hidden && hangarCanvas.offsetParent !== null) {
        const t = now / 1000;
        const w = hangarCanvas.width;
        const h = hangarCanvas.height;
        const cx = w / 2;
        const cy = h / 2;
        const color = getSphereColorFn();

        hCtx.clearRect(0, 0, w, h);
        const pulse = 1 + 0.08 * Math.sin(t * 2.6);

        // Outer aura
        const grad = hCtx.createRadialGradient(cx, cy, 4, cx, cy, 56 * pulse);
        grad.addColorStop(0, "#FFFFFF");
        grad.addColorStop(0.38, color.primaryHex);
        grad.addColorStop(0.75, `${color.glowHex}66`);
        grad.addColorStop(1, "rgba(0,0,0,0)");
        hCtx.fillStyle = grad;
        hCtx.beginPath();
        hCtx.arc(cx, cy, 56 * pulse, 0, Math.PI * 2);
        hCtx.fill();

        // Orbiting sparks
        hCtx.fillStyle = "#FFFFFF";
        for (let i = 0; i < 3; i++) {
          const ang = t * 1.8 + (i * Math.PI * 2) / 3;
          const ox = cx + Math.cos(ang) * 42;
          const oy = cy + Math.sin(ang) * 42;
          hCtx.beginPath();
          hCtx.arc(ox, oy, 3, 0, Math.PI * 2);
          hCtx.fill();
        }
      }

      hangarAnimFrameId = requestAnimationFrame(drawHangarSphere);
    }
    hangarAnimFrameId = requestAnimationFrame(drawHangarSphere);
  }

  return {
    state,
    initLevel,
    bindCanvasEvents,
    startLoop,
    fireVirtueRemedy,
    checkLevelCompletion,
    startHangarSphereAnimation,
    setOnStateChange: (fn) => { onStateChange = fn; },
    activatePeaceAura: () => {
      state.peaceAuraTimer = 12.0;
      notifyUI();
    }
  };
})();
