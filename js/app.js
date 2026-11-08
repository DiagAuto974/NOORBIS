// ============================================================================
// NOÔRBIS — CONTRÔLEUR D'APPLICATION WEB (HANGAR, JEU, INVENTAIRE, GLOSSAIRE, EXPORT)
// ============================================================================

(function () {
  const C = window.NoorbisCatalog;
  const A = window.NoorbisAudio;
  const G = window.NoorbisGame;

  const STORAGE_KEY = "noorbis_web_state_v2";

  const appState = {
    currentTab: "hangar",
    selectedColorId: "cyan",
    unlockedLevel: 1,
    totalScore: 0,
    positiveThoughtsCount: 0,
    maxShields: 3,
    collectedItems: [], // [{ id, fruit, situation, reference, content, collectedAt }]
    levelRecords: {},   // { [level]: { isCompleted, bestScore, collectedCount, brainBonusCollected } }
    claimedShareIds: [],
    hangarFilter: "ALL", // ALL, MILESTONES, UNCOMPLETED
    inventorySearch: "",
    inventoryFilter: null, // null, "__BRAIN__", or fruit name
    glossaryCategory: null, // null, "OBSTACLE", "TARGET", "RELIC", "BONUS"
    settings: {
      sfxEnabled: true,
      musicThemeOverride: -1, // -2 = Off, -1 = Auto, 0..13 = Manual
      inventorySortAscending: false,
      joystickEnabled: true,
      directTextEnabled: true,
      showGridEnabled: false,
      showMiniMapEnabled: true
    }
  };

  function loadState() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (raw) {
        const parsed = JSON.parse(raw);
        Object.assign(appState, parsed);
        if (parsed.settings) {
          appState.settings = Object.assign({}, appState.settings, parsed.settings);
        }
      }
    } catch (_) {}
    appState.maxShields = Math.min(7, 3 + Math.floor((appState.positiveThoughtsCount || 0) / 50));
  }

  function saveState() {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(appState));
    } catch (_) {}
  }

  function showToast(msg) {
    const container = document.getElementById("toast-container");
    if (!container) return;
    const el = document.createElement("div");
    el.className = "toast-msg";
    el.textContent = msg;
    container.appendChild(el);
    setTimeout(() => {
      el.remove();
    }, 3200);
  }

  function getSphereColor() {
    return C.AvailableSphereColors.find(c => c.id === appState.selectedColorId) || C.AvailableSphereColors[0];
  }

  function getCollectedIdsSet() {
    return new Set(appState.collectedItems.map(i => i.id));
  }

  const appCallbacks = {
    getSettings: () => appState.settings,
    getSphereColor,
    getCollectedIdsSet,
    onCollectMeditation: (med) => {
      if (!appState.collectedItems.some(x => x.id === med.id)) {
        appState.collectedItems.push({
          ...med,
          collectedAt: Date.now()
        });
        saveState();
      }
      if (appState.settings.directTextEnabled) {
        openDirectMeditationModal(med, true);
      }
    },
    onPositiveThoughtEarned: (delta) => {
      const oldMax = appState.maxShields;
      appState.positiveThoughtsCount = (appState.positiveThoughtsCount || 0) + delta;
      appState.maxShields = Math.min(7, 3 + Math.floor(appState.positiveThoughtsCount / 50));
      if (appState.maxShields > oldMax) {
        G.state.maxShields = appState.maxShields;
        G.state.shields = Math.min(G.state.maxShields, G.state.shields + 1);
        showToast(`💖 Palier de 50 Pensées atteint ! Cœurs max : ${appState.maxShields}/7`);
      }
      saveState();
    },
    onTriggerEndMissionTally: () => {
      openEndMissionTallyModal();
    }
  };

  // ==========================================================================
  // NAVIGATION & TABS
  // ==========================================================================
  function switchTab(tabId) {
    A.playClick();
    if (tabId === "settings") {
      openSettingsModal();
      return;
    }
    appState.currentTab = tabId;
    document.querySelectorAll(".nav-item").forEach(btn => {
      btn.classList.toggle("active", btn.dataset.tab === tabId);
    });
    document.querySelectorAll(".screen-view").forEach(view => {
      view.classList.toggle("active", view.id === `view-${tabId}`);
    });

    if (tabId === "hangar") renderHangar();
    if (tabId === "inventory") renderInventory();
    if (tabId === "game") {
      updateGameHUD();
    }
  }

  // ==========================================================================
  // HANGAR VIEW
  // ==========================================================================
  function renderHangar() {
    const sphereColor = getSphereColor();
    const brainCount = appState.collectedItems.filter(i => i.id >= 1001 && i.id <= 1072).length;
    const fruitMedCount = appState.collectedItems.filter(i => i.id < 1001).length;

    const statScore = document.getElementById("stat-total-score");
    const statMeds = document.getElementById("stat-meditations");
    const statBrain = document.getElementById("stat-brain");
    const statLevel = document.getElementById("stat-level");

    if (statScore) statScore.textContent = appState.totalScore;
    if (statMeds) statMeds.textContent = `${fruitMedCount} / 810`;
    if (statBrain) statBrain.textContent = `${brainCount} / 72`;
    if (statLevel) statLevel.textContent = `#${appState.unlockedLevel} / 135`;

    const colorLabel = document.getElementById("active-sphere-color-label");
    if (colorLabel) {
      colorLabel.textContent = `Couleur : ${sphereColor.name} • Cœurs : ${appState.maxShields}/7 (${appState.positiveThoughtsCount % 50}/50 ✨)`;
    }

    // Render Color Picker
    const colorRow = document.getElementById("color-picker-row");
    if (colorRow) {
      colorRow.innerHTML = "";
      C.AvailableSphereColors.forEach(c => {
        const isSel = c.id === appState.selectedColorId;
        const card = document.createElement("div");
        card.className = `color-card ${isSel ? "selected" : ""}`;
        if (isSel) card.style.borderColor = c.primaryHex;
        card.innerHTML = `
          <div class="color-swatch" style="background:${c.primaryHex};box-shadow:0 0 14px ${c.primaryHex}88">
            ${isSel ? "✓" : ""}
          </div>
          <div class="color-card-name">${c.name}</div>
        `;
        card.addEventListener("click", () => {
          A.playClick();
          appState.selectedColorId = c.id;
          saveState();
          renderHangar();
        });
        colorRow.appendChild(card);
      });
    }

    // Hangar filter chips
    document.querySelectorAll("[data-hangar-filter]").forEach(btn => {
      btn.classList.toggle("active", btn.dataset.hangarFilter === appState.hangarFilter);
    });

    // Render 135 Levels
    const levelsList = document.getElementById("levels-list");
    if (!levelsList) return;
    levelsList.innerHTML = "";

    const milestoneLevels = new Set([1, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100, 105, 110, 115, 120, 125, 130, 135]);
    const collectedIds = getCollectedIdsSet();

    C.getAllSectors().forEach(sector => {
      const rec = appState.levelRecords[sector.levelNumber] || null;
      const isCompleted = Boolean(rec && rec.isCompleted);
      const isUnlocked = sector.levelNumber <= appState.unlockedLevel;
      const isCurrent = sector.levelNumber === appState.unlockedLevel;
      const isMilestone = milestoneLevels.has(sector.levelNumber);

      if (appState.hangarFilter === "MILESTONES" && !isMilestone) return;
      if (appState.hangarFilter === "UNCOMPLETED" && isCompleted) return;

      const brainFrag = C.getBrainBonusForLevel(sector.levelNumber);
      const isBrainDone = collectedIds.has(brainFrag.id) || Boolean(rec && rec.brainBonusCollected);

      const card = document.createElement("div");
      card.className = `level-card ${isUnlocked ? "unlocked" : "locked"} ${isCurrent ? "current" : ""} ${isCompleted ? "completed" : ""}`;

      const fruitsHtml = sector.fruits.map(f => {
        return `<span class="mini-tag ${isCompleted ? "done" : ""}">${isCompleted ? "✓ " : "✦ "}${f}</span>`;
      }).join("");

      const brainHtml = `<span class="mini-tag ${isBrainDone ? "done" : "brain"}">🧠 ${isBrainDone ? "✓" : "?"}</span>`;
      const milestoneHtml = isMilestone ? `<span class="mini-tag milestone">★ Palier Nouveauté</span>` : "";

      card.innerHTML = `
        <div class="level-badge">
          ${isCompleted ? "✓" : isUnlocked ? `#${sector.levelNumber}` : "🔒"}
        </div>
        <div class="level-info">
          <div class="level-title-row">
            <span class="level-name">${sector.name}</span>
            ${milestoneHtml}
          </div>
          <div class="level-sub">
            ${sector.subtitle}${rec && rec.bestScore > 0 ? ` • Record : ${rec.bestScore} pts` : ""}
          </div>
          <div class="level-fruits-row">
            ${fruitsHtml}
            ${brainHtml}
          </div>
        </div>
        ${isUnlocked ? `<div class="level-play-icon">▶</div>` : ""}
      `;

      if (isUnlocked) {
        card.addEventListener("click", () => {
          startLevelAndSwitch(sector.levelNumber);
        });
      }
      levelsList.appendChild(card);
    });
  }

  function startLevelAndSwitch(levelNumber) {
    A.playClick();
    A.setConfig(appState.settings.sfxEnabled, appState.settings.musicThemeOverride, levelNumber);
    G.initLevel(levelNumber, appState);
    switchTab("game");
    syncGameModals();
  }

  // ==========================================================================
  // SPACE GAME HUD & OVERLAYS
  // ==========================================================================
  function updateGameHUD() {
    const s = G.state;
    const titleEl = document.getElementById("hud-sector-title");
    if (titleEl) titleEl.textContent = s.sector.name;

    const shieldsEl = document.getElementById("hud-shields-row");
    if (shieldsEl) {
      let hearts = "";
      for (let i = 0; i < s.maxShields; i++) {
        hearts += `<span class="heart-icon" style="opacity:${i < s.shields ? 1 : 0.25}">${i < s.shields ? "❤️" : "🖤"}</span>`;
      }
      const remainingTargets = Math.max(0, s.totalTargetsCount - s.targetsSavedCount - s.targetsDestroyedCount);
      const remainingNeurons = Math.max(0, s.totalNeuronsCount - s.neuronsPurifiedCount);

      shieldsEl.innerHTML = `
        ${hearts}
        <span class="hud-badge" style="background:rgba(56,189,248,0.18);color:#38BDF8">
          ${remainingTargets > 0 ? `✨ ${remainingTargets} à sauver` : `✓ ${s.targetsSavedCount}/${s.totalTargetsCount}`}
        </span>
        <span class="hud-badge" style="background:rgba(168,85,247,0.18);color:#C084FC">
          ${remainingNeurons > 0 ? `🦠 ${remainingNeurons} neurones` : "🧠 Purifiés"}
        </span>
        <span class="hud-badge" style="background:rgba(16,185,129,0.18);color:#10B981;cursor:pointer" id="hud-energy-chip" title="Cliquez pour projeter un remède">
          ⚡ ${s.purificationEnergy} pts
        </span>
      `;
      const energyChip = document.getElementById("hud-energy-chip");
      if (energyChip) {
        energyChip.onclick = () => {
          if (s.purificationEnergy >= 10) {
            s.isVirtueMenuOpen = true;
            syncGameModals();
          } else {
            showToast("Il faut au moins 10 pts d'Énergie (sauvez une pensée pure ✨ ou recueillez une relique).");
          }
        };
      }
    }

    const relicsVal = document.getElementById("hud-relics-val");
    if (relicsVal) {
      relicsVal.textContent = `${s.collectedCount}/3 ${s.isBrainBonusCollected ? "+ 🧠✓" : "+ 🧠"}`;
    }

    const scoreVal = document.getElementById("hud-score-val");
    if (scoreVal) scoreVal.textContent = s.score;

    // Active status banners (Sanctuary, Mire, Obscurantism, Active PowerUps)
    const bannerContainer = document.getElementById("hud-banners");
    if (bannerContainer) {
      const banners = [];
      if (s.activeSanctuaryFruit) {
        banners.push(`<div class="hud-sub-banner" style="color:#10B981;border-color:#10B981">🕊️ Sanctuaire : ${s.activeSanctuaryFruit} (${Math.round(s.sanctuaryProgress * 100)}%) — Zone Protégée</div>`);
      }
      if (s.peaceAuraTimer > 0) {
        banners.push(`<div class="hud-sub-banner" style="color:#10B981;border-color:#10B981">🛡️ Pensée Ancrée : Aura de Paix (${Math.ceil(s.peaceAuraTimer)}s)</div>`);
      }
      if (s.isInMentalMire) {
        banners.push(`<div class="hud-sub-banner" style="color:#A3E635;border-color:#A3E635">🕸️ Marécage Mental : Vitesse réduite</div>`);
      }
      if (s.nearbyTargetNeuronId !== null && s.purificationEnergy >= 10) {
        banners.push(`<div class="hud-sub-banner" style="color:#00E5FF;border-color:#00E5FF;cursor:pointer" id="banner-open-remedy">✨ Neurone corrompu proche : Cliquez ici ou sur votre sphère pour projeter une Vertu (10 Énergie)</div>`);
      }
      bannerContainer.innerHTML = banners.join("");
      const btnRem = document.getElementById("banner-open-remedy");
      if (btnRem) {
        btnRem.onclick = () => {
          s.isVirtueMenuOpen = true;
          syncGameModals();
        };
      }
    }
  }

  function syncGameModals() {
    const s = G.state;
    updateGameHUD();

    // 1. Mission Start Briefing Modal
    const briefingModal = document.getElementById("modal-mission-briefing");
    if (briefingModal) {
      briefingModal.classList.toggle("open", Boolean(s.isMissionBriefingOpen));
      if (s.isMissionBriefingOpen) {
        document.getElementById("briefing-sector-title").textContent = `${s.sector.name} — ${s.sector.subtitle}`;
        const listEl = document.getElementById("briefing-unlocks-list");
        listEl.innerHTML = s.newUnlockedElements.map(u => `
          <div class="glossary-item-card" style="border-color:${u.color}55">
            <div class="glossary-preview-box" style="font-size:24px;border-color:${u.color}">${u.emoji}</div>
            <div>
              <div style="font-size:11px;font-weight:700;color:${u.color}">${u.category}</div>
              <div style="font-size:14px;font-weight:700;color:#F8FAFC;margin:2px 0">${u.title}</div>
              <div style="font-size:12.5px;color:#94A3B8;line-height:1.45">${u.desc}</div>
            </div>
          </div>
        `).join("");
      }
    }

    // 2. Virtue Remedy Modal
    const virtueModal = document.getElementById("modal-virtue-remedy");
    if (virtueModal) {
      virtueModal.classList.toggle("open", Boolean(s.isVirtueMenuOpen));
      if (s.isVirtueMenuOpen) {
        const neuron = s.infectedNeurons.find(n => n.id === s.nearbyTargetNeuronId && !n.isPurified) ||
                       s.infectedNeurons.find(n => !n.isPurified);
        const symptomEl = document.getElementById("virtue-symptom-box");
        if (neuron) {
          const corr = C.NeuronCorruptionTypes[neuron.corruptionType];
          symptomEl.innerHTML = `
            <div style="padding:10px 12px;border-radius:12px;background:${corr.corruptedColorHex}22;border:1px solid ${corr.corruptedColorHex}">
              <div style="font-weight:700;color:${corr.corruptedColorHex};font-size:13.5px">🦠 ${corr.displayName}</div>
              <div style="font-size:12px;color:#F8FAFC">Symptôme : ${corr.symptomLabel}</div>
            </div>
          `;
        } else {
          symptomEl.innerHTML = `<div style="font-size:12.5px;color:#94A3B8">Choisissez une vertu pour dissoudre les obstacles dogmatiques ou purifier les neurones :</div>`;
        }

        const remList = document.getElementById("virtue-options-list");
        const unlockedRemedies = Object.values(C.VirtueRemedyTypes).filter(r => r.minLevel <= s.levelNumber);
        remList.innerHTML = "";
        unlockedRemedies.forEach(r => {
          const btn = document.createElement("button");
          btn.className = "virtue-option-btn";
          btn.style.borderColor = `${r.colorHex}66`;
          btn.innerHTML = `
            <div>
              <div style="font-weight:700;font-size:13.5px;color:${r.colorHex}">${r.emoji} ${r.displayName}</div>
              <div style="font-size:11.5px;color:#94A3B8">${r.description}</div>
            </div>
            <span class="mini-tag" style="background:${r.colorHex}22;color:${r.colorHex};font-family:var(--font-display)">${r.word}</span>
          `;
          btn.onclick = () => {
            if (!G.fireVirtueRemedy(r.id)) {
              showToast("Aucune cible à portée de tir !");
            }
            syncGameModals();
          };
          remList.appendChild(btn);
        });
      }
    }

    // 3. Pause Modal
    const pauseModal = document.getElementById("modal-pause");
    if (pauseModal) {
      pauseModal.classList.toggle("open", Boolean(s.isPaused));
    }

    // 4. Level Complete Modal
    const completeModal = document.getElementById("modal-level-complete");
    if (completeModal) {
      completeModal.classList.toggle("open", Boolean(s.isLevelComplete));
      if (s.isLevelComplete) {
        document.getElementById("complete-score-text").textContent = `Score final de la mission : ${s.score} pts`;
        const listEl = document.getElementById("complete-meditations-list");
        listEl.innerHTML = s.newlyCollectedThisLevel.map(m => `
          <div class="meditation-card" style="padding:12px" data-med-id="${m.id}">
            <div style="display:flex;justify-content:space-between;align-items:center">
              <span class="mini-tag">${m.fruit}</span>
              <span style="font-size:11.5px;color:var(--cyan);font-weight:700">${m.reference}</span>
            </div>
            <div style="font-size:12px;color:#94A3B8">${m.situation}</div>
            <div style="font-size:13px;color:#F8FAFC;line-height:1.45">« ${m.content} »</div>
          </div>
        `).join("");
      }
    }

    // 5. Game Over Modal
    const gameOverModal = document.getElementById("modal-game-over");
    if (gameOverModal) {
      gameOverModal.classList.toggle("open", Boolean(s.isGameOver));
    }
  }

  // ==========================================================================
  // END-MISSION TARGETS SCORE ANIMATED TALLY MODAL
  // ==========================================================================
  let tallyInterval = null;
  function openEndMissionTallyModal() {
    const s = G.state;
    const modal = document.getElementById("modal-end-tally");
    if (!modal) return;
    modal.classList.add("open");

    const baseScore = s.score;
    const saved = s.targetsSavedCount;
    const missed = Math.max(0, s.totalTargetsCount - saved);

    let animSaved = 0;
    let animMissed = 0;
    let displayScore = baseScore;
    let phase = "SAVED";

    const btnValidate = document.getElementById("btn-validate-tally");
    btnValidate.disabled = true;

    function renderTallyStep() {
      document.getElementById("tally-saved-count").textContent = `${animSaved} / ${saved}`;
      document.getElementById("tally-saved-pts").textContent = `+${animSaved * 10} pts`;
      document.getElementById("tally-missed-count").textContent = `${animMissed} / ${missed}`;
      document.getElementById("tally-missed-pts").textContent = `-${animMissed * 20} pts`;
      document.getElementById("tally-total-score").textContent = `${displayScore} pts`;
    }

    renderTallyStep();
    if (tallyInterval) clearInterval(tallyInterval);

    tallyInterval = setInterval(() => {
      if (phase === "SAVED") {
        if (animSaved < saved) {
          animSaved++;
          displayScore += 10;
          A.playScoreTick(true);
          renderTallyStep();
        } else {
          phase = "MISSED";
        }
      } else if (phase === "MISSED") {
        if (animMissed < missed) {
          animMissed++;
          displayScore = Math.max(0, displayScore - 20);
          A.playScoreTick(false);
          renderTallyStep();
        } else {
          phase = "DONE";
          clearInterval(tallyInterval);
          tallyInterval = null;
          btnValidate.disabled = false;
        }
      }
    }, 150);

    btnValidate.onclick = () => {
      if (tallyInterval) {
        clearInterval(tallyInterval);
        tallyInterval = null;
      }
      const delta = saved * 10 - missed * 20;
      s.score = Math.max(0, s.score + delta);
      s.isEndTargetsScoreDialogOpen = false;
      s.isLevelComplete = true;
      modal.classList.remove("open");

      // Persist level completion & record
      const prevRec = appState.levelRecords[s.levelNumber] || { bestScore: 0, brainBonusCollected: false };
      appState.levelRecords[s.levelNumber] = {
        isCompleted: true,
        bestScore: Math.max(prevRec.bestScore || 0, s.score),
        collectedCount: s.collectedCount,
        brainBonusCollected: Boolean(prevRec.brainBonusCollected || s.isBrainBonusCollected)
      };
      if (s.levelNumber >= appState.unlockedLevel && appState.unlockedLevel < 135) {
        appState.unlockedLevel = s.levelNumber + 1;
      }
      appState.totalScore += s.score;
      saveState();
      A.playLevelComplete();
      syncGameModals();
    };
  }

  // ==========================================================================
  // MEDITATION READER MODAL (WITH KEYWORD CHALLENGE, ANCHOR THOUGHT & SHARE)
  // ==========================================================================
  function openDirectMeditationModal(med, isFromGameRelic = false) {
    const modal = document.getElementById("modal-meditation-reader");
    if (!modal || !med) return;
    modal.classList.add("open");

    const isBrain = med.id >= 1001 && med.id <= 1072;
    const challenge = isFromGameRelic && !isBrain ? C.createKeywordChallenge(med) : null;
    let isDecoded = !challenge;

    const fruitBadge = document.getElementById("reader-fruit-badge");
    const sitText = document.getElementById("reader-situation");
    const refText = document.getElementById("reader-reference");
    const contentBox = document.getElementById("reader-content");
    const challengeBox = document.getElementById("reader-challenge-box");
    const feelingsBox = document.getElementById("reader-feelings-box");
    const reflectionOut = document.getElementById("reader-reflection-output");
    const anchorBtn = document.getElementById("btn-reader-anchor");
    const shareBtn = document.getElementById("btn-reader-share");

    fruitBadge.textContent = med.fruit;
    sitText.textContent = `Situation : ${med.situation}`;
    refText.textContent = med.reference;
    reflectionOut.style.display = "none";
    anchorBtn.style.display = isFromGameRelic ? "inline-flex" : "none";

    function renderDecodeState() {
      if (isDecoded) {
        contentBox.textContent = `« ${med.content} »`;
        challengeBox.style.display = "none";
      } else {
        contentBox.textContent = `« ${challenge.maskedContent} »`;
        challengeBox.style.display = "block";
        const optsRow = document.getElementById("reader-challenge-options");
        optsRow.innerHTML = "";
        challenge.options.forEach(opt => {
          const b = document.createElement("button");
          b.className = "chip-btn";
          b.textContent = opt;
          b.onclick = () => {
            if (C.normalizeText(opt) === C.normalizeText(challenge.correctWord)) {
              isDecoded = true;
              G.state.score += 25;
              A.playTargetSaved();
              showToast("✨ Mot-clé restauré ! +25 pts de Sagesse");
              renderDecodeState();
              updateGameHUD();
            } else {
              A.playObstacleHit();
              showToast("Essayez un autre mot-clé...");
            }
          };
          optsRow.appendChild(b);
        });
      }
    }
    renderDecodeState();

    // 6 Feelings chips
    feelingsBox.innerHTML = "";
    C.FeelingTypes.forEach(f => {
      const chip = document.createElement("button");
      chip.className = "chip-btn";
      chip.textContent = `${f.emoji} ${f.label}`;
      chip.onclick = () => {
        A.playClick();
        reflectionOut.style.display = "block";
        reflectionOut.textContent = C.generateReflection(med, f);
      };
      feelingsBox.appendChild(chip);
    });

    // Anchor thought button (+12s Peace Aura)
    anchorBtn.onclick = () => {
      G.activatePeaceAura();
      G.state.lastCollectedMeditation = null;
      modal.classList.remove("open");
      showToast("🕊️ Pensée ancrée : Aura de Paix active pendant 12 secondes !");
      updateGameHUD();
    };

    // Share button (+3 Positive Thoughts)
    const alreadyShared = appState.claimedShareIds.includes(med.id);
    shareBtn.textContent = alreadyShared ? "📤 Partager cette pensée" : "📤 Partager (+3 ✨ Pensées)";
    shareBtn.onclick = async () => {
      const shareText = `« ${med.content} » — ${med.reference} (${med.fruit} • ${med.situation}) via Noôrbis`;
      if (navigator.share) {
        try { await navigator.share({ title: med.reference, text: shareText }); } catch (_) {}
      } else if (navigator.clipboard) {
        try {
          await navigator.clipboard.writeText(shareText);
          showToast("📋 Texte copié dans le presse-papiers !");
        } catch (_) {}
      }
      if (!appState.claimedShareIds.includes(med.id)) {
        appState.claimedShareIds.push(med.id);
        appCallbacks.onPositiveThoughtEarned(3);
        shareBtn.textContent = "📤 Partager cette pensée (Bonus reçu ✓)";
        showToast("🎁 +3 Pensées Positives gagnées pour votre partage !");
        saveState();
      }
    };

    document.getElementById("btn-reader-close").onclick = () => {
      G.state.lastCollectedMeditation = null;
      modal.classList.remove("open");
      if (isFromGameRelic) {
        // Re-check if level finished while reading
        const allFruitRelicsDone = G.state.relics.filter(r => !r.isBrainBonus).every(r => r.isCollected);
        const allTargetsResolved = (G.state.targetsSavedCount + G.state.targetsDestroyedCount) >= G.state.totalTargetsCount;
        const allNeuronsPurified = G.state.neuronsPurifiedCount >= G.state.totalNeuronsCount;
        if (allFruitRelicsDone && allTargetsResolved && allNeuronsPurified && !G.state.isLevelComplete) {
          G.state.isEndTargetsScoreDialogOpen = true;
          openEndMissionTallyModal();
        }
      }
    };
  }

  // ==========================================================================
  // INVENTORY VIEW
  // ==========================================================================
  function renderInventory() {
    const brainCount = appState.collectedItems.filter(i => i.id >= 1001 && i.id <= 1072).length;
    const fruitCount = appState.collectedItems.filter(i => i.id < 1001).length;
    const totalCount = appState.collectedItems.length;

    const countEl = document.getElementById("inv-progress-count");
    const barEl = document.getElementById("inv-progress-fill");
    const brainBadge = document.getElementById("inv-brain-badge");
    if (countEl) countEl.textContent = `${fruitCount} / 810`;
    if (barEl) barEl.style.width = `${Math.min(100, (totalCount / 882) * 100)}%`;
    if (brainBadge) brainBadge.textContent = `🧠 Bonus Cerveau (Pensée libre & Droit) : ${brainCount} / 72`;

    // Filter chips
    const filterRow = document.getElementById("inv-filter-row");
    if (filterRow) {
      const filters = [
        { key: null, label: `Tous (${totalCount})` },
        { key: "__BRAIN__", label: `🧠 Cerveau (${brainCount}/72)` },
        ...C.FRUITS.map(f => ({ key: f, label: f }))
      ];
      filterRow.innerHTML = "";
      filters.forEach(f => {
        const btn = document.createElement("button");
        btn.className = `chip-btn ${appState.inventoryFilter === f.key ? "active" : ""}`;
        btn.textContent = f.label;
        btn.onclick = () => {
          appState.inventoryFilter = f.key;
          renderInventory();
        };
        filterRow.appendChild(btn);
      });
    }

    // Filter & Sort items
    const q = C.normalizeText(appState.inventorySearch);
    let items = appState.collectedItems.filter(item => {
      const matchesFilter =
        appState.inventoryFilter === null
          ? true
          : appState.inventoryFilter === "__BRAIN__"
          ? item.id >= 1001 && item.id <= 1072
          : C.normalizeText(item.fruit) === C.normalizeText(appState.inventoryFilter);

      const matchesSearch =
        !q ||
        C.normalizeText(item.content).includes(q) ||
        C.normalizeText(item.reference).includes(q) ||
        C.normalizeText(item.situation).includes(q) ||
        C.normalizeText(item.fruit).includes(q);

      return matchesFilter && matchesSearch;
    });

    items.sort((a, b) =>
      appState.settings.inventorySortAscending
        ? (a.collectedAt || 0) - (b.collectedAt || 0)
        : (b.collectedAt || 0) - (a.collectedAt || 0)
    );

    const grid = document.getElementById("inv-meditation-grid");
    if (!grid) return;
    if (items.length === 0) {
      grid.innerHTML = `
        <div style="text-align:center;padding:48px 16px;color:#94A3B8">
          <div style="font-size:42px;margin-bottom:10px">🌌</div>
          <div style="font-weight:700;color:#F8FAFC;margin-bottom:4px">Aucune méditation trouvée</div>
          <div style="font-size:13px">Explorez les 135 secteurs spatiaux pour recueillir les textes et les 72 fragments Cerveau.</div>
        </div>
      `;
      return;
    }

    grid.innerHTML = "";
    items.forEach(item => {
      const isBrain = item.id >= 1001 && item.id <= 1072;
      const card = document.createElement("div");
      card.className = `meditation-card ${isBrain ? "brain-card" : ""}`;
      card.innerHTML = `
        <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;flex-wrap:wrap">
          <span class="mini-tag ${isBrain ? "brain" : ""}">${item.fruit}</span>
          <span style="font-family:var(--font-display);font-size:12px;color:var(--cyan);font-weight:700">${item.reference}</span>
        </div>
        <div style="font-size:12px;color:#94A3B8">Situation : ${item.situation}</div>
        <div style="font-size:13.5px;color:#F8FAFC;line-height:1.5">« ${item.content} »</div>
      `;
      card.onclick = () => openDirectMeditationModal(item, false);
      grid.appendChild(card);
    });
  }

  // ==========================================================================
  // GLOSSARY MODAL (TOUTES LES ENTITÉS + 29 VARIANTES JURIDIQUES & COGNITIVES)
  // ==========================================================================
  function openGlossaryModal() {
    const modal = document.getElementById("modal-glossary");
    if (!modal) return;
    modal.classList.add("open");
    renderGlossaryContent();
  }

  function renderGlossaryContent() {
    const cat = appState.glossaryCategory;
    document.querySelectorAll("[data-glossary-cat]").forEach(btn => {
      const val = btn.dataset.glossaryCat === "ALL" ? null : btn.dataset.glossaryCat;
      btn.classList.toggle("active", val === cat);
    });

    const entries = [];

    if (!cat || cat === "RELIC") {
      entries.push(
        { emoji: "✨", title: "Étincelle de l'Esprit (Votre Sphère)", badge: "Joueur • Cliquez : Remède", color: "#00E5FF", desc: "Votre conscience lumineuse. Naviguez au doigt, à la souris, au clavier ou via le joystick circulaire. Cliquez sur la sphère près d'un neurone infecté (≥ 10 Énergie) pour projeter une Vertu !" },
        { emoji: "🕊️", title: "Relique de Fruit & Sanctuaire d'Harmonisation", badge: "Objectif (+100 pts)", color: "#F59E0B", desc: "Chaque relique est entourée d'un Sanctuaire protecteur qui repousse les obstacles et harmonise un texte des 9 Fruits de l'Esprit." },
        { emoji: "🧠", title: "Relique Cerveau (72 Fragments Droit & Pensée Libre)", badge: "Bonus Cerveau (+150 pts)", color: "#F43F5E", desc: "Présente dans chaque secteur (1 par niveau), elle débloque l'un des 72 textes sur l'esprit critique, les vices du consentement, la neurobiologie et les libertés fondamentales." }
      );
    }

    if (!cat || cat === "TARGET") {
      Object.values(C.RescueTargetTypes).forEach(t => {
        entries.push({
          emoji: t.symbol,
          title: t.displayName,
          badge: "Pensée Pure (+10 / -20 pts)",
          color: t.colorHex,
          desc: `${t.description}. Toutes les 50 pensées sauvées ou neurones purifiés, vous gagnez +1 Cœur permanent (jusqu'à 7 cœurs) !`
        });
      });
      Object.values(C.NeuronCorruptionTypes).forEach(n => {
        const ideal = C.VirtueRemedyTypes[n.idealRemedy];
        entries.push({
          emoji: "🦠",
          title: `${n.displayName} (Niv. ${n.minLevel}+)`,
          badge: `Remède 100% : ${ideal.word}`,
          color: n.corruptedColorHex,
          desc: `Symptôme : ${n.symptomLabel}. Projetez la vertu « ${ideal.displayName} » pour le purifier instantanément à 100%.`
        });
      });
    }

    if (!cat || cat === "OBSTACLE") {
      Object.values(C.ObstacleTypes).forEach(o => {
        entries.push({
          emoji: "⚠️",
          title: `${o.displayName} (Niv. ${o.minLevel}+)`,
          badge: "Archétype d'Obstacle",
          color: o.strokeHex,
          desc: o.description
        });
      });
      C.SimpleObstacleVariants.forEach(v => {
        const catInfo = C.SimpleObstacleCategories[v.category];
        entries.push({
          emoji: v.symbolEmoji,
          title: `${v.displayName} (Niv. ${v.minLevel}+)`,
          badge: catInfo ? catInfo.displayName : v.category,
          color: v.accentHex,
          desc: v.definition
        });
      });
    }

    if (!cat || cat === "BONUS") {
      Object.values(C.VirtueRemedyTypes).forEach(r => {
        entries.push({
          emoji: r.emoji,
          title: `Remède : ${r.displayName} (Niv. ${r.minLevel}+)`,
          badge: `Mot projeté : ${r.word}`,
          color: r.colorHex,
          desc: r.description
        });
      });
      Object.values(C.PowerUpTypes).forEach(p => {
        entries.push({
          emoji: p.iconLabel,
          title: `${p.displayName} (Niv. ${p.minLevel}+)`,
          badge: "Bonus Protecteur",
          color: p.colorHex,
          desc: p.description
        });
      });
    }

    const listEl = document.getElementById("glossary-entries-list");
    if (!listEl) return;
    listEl.innerHTML = entries.map(e => `
      <div class="glossary-item-card" style="border-color:${e.color}44">
        <div class="glossary-preview-box" style="font-size:24px;border-color:${e.color}88">${e.emoji}</div>
        <div style="flex:1">
          <div style="display:flex;justify-content:space-between;align-items:center;gap:8px;flex-wrap:wrap">
            <span style="font-weight:700;font-size:14px;color:#F8FAFC">${e.title}</span>
            <span class="mini-tag" style="background:${e.color}22;color:${e.color}">${e.badge}</span>
          </div>
          <div style="font-size:12.5px;color:#94A3B8;margin-top:4px;line-height:1.45">${e.desc}</div>
        </div>
      </div>
    `).join("");
  }

  // ==========================================================================
  // SETTINGS MODAL
  // ==========================================================================
  function openSettingsModal() {
    const modal = document.getElementById("modal-settings");
    if (!modal) return;
    modal.classList.add("open");

    const s = appState.settings;
    document.getElementById("chk-sfx").checked = Boolean(s.sfxEnabled);
    document.getElementById("chk-sort-asc").checked = Boolean(s.inventorySortAscending);
    document.getElementById("chk-joystick").checked = Boolean(s.joystickEnabled);
    document.getElementById("chk-direct-text").checked = Boolean(s.directTextEnabled);
    document.getElementById("chk-grid").checked = Boolean(s.showGridEnabled);
    document.getElementById("chk-minimap").checked = Boolean(s.showMiniMapEnabled);

    const musicContainer = document.getElementById("settings-music-chips");
    if (musicContainer) {
      const options = [
        { val: -1, label: "🔄 Auto (Selon Secteur)" },
        { val: -2, label: "🔇 Désactivée" },
        ...A.MUSIC_THEME_NAMES.map((name, idx) => ({ val: idx, label: `${idx + 1}. ${name}` }))
      ];
      musicContainer.innerHTML = "";
      options.forEach(opt => {
        const chip = document.createElement("button");
        chip.className = `chip-btn ${s.musicThemeOverride === opt.val ? "active" : ""}`;
        chip.textContent = opt.label;
        chip.onclick = () => {
          s.musicThemeOverride = opt.val;
          A.setConfig(s.sfxEnabled, s.musicThemeOverride, G.state.levelNumber);
          saveState();
          openSettingsModal();
        };
        musicContainer.appendChild(chip);
      });
    }
  }

  // ==========================================================================
  // STANDALONE SINGLE-FILE HTML5 BUNDLE EXPORTER FOR GITHUB PAGES
  // ==========================================================================
  async function exportStandaloneHtmlBundle() {
    A.playClick();
    showToast("📦 Préparation du fichier index.html autonome pour GitHub Pages...");
    try {
      await C.loadMeditationsMatrix();
      const [cssResp, catResp, audResp, gameResp, appResp] = await Promise.all([
        fetch("./css/style.css").catch(() => fetch("./style.css")),
        fetch("./js/catalog.js").catch(() => fetch("./catalog.js")),
        fetch("./js/audio.js").catch(() => fetch("./audio.js")),
        fetch("./js/game.js").catch(() => fetch("./game.js")),
        fetch("./js/app.js").catch(() => fetch("./app.js"))
      ]);

      const cssText = cssResp && cssResp.ok ? await cssResp.text() : "";
      const catText = catResp && catResp.ok ? await catResp.text() : "";
      const audText = audResp && audResp.ok ? await audResp.text() : "";
      const gameText = gameResp && gameResp.ok ? await gameResp.text() : "";
      const appText = appResp && appResp.ok ? await appResp.text() : "";
      const matrixJson = JSON.stringify(C.getAllMeditations());

      const bodyContent = document.getElementById("app-shell").outerHTML;
      const modalsContent = document.getElementById("modals-root").outerHTML;

      const fullHtml = `<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
  <title>Noôrbis — L'Étincelle de l'Esprit (135 Niveaux)</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@500;700;800&family=Plus+Jakarta+Sans:wght@400;500;600;700&display=swap" rel="stylesheet">
  <style>${cssText}</style>
</head>
<body>
  ${bodyContent}
  ${modalsContent}
  <div id="toast-container"></div>
  <script>window.NOORBIS_MEDITATIONS_MATRIX = ${matrixJson};<\/script>
  <script>${catText}<\/script>
  <script>${audText}<\/script>
  <script>${gameText}<\/script>
  <script>${appText}<\/script>
</body>
</html>`;

      const blob = new Blob([fullHtml], { type: "text/html;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = "index.html";
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      showToast("✅ Fichier index.html autonome téléchargé ! Prêt pour GitHub Pages.");
    } catch (err) {
      showToast("⚠️ Vous pouvez utiliser directement les fichiers index.html, css/ et js/ du dépôt sur GitHub Pages.");
    }
  }

  // ==========================================================================
  // EXPORT & IMPORT DE LA SAUVEGARDE LOCALE (.JSON / LOCALSTORAGE)
  // ==========================================================================
  function exportLocalSaveJson() {
    A.playClick();
    try {
      saveState();
      const rawFromStorage = localStorage.getItem(STORAGE_KEY);
      const parsedState = rawFromStorage ? JSON.parse(rawFromStorage) : appState;
      const exportPayload = {
        app: "Noôrbis",
        version: 2,
        storageKey: STORAGE_KEY,
        exportedAt: new Date().toISOString(),
        ...parsedState
      };
      const jsonString = JSON.stringify(exportPayload, null, 2);

      if (window.AndroidWebBridge && typeof window.AndroidWebBridge.exportSaveJson === "function") {
        window.AndroidWebBridge.exportSaveJson(jsonString);
        showToast("📥 Sauvegarde locale (.json) prête à être exportée !");
        return;
      }

      const blob = new Blob([jsonString], { type: "application/json;charset=utf-8" });
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      const dateStamp = new Date().toISOString().slice(0, 10);
      a.href = url;
      a.download = `noorbis_sauvegarde_${dateStamp}.json`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
      showToast("📥 Sauvegarde locale téléchargée (.json) !");
    } catch (err) {
      showToast("⚠️ Impossible d'exporter la sauvegarde locale.");
    }
  }

  function applyImportedSaveObject(imported) {
    if (!imported || typeof imported !== "object") {
      throw new Error("Format JSON invalide");
    }
    const source = imported.state && typeof imported.state === "object" ? imported.state : imported;

    if (typeof source.selectedColorId === "string") {
      appState.selectedColorId = source.selectedColorId;
    }
    if (typeof source.unlockedLevel === "number") {
      appState.unlockedLevel = Math.max(1, Math.min(135, Math.floor(source.unlockedLevel)));
    }
    if (typeof source.totalScore === "number") {
      appState.totalScore = Math.max(0, Math.floor(source.totalScore));
    }
    if (typeof source.positiveThoughtsCount === "number") {
      appState.positiveThoughtsCount = Math.max(0, Math.floor(source.positiveThoughtsCount));
    }
    if (Array.isArray(source.collectedItems)) {
      appState.collectedItems = source.collectedItems;
    }
    if (source.levelRecords && typeof source.levelRecords === "object") {
      appState.levelRecords = source.levelRecords;
    }
    if (Array.isArray(source.claimedShareIds)) {
      appState.claimedShareIds = source.claimedShareIds;
    }
    if (source.settings && typeof source.settings === "object") {
      appState.settings = Object.assign({}, appState.settings, source.settings);
    }

    appState.maxShields = Math.min(7, 3 + Math.floor((appState.positiveThoughtsCount || 0) / 50));

    // Écriture dans le localStorage puis relecture pour garantir la cohérence
    localStorage.setItem(STORAGE_KEY, JSON.stringify(appState));
    loadState();

    A.setConfig(appState.settings.sfxEnabled, appState.settings.musicThemeOverride, G.state.levelNumber);
    G.initLevel(appState.unlockedLevel, appState);
    renderHangar();
    renderInventory();
    openSettingsModal();
    updateGameHUD();
  }

  function importLocalSaveJson(file) {
    if (!file) return;
    const reader = new FileReader();
    reader.onload = (event) => {
      try {
        const text = event.target.result;
        const parsed = JSON.parse(text);
        applyImportedSaveObject(parsed);
        A.playTargetSaved();
        showToast("✅ Sauvegarde importée et enregistrée dans le localStorage !");
      } catch (err) {
        A.playObstacleHit();
        showToast("❌ Fichier .json invalide ou corrompu.");
      }
    };
    reader.onerror = () => {
      showToast("❌ Erreur lors de la lecture du fichier .json.");
    };
    reader.readAsText(file, "UTF-8");
  }

  // ==========================================================================
  // INITIALIZATION
  // ==========================================================================
  window.addEventListener("DOMContentLoaded", async () => {
    loadState();
    await C.loadMeditationsMatrix();

    // Navigation buttons
    document.querySelectorAll(".nav-item").forEach(btn => {
      btn.addEventListener("click", () => switchTab(btn.dataset.tab));
    });

    // Hangar filter buttons
    document.querySelectorAll("[data-hangar-filter]").forEach(btn => {
      btn.addEventListener("click", () => {
        appState.hangarFilter = btn.dataset.hangarFilter;
        renderHangar();
      });
    });

    // Glossary buttons
    const btnOpenGlossary = document.getElementById("btn-open-glossary");
    if (btnOpenGlossary) btnOpenGlossary.onclick = openGlossaryModal;
    const btnHudGlossary = document.getElementById("btn-hud-glossary");
    if (btnHudGlossary) btnHudGlossary.onclick = () => {
      G.state.isPaused = true;
      openGlossaryModal();
    };
    const btnCloseGlossary = document.getElementById("btn-close-glossary");
    if (btnCloseGlossary) btnCloseGlossary.onclick = () => {
      document.getElementById("modal-glossary").classList.remove("open");
    };
    document.querySelectorAll("[data-glossary-cat]").forEach(btn => {
      btn.onclick = () => {
        appState.glossaryCategory = btn.dataset.glossaryCat === "ALL" ? null : btn.dataset.glossaryCat;
        renderGlossaryContent();
      };
    });

    // Export standalone HTML button
    const btnExportHtml = document.getElementById("btn-export-html");
    if (btnExportHtml) btnExportHtml.onclick = exportStandaloneHtmlBundle;

    // HUD Buttons
    const btnHudPause = document.getElementById("btn-hud-pause");
    if (btnHudPause) btnHudPause.onclick = () => {
      G.state.isPaused = !G.state.isPaused;
      syncGameModals();
    };
    const btnHudSettings = document.getElementById("btn-hud-settings");
    if (btnHudSettings) btnHudSettings.onclick = () => {
      G.state.isPaused = true;
      openSettingsModal();
    };

    // Mission Briefing close
    const btnStartMission = document.getElementById("btn-start-mission");
    if (btnStartMission) btnStartMission.onclick = () => {
      G.state.isMissionBriefingOpen = false;
      syncGameModals();
    };

    // Virtue Remedy modal close
    const btnCloseVirtue = document.getElementById("btn-close-virtue");
    if (btnCloseVirtue) btnCloseVirtue.onclick = () => {
      G.state.isVirtueMenuOpen = false;
      syncGameModals();
    };

    // Pause modal actions
    document.getElementById("btn-resume-game").onclick = () => {
      G.state.isPaused = false;
      syncGameModals();
    };
    document.getElementById("btn-restart-level").onclick = () => {
      G.initLevel(G.state.levelNumber, appState);
      syncGameModals();
    };
    document.getElementById("btn-quit-to-hangar").onclick = () => {
      G.state.isPaused = false;
      switchTab("hangar");
      syncGameModals();
    };

    // Level Complete modal actions
    document.getElementById("btn-next-level").onclick = () => {
      const nextLvl = Math.min(135, G.state.levelNumber + 1);
      startLevelAndSwitch(nextLvl);
    };
    document.getElementById("btn-complete-hangar").onclick = () => {
      G.state.isLevelComplete = false;
      switchTab("hangar");
      syncGameModals();
    };

    // Game Over modal actions
    document.getElementById("btn-retry-gameover").onclick = () => {
      G.initLevel(G.state.levelNumber, appState);
      syncGameModals();
    };
    document.getElementById("btn-gameover-hangar").onclick = () => {
      G.state.isGameOver = false;
      switchTab("hangar");
      syncGameModals();
    };

    // Inventory Search
    const searchInput = document.getElementById("inv-search-input");
    const searchClear = document.getElementById("inv-search-clear");
    if (searchInput) {
      searchInput.addEventListener("input", () => {
        appState.inventorySearch = searchInput.value;
        renderInventory();
      });
    }
    if (searchClear) {
      searchClear.onclick = () => {
        appState.inventorySearch = "";
        if (searchInput) searchInput.value = "";
        renderInventory();
      };
    }

    // Settings checkboxes
    const bindChk = (id, key) => {
      const el = document.getElementById(id);
      if (!el) return;
      el.onchange = () => {
        appState.settings[key] = el.checked;
        A.setConfig(appState.settings.sfxEnabled, appState.settings.musicThemeOverride, G.state.levelNumber);
        saveState();
        if (appState.currentTab === "inventory") renderInventory();
      };
    };
    bindChk("chk-sfx", "sfxEnabled");
    bindChk("chk-sort-asc", "inventorySortAscending");
    bindChk("chk-joystick", "joystickEnabled");
    bindChk("chk-direct-text", "directTextEnabled");
    bindChk("chk-grid", "showGridEnabled");
    bindChk("chk-minimap", "showMiniMapEnabled");

    // Export / Import JSON save buttons in Settings
    const btnExportSaveJson = document.getElementById("btn-export-save-json");
    const btnImportSaveJson = document.getElementById("btn-import-save-json");
    const inputImportSaveJson = document.getElementById("input-import-save-json");

    if (btnExportSaveJson) {
      btnExportSaveJson.onclick = exportLocalSaveJson;
    }
    if (btnImportSaveJson && inputImportSaveJson) {
      btnImportSaveJson.onclick = () => {
        A.playClick();
        inputImportSaveJson.value = "";
        inputImportSaveJson.click();
      };
      inputImportSaveJson.onchange = (e) => {
        const file = e.target.files && e.target.files[0];
        if (file) importLocalSaveJson(file);
      };
    }

    const btnUnlockAll = document.getElementById("btn-unlock-all-levels");
    if (btnUnlockAll) {
      btnUnlockAll.onclick = () => {
        appState.unlockedLevel = 135;
        saveState();
        renderHangar();
        showToast("🔓 Les 135 secteurs spatiaux sont maintenant débloqués !");
      };
    }

    document.getElementById("btn-close-settings").onclick = () => {
      document.getElementById("modal-settings").classList.remove("open");
    };

    // Initialize Hangar animated sphere & Game Canvas
    G.startHangarSphereAnimation(document.getElementById("hangar-sphere-canvas"), getSphereColor);
    G.bindCanvasEvents(document.getElementById("space-canvas"), appCallbacks);
    G.setOnStateChange(() => {
      updateGameHUD();
      syncGameModals();
    });
    G.initLevel(appState.unlockedLevel, appState);
    G.startLoop(appCallbacks);

    renderHangar();
    renderInventory();
  });
})();
